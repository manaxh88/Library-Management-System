package com.example.library.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BorrowRecordResponse {
    private Long borrowRecordId;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String author;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer status;
    private Integer isRenew;
    private BigDecimal fine;
    private Long renewRequestId;

    public BorrowRecordResponse() {}

    public BorrowRecordResponse(Long borrowRecordId, Long userId, Long bookId, String bookTitle, String author,
                              LocalDateTime borrowDate, LocalDateTime dueDate, LocalDateTime returnDate,
                              Integer status, Integer isRenew, BigDecimal fine, Long renewRequestId) {
        this.borrowRecordId = borrowRecordId;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.author = author;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.isRenew = isRenew;
        this.fine = fine;
        this.renewRequestId = renewRequestId;
    }

    // Getters and Setters
    public Long getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(Long borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsRenew() {
        return isRenew;
    }

    public void setIsRenew(Integer isRenew) {
        this.isRenew = isRenew;
    }

    public BigDecimal getFine() {
        return fine;
    }

    public void setFine(BigDecimal fine) {
        this.fine = fine;
    }

    public Long getRenewRequestId() {
        return renewRequestId;
    }

    public void setRenewRequestId(Long renewRequestId) {
        this.renewRequestId = renewRequestId;
    }
}
