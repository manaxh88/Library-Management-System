package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.BookRequest;
import com.example.library.entity.BookInfo;
import com.example.library.service.BookService;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ApiResponse<Page<BookInfo>> listBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(bookService.listBooks(keyword, page, size));
    }

    @PostMapping("/books")
    public ResponseEntity<ApiResponse<BookInfo>> createBook(@RequestBody BookRequest request) {
        BookInfo created = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookInfo>> updateBook(
            @PathVariable("id") Long id,
            @RequestBody BookRequest request) {
        Optional<BookInfo> updated = bookService.updateBook(id, request);
        if (updated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(updated.get()));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable("id") Long id) {
        boolean deleted = bookService.deleteBook(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
