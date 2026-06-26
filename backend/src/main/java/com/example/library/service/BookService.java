package com.example.library.service;

import com.example.library.dto.BookRequest;
import com.example.library.entity.BookInfo;
import com.example.library.repository.BookInfoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookInfoRepository bookInfoRepository;

    public BookService(BookInfoRepository bookInfoRepository) {
        this.bookInfoRepository = bookInfoRepository;
    }

    public Page<BookInfo> listBooks(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        if (!StringUtils.hasText(keyword)) {
            return bookInfoRepository.findAll(pageable);
        }
        return bookInfoRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
    }

    public BookInfo createBook(BookRequest request) {
        BookInfo book = new BookInfo();
        applyRequest(book, request);
        if (book.getCreateTime() == null) {
            book.setCreateTime(LocalDateTime.now());
        }
        return bookInfoRepository.save(book);
    }

    public Optional<BookInfo> updateBook(Long bookId, BookRequest request) {
        Optional<BookInfo> optionalBook = bookInfoRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            return Optional.empty();
        }

        BookInfo book = optionalBook.get();
        applyRequest(book, request);
        if (book.getCreateTime() == null) {
            book.setCreateTime(LocalDateTime.now());
        }
        return Optional.of(bookInfoRepository.save(book));
    }

    public boolean deleteBook(Long bookId) {
        if (!bookInfoRepository.existsById(bookId)) {
            return false;
        }
        bookInfoRepository.deleteById(bookId);
        return true;
    }

    private void applyRequest(BookInfo book, BookRequest request) {
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalStock(request.getTotalStock());
        book.setAvailableStock(request.getAvailableStock());
        book.setCoverUrl(request.getCoverUrl());
        book.setCreateTime(request.getCreateTime());
    }
}
