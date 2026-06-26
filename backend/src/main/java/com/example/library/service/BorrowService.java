package com.example.library.service;

import com.example.library.dto.BorrowRequest;
import com.example.library.dto.BatchBorrowResultResponse;
import com.example.library.dto.BookStockCheckResponse;
import com.example.library.entity.BookInfo;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.SysUser;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.SysUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {

    private static final int STATUS_BORROWING = 0;
    private static final int STATUS_RETURNED = 1;
    private static final int STATUS_OVERDUE = 2;

    private final SysUserRepository sysUserRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowService(
            SysUserRepository sysUserRepository,
            BookInfoRepository bookInfoRepository,
            BorrowRecordRepository borrowRecordRepository) {
        this.sysUserRepository = sysUserRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public BorrowRecord borrowBook(BorrowRequest request) {
        SysUser user = sysUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        long currentCount = borrowRecordRepository.countByUserIdAndStatus(user.getUserId(), STATUS_BORROWING);
        if (user.getMaxLimit() != null && currentCount >= user.getMaxLimit()) {
            throw new IllegalStateException("Borrow limit exceeded");
        }

        BookInfo book = bookInfoRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalStateException("Book not found"));

        if (book.getAvailableStock() == null || book.getAvailableStock() <= 0) {
            throw new IllegalStateException("No stock available");
        }

        book.setAvailableStock(book.getAvailableStock() - 1);
        bookInfoRepository.save(book);

        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setUserId(user.getUserId());
        record.setBookId(book.getBookId());
        record.setBorrowDate(now);
        record.setDueDate(now.plusDays(30));
        record.setReturnDate(null);
        record.setStatus(STATUS_BORROWING);
        record.setIsRenew(0);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Borrow record not found"));

        if (record.getStatus() != STATUS_BORROWING && record.getStatus() != STATUS_OVERDUE) {
            throw new IllegalStateException("Invalid record status for return");
        }

        BookInfo book = bookInfoRepository.findById(record.getBookId())
                .orElseThrow(() -> new IllegalStateException("Book not found"));

        record.setReturnDate(LocalDateTime.now());
        record.setStatus(STATUS_RETURNED);

        book.setAvailableStock(book.getAvailableStock() + 1);
        bookInfoRepository.save(book);

        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord borrowBookByUsernameAndIsbn(String username, String isbn) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Username is required");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalStateException("ISBN is required");
        }

        SysUser user = sysUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        BookInfo book = bookInfoRepository.findFirstByIsbn(isbn.trim())
                .orElseThrow(() -> new IllegalStateException("Book not found"));

        BorrowRequest request = new BorrowRequest();
        request.setUserId(user.getUserId());
        request.setBookId(book.getBookId());
        return borrowBook(request);
    }

    @Transactional
    public BorrowRecord returnBookByUsernameAndIsbn(String username, String isbn) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Username is required");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalStateException("ISBN is required");
        }

        SysUser user = sysUserRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        BookInfo book = bookInfoRepository.findFirstByIsbn(isbn.trim())
                .orElseThrow(() -> new IllegalStateException("Book not found"));

        BorrowRecord record = borrowRecordRepository
                .findFirstByUserIdAndBookIdAndStatusInOrderByBorrowDateDesc(
                        user.getUserId(),
                        book.getBookId(),
                        List.of(STATUS_BORROWING, STATUS_OVERDUE))
                .orElseThrow(() -> new IllegalStateException("No active borrow record found"));

        return returnBook(record.getRecordId());
    }

    public Optional<BookStockCheckResponse> checkBookStockByIsbn(String rawIsbn) {
        if (rawIsbn == null || rawIsbn.trim().isEmpty()) {
            return Optional.empty();
        }
        String isbn = rawIsbn.trim();
        return bookInfoRepository.findFirstByIsbn(isbn)
                .map(book -> new BookStockCheckResponse(
                        book.getBookId(),
                        book.getIsbn(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getTotalStock(),
                        book.getAvailableStock(),
                        book.getAvailableStock() != null && book.getAvailableStock() > 0));
    }

    @Transactional
    public BatchBorrowResultResponse batchBorrowByIsbns(Long userId, List<String> rawIsbns) {
        if (userId == null) {
            throw new IllegalStateException("User ID is required");
        }
        if (rawIsbns == null || rawIsbns.isEmpty()) {
            throw new IllegalStateException("ISBN list is required");
        }

        List<String> normalizedIsbns = rawIsbns.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .map(String::trim)
                .toList();

        if (normalizedIsbns.isEmpty()) {
            throw new IllegalStateException("ISBN list is required");
        }
        if (normalizedIsbns.size() > 20) {
            throw new IllegalStateException("一次最多借阅20本");
        }

        List<BatchBorrowResultResponse.Item> items = normalizedIsbns.stream()
                .map(isbn -> {
                    Optional<BookInfo> bookOpt = bookInfoRepository.findFirstByIsbn(isbn);
                    if (bookOpt.isEmpty()) {
                        return new BatchBorrowResultResponse.Item(isbn, null, null, "FAILED", "图书不存在", null, null);
                    }

                    BookInfo book = bookOpt.get();
                    try {
                        BorrowRequest request = new BorrowRequest();
                        request.setUserId(userId);
                        request.setBookId(book.getBookId());
                        BorrowRecord record = borrowBook(request);
                        return new BatchBorrowResultResponse.Item(
                                isbn,
                                book.getBookId(),
                                book.getTitle(),
                                "SUCCESS",
                                "借阅成功",
                                record.getRecordId(),
                                record.getDueDate());
                    } catch (Exception ex) {
                        return new BatchBorrowResultResponse.Item(
                                isbn,
                                book.getBookId(),
                                book.getTitle(),
                                "FAILED",
                                ex.getMessage(),
                                null,
                                null);
                    }
                })
                .toList();

        int successCount = (int) items.stream().filter(item -> "SUCCESS".equals(item.getStatus())).count();
        int failedCount = items.size() - successCount;

        return new BatchBorrowResultResponse(
                "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                LocalDateTime.now(),
                userId,
                items.size(),
                successCount,
                failedCount,
                items);
    }
}
