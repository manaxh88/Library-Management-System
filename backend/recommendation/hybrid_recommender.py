import json
import re
import sys
from collections import defaultdict
from typing import Dict, List, Set

import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


ROLE_STUDENT = 2


def safe_text(value) -> str:
    if value is None:
        return ""
    return str(value).strip()


def normalize_scores(score_map: Dict[int, float]) -> Dict[int, float]:
    if not score_map:
        return {}
    max_score = max(score_map.values())
    if max_score <= 0:
        return {k: 0.0 for k in score_map}
    return {k: float(v / max_score) for k, v in score_map.items()}


def extract_grade(username: str) -> str:
    match = re.search(r"(20\d{2})", safe_text(username))
    return match.group(1) if match else ""


def user_based_collaborative_filtering(
    target_user_id: int,
    users: List[dict],
    borrows: List[dict],
    min_similarity: float,
) -> Dict[int, float]:
    borrowed_by_user: Dict[int, Set[int]] = defaultdict(set)
    all_books: Set[int] = set()

    for record in borrows:
        user_id = record.get("user_id")
        book_id = record.get("book_id")
        if user_id is None or book_id is None:
            continue
        borrowed_by_user[int(user_id)].add(int(book_id))
        all_books.add(int(book_id))

    if target_user_id not in borrowed_by_user or not all_books:
        return {}

    user_ids = sorted({int(u.get("user_id")) for u in users if u.get("user_id") is not None})
    if target_user_id not in user_ids:
        user_ids.append(target_user_id)
    user_ids = sorted(set(user_ids))

    book_ids = sorted(all_books)
    user_index = {uid: idx for idx, uid in enumerate(user_ids)}
    book_index = {bid: idx for idx, bid in enumerate(book_ids)}

    matrix = np.zeros((len(user_ids), len(book_ids)), dtype=float)
    for uid, books in borrowed_by_user.items():
        if uid not in user_index:
            continue
        for bid in books:
            if bid in book_index:
                matrix[user_index[uid], book_index[bid]] = 1.0

    target_idx = user_index[target_user_id]
    similarities = cosine_similarity(matrix[target_idx : target_idx + 1], matrix).flatten()

    target_borrowed = borrowed_by_user[target_user_id]
    candidate_scores: Dict[int, float] = defaultdict(float)

    for other_uid, sim in zip(user_ids, similarities):
        if other_uid == target_user_id:
            continue
        if sim < min_similarity:
            continue
        for bid in borrowed_by_user.get(other_uid, set()):
            if bid in target_borrowed:
                continue
            candidate_scores[bid] += float(sim)

    return normalize_scores(candidate_scores)


def content_based_recommendation(
    target_user_id: int,
    books: List[dict],
    borrows: List[dict],
) -> Dict[int, float]:
    books_by_id = {int(b["book_id"]): b for b in books if b.get("book_id") is not None}
    if not books_by_id:
        return {}

    user_borrowed = {
        int(item.get("book_id"))
        for item in borrows
        if item.get("user_id") == target_user_id and item.get("book_id") is not None
    }

    if not user_borrowed:
        return {}

    ordered_book_ids = sorted(books_by_id.keys())
    corpus = []
    for bid in ordered_book_ids:
        book = books_by_id[bid]
        text = " ".join(
            [
                safe_text(book.get("title")),
                safe_text(book.get("author")),
                safe_text(book.get("category")),
                safe_text(book.get("description")),
            ]
        )
        corpus.append(text)

    vectorizer = TfidfVectorizer(analyzer="char", ngram_range=(2, 4), min_df=1)
    tfidf = vectorizer.fit_transform(corpus)

    profile_indices = [ordered_book_ids.index(bid) for bid in user_borrowed if bid in books_by_id]
    if not profile_indices:
        return {}

    user_profile = tfidf[profile_indices].mean(axis=0)
    sim_scores = cosine_similarity(user_profile, tfidf).flatten()

    content_scores: Dict[int, float] = {}
    for idx, bid in enumerate(ordered_book_ids):
        if bid in user_borrowed:
            continue
        content_scores[bid] = float(sim_scores[idx])

    return normalize_scores(content_scores)


