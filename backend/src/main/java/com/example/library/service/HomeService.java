package com.example.library.service;

import com.example.library.dto.HomeOverviewResponse;
import com.example.library.entity.BookInfo;
import com.example.library.entity.BorrowRecord;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.SysUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;
    private final SysUserRepository sysUserRepository;

    public HomeService(
        BorrowRecordRepository borrowRecordRepository,
        BookInfoRepository bookInfoRepository,
        SysUserRepository sysUserRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public HomeOverviewResponse getOverview() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long todayBorrowCount = borrowRecordRepository.countByBorrowDateBetween(startOfDay, endOfDay);
        long availableBookCount = Math.max(0L, valueOrZero(bookInfoRepository.sumAvailableStock()));
        long activeReaderCount = sysUserRepository.countActiveReaders();

        List<HomeOverviewResponse.ActivityItem> activities = buildRecentActivities();
        List<HomeOverviewResponse.BookItem> hotBooks = buildHotBooks();
        List<HomeOverviewResponse.BookItem> newBooks = buildNewBooks();

        return HomeOverviewResponse.builder()
            .todayBorrowCount(todayBorrowCount)
            .availableBookCount(availableBookCount)
            .activeReaderCount(activeReaderCount)
            .recentActivities(activities)
            .hotBooks(hotBooks)
            .newBooks(newBooks)
            .build();
    }

    private List<HomeOverviewResponse.BookItem> buildHotBooks() {
        List<HomeOverviewResponse.BookItem> list = new ArrayList<>();
        List<Object[]> stats = borrowRecordRepository.findTopBorrowedBookStats();

        for (Object[] row : stats) {
            if (row == null || row.length < 2) {
                continue;
            }

            Long bookId = toLong(row[0]);
            Long borrowCount = toLong(row[1]);
            if (bookId == null) {
                continue;
            }

            BookInfo book = bookInfoRepository.findById(bookId).orElse(null);
            if (book == null) {
                continue;
            }

            list.add(HomeOverviewResponse.BookItem.builder()
                .bookId(bookId)
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .location(book.getLocation())
                .totalStock(book.getTotalStock())
                .availableStock(book.getAvailableStock() == null ? 0 : book.getAvailableStock())
                .coverUrl(book.getCoverUrl())
                .publisher(book.getPublisher())
                .rating(book.getRating())
                .description(book.getDescription())
                .borrowCount(borrowCount == null ? 0L : borrowCount)
                .build());
        }

        return list;
    }

    private List<HomeOverviewResponse.BookItem> buildNewBooks() {
        List<HomeOverviewResponse.BookItem> list = new ArrayList<>();
        List<BookInfo> books = bookInfoRepository.findTop5ByOrderByCreateTimeDesc();

        for (BookInfo book : books) {
            if (book == null) {
                continue;
            }
            list.add(HomeOverviewResponse.BookItem.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .location(book.getLocation())
                .totalStock(book.getTotalStock())
                .availableStock(book.getAvailableStock() == null ? 0 : book.getAvailableStock())
                .coverUrl(book.getCoverUrl())
                .publisher(book.getPublisher())
                .rating(book.getRating())
                .description(book.getDescription())
                .borrowCount(0L)
                .build());
        }

        return list;
    }

    private List<HomeOverviewResponse.ActivityItem> buildRecentActivities() {
        List<HomeOverviewResponse.ActivityItem> activities = new ArrayList<>();

        List<BorrowRecord> borrowRecords = borrowRecordRepository.findTop5ByOrderByBorrowDateDesc();
        for (BorrowRecord record : borrowRecords) {
            if (record == null || record.getBorrowDate() == null) {
                continue;
            }

            // String userName = sysUserRepository.findById(record.getUserId())
            //     .map(user -> user.getRealName() != null && !user.getRealName().isBlank() ? user.getRealName() : user.getUsername())
            //     .orElse("读者");

            String userName = sysUserRepository.findById(record.getUserId())
                .map(user -> user.getRealName() != null && !user.getRealName().isBlank() ? user.getRealName() : user.getUsername())
                .orElse("读者");
            String maskedName = maskName(userName);

            String bookTitle = bookInfoRepository.findById(record.getBookId())
                .map(BookInfo::getTitle)
                .orElse("未知图书");
            

            activities.add(HomeOverviewResponse.ActivityItem.builder()
                .type("BORROW")
                .title("借阅成功")
                // .content("读者 " + userName + " 借阅《" + bookTitle + "》")
                .content("读者 " + maskedName + " 借阅《" + bookTitle + "》")
                .time(record.getBorrowDate())
                .build());
        }

        List<BookInfo> latestBooks = bookInfoRepository.findTop5ByOrderByCreateTimeDesc();
        for (BookInfo book : latestBooks) {
            if (book == null || book.getCreateTime() == null) {
                continue;
            }
            int stock = book.getAvailableStock() == null ? 0 : book.getAvailableStock();
            activities.add(HomeOverviewResponse.ActivityItem.builder()
                .type("BOOK")
                .title("新增图书")
                .content("《" + book.getTitle() + "》已入库（可借 " + stock + "）")
                .time(book.getCreateTime())
                .build());
        }

        activities.sort(Comparator.comparing(HomeOverviewResponse.ActivityItem::getTime).reversed());
        if (activities.size() > 6) {
            return new ArrayList<>(activities.subList(0, 6));
        }
        return activities;
    }
    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "*";
        }

        String trimmed = name.trim();
        if (trimmed.length() == 1) {
            return "*";
        }

        String[] compoundSurnames = {
            "欧阳", "司马", "上官", "诸葛", "东方", "独孤", "南宫", "夏侯",
            "皇甫", "尉迟", "公孙", "慕容", "宇文", "司徒", "司空", "长孙"
        };

        for (String surname : compoundSurnames) {
            if (trimmed.startsWith(surname)) {
                int hiddenLen = Math.max(1, trimmed.length() - surname.length());
                return surname + "*".repeat(hiddenLen);
            }
        }

        return trimmed.substring(0, 1) + "*".repeat(Math.max(1, trimmed.length() - 1));
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
