package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.BatchBorrowByIsbnRequest;
import com.example.library.dto.BatchBorrowResultResponse;
import com.example.library.dto.BookStockCheckResponse;
import com.example.library.dto.BorrowByIdentityRequest;
import com.example.library.dto.BorrowRecordResponse;
import com.example.library.dto.BorrowRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.service.BorrowService;
import com.example.library.service.StudentService;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.BookInfoRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BorrowController {

    private final BorrowService borrowService;
    private final StudentService studentService;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;

    public BorrowController(
            BorrowService borrowService,
            StudentService studentService,
            BorrowRecordRepository borrowRecordRepository,
            BookInfoRepository bookInfoRepository) {
        this.borrowService = borrowService;
        this.studentService = studentService;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookInfoRepository = bookInfoRepository;
    }

    @PostMapping("/borrow")
    public ResponseEntity<ApiResponse<BorrowRecord>> borrowBook(@RequestBody BorrowRequest request) {
        try {
            BorrowRecord record = borrowService.borrowBook(request);
            return ResponseEntity.ok(ApiResponse.ok(record));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/borrow/by-identity")
    public ResponseEntity<ApiResponse<BorrowRecord>> borrowBookByIdentity(@RequestBody BorrowByIdentityRequest request) {
        try {
            BorrowRecord record = borrowService.borrowBookByUsernameAndIsbn(request.getUsername(), request.getIsbn());
            return ResponseEntity.ok(ApiResponse.ok(record));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PutMapping("/borrow/{recordId}/return")
    public ResponseEntity<ApiResponse<BorrowRecord>> returnBook(@PathVariable Long recordId) {
        try {
            BorrowRecord record = borrowService.returnBook(recordId);
            return ResponseEntity.ok(ApiResponse.ok(record));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/borrow/by-identity/return")
    public ResponseEntity<ApiResponse<BorrowRecord>> returnBookByIdentity(@RequestBody BorrowByIdentityRequest request) {
        try {
            BorrowRecord record = borrowService.returnBookByUsernameAndIsbn(request.getUsername(), request.getIsbn());
            return ResponseEntity.ok(ApiResponse.ok(record));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/borrow/books/by-isbn")
    public ResponseEntity<ApiResponse<BookStockCheckResponse>> checkBookStockByIsbn(@RequestParam String isbn) {
        Optional<BookStockCheckResponse> response = borrowService.checkBookStockByIsbn(isbn);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("图书不存在"));
        }
        return ResponseEntity.ok(ApiResponse.ok(response.get()));
    }

    @PostMapping("/borrow/batch")
    public ResponseEntity<ApiResponse<BatchBorrowResultResponse>> batchBorrowByIsbn(
            @RequestBody BatchBorrowByIsbnRequest request) {
        try {
            BatchBorrowResultResponse response = borrowService.batchBorrowByIsbns(request.getUserId(), request.getIsbns());
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @GetMapping("/borrow/records")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getBorrowRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        try {
            List<BorrowRecord> records;
            
            if (status != null && !status.isEmpty()) {
                String[] statusArray = status.split(",");
                List<Integer> statusList = java.util.Arrays.stream(statusArray)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                records = borrowRecordRepository.findByUserIdAndStatusIn(userId, statusList);
            } else {
                records = borrowRecordRepository.findByUserId(userId);
            }

            List<BorrowRecordResponse> responses = records.stream()
                    .map(record -> {
                        Optional<com.example.library.entity.BookInfo> bookOpt = 
                            bookInfoRepository.findById(record.getBookId());
                        String bookTitle = bookOpt.map(com.example.library.entity.BookInfo::getTitle)
                                .orElse("未知图书");
                        String author = bookOpt.map(com.example.library.entity.BookInfo::getAuthor)
                                .orElse("");
                        return new BorrowRecordResponse(
                            record.getRecordId(),
                            record.getUserId(),
                            record.getBookId(),
                            bookTitle,
                            author,
                            record.getBorrowDate(),
                            record.getDueDate(),
                            record.getReturnDate(),
                            record.getStatus(),
                            record.getIsRenew(),
                            record.getFine(),
                            record.getRenewRequestId()
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }
}
