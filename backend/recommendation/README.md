# 混合推荐算法原型说明

## 1. 算法组成
- 基于用户的协同过滤（UserCF）：根据借阅行为计算用户相似度（Cosine Similarity）
- 基于内容的推荐（Content-Based）：对图书标题/作者/分类/简介做 TF-IDF 特征提取
- 用户画像加权（Profile）：结合用户院系（专业）与年级特征做偏好加分
- 结果融合（Hybrid）：`final = w_cf * cf + w_content * content + w_profile * profile`

## 2. Python 原型
脚本位置：`hybrid_recommender.py`

输入（stdin JSON）主要字段：
- `target_user_id`
- `top_n`
- `users[]`
- `books[]`
- `borrows[]`
- `config`：`collaborative_weight` / `content_weight` / `profile_weight` / `min_similarity`

输出（stdout JSON）：
- `recommendations[]`，包含 `book_id`、`score`、`reason`

## 3. 依赖安装
```bash
pip install -r backend/recommendation/requirements.txt
```

## 4. Java 交互
Spring 服务 `HybridRecommendationService` 通过进程调用 Python：
- 配置项：
  - `app.recommendation.python-command`
  - `app.recommendation.python-script`
  - `app.recommendation.default-top-n`
- 管理员触发重算接口：`POST /api/admin/recommendation/rebuild`
- 学生查询个性化推荐：`GET /api/student/recommendations`

## 5. 日志与过滤
- 自动过滤目标用户已借阅图书
- 每次生成推荐写入 `recommendation_list`（个性化类型）
- 同时记录 `operation_log`，操作类型：`RECOMMENDATION_GENERATE`