def profile_weight_recommendation(
    target_user_id: int,
    users: List[dict],
    books: List[dict],
    borrows: List[dict],
) -> Dict[int, float]:
    user_map = {int(u["user_id"]): u for u in users if u.get("user_id") is not None}
    target_user = user_map.get(target_user_id)
    if not target_user:
        return {}

    dept = safe_text(target_user.get("dept_name"))
    username = safe_text(target_user.get("username"))
    grade = safe_text(target_user.get("grade")) or extract_grade(username)

    dept_tokens = [token for token in re.split(r"[\s,，、/\\-]", dept) if token]
    grade_year = int(grade) if grade.isdigit() else 0

    borrowed_book_ids = {
        int(item.get("book_id"))
        for item in borrows
        if item.get("user_id") == target_user_id and item.get("book_id") is not None
    }

    profile_scores: Dict[int, float] = {}
    for book in books:
        bid = book.get("book_id")
        if bid is None:
            continue
        bid = int(bid)
        if bid in borrowed_book_ids:
            continue

        text = " ".join(
            [
                safe_text(book.get("category")),
                safe_text(book.get("title")),
                safe_text(book.get("description")),
            ]
        )

        score = 0.0
        for token in dept_tokens:
            if token and token in text:
                score += 0.4

        if grade_year >= 2024 and ("基础" in text or "入门" in text):
            score += 0.2
        if grade_year <= 2021 and ("进阶" in text or "研究" in text):
            score += 0.2

        profile_scores[bid] = score

    return normalize_scores(profile_scores)


def recommend(payload: dict) -> dict:
    target_user_id = int(payload.get("target_user_id"))
    top_n = int(payload.get("top_n", 10))

    users = payload.get("users", [])
    books = payload.get("books", [])
    borrows = payload.get("borrows", [])
    config = payload.get("config", {})

    collaborative_weight = float(config.get("collaborative_weight", 0.45))
    content_weight = float(config.get("content_weight", 0.35))
    profile_weight = float(config.get("profile_weight", 0.20))
    min_similarity = float(config.get("min_similarity", 0.10))

    cf_scores = user_based_collaborative_filtering(target_user_id, users, borrows, min_similarity)
    content_scores = content_based_recommendation(target_user_id, books, borrows)
    profile_scores = profile_weight_recommendation(target_user_id, users, books, borrows)

    borrowed_book_ids = {
        int(item.get("book_id"))
        for item in borrows
        if item.get("user_id") == target_user_id and item.get("book_id") is not None
    }

    candidate_book_ids = {
        int(book.get("book_id"))
        for book in books
        if book.get("book_id") is not None and int(book.get("book_id")) not in borrowed_book_ids
    }

    merged = []
    for bid in candidate_book_ids:
        cf = cf_scores.get(bid, 0.0)
        content = content_scores.get(bid, 0.0)
        profile = profile_scores.get(bid, 0.0)
        final_score = collaborative_weight * cf + content_weight * content + profile_weight * profile

        reasons = []
        if cf > 0:
            reasons.append("相似读者偏好")
        if content > 0:
            reasons.append("内容关键词匹配")
        if profile > 0:
            reasons.append("专业年级画像匹配")
        if not reasons:
            reasons.append("通用热门候选")

        merged.append(
            {
                "book_id": bid,
                "score": round(float(final_score), 6),
                "cf_score": round(float(cf), 6),
                "content_score": round(float(content), 6),
                "profile_score": round(float(profile), 6),
                "reason": " + ".join(reasons),
            }
        )

    merged.sort(key=lambda x: x["score"], reverse=True)
    return {"recommendations": merged[:top_n]}


def main() -> None:
    raw = sys.stdin.read()
    if not raw:
        print(json.dumps({"recommendations": []}, ensure_ascii=False))
        return

    payload = json.loads(raw)
    result = recommend(payload)
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
