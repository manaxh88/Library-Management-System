package com.example.library.repository;

import com.example.library.entity.BorrowRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    long countByUserIdAndStatus(Long userId, Integer status);

    long countByUserId(Long userId);

    List<BorrowRecord> findByUserIdAndReturnDateIsNull(Long userId);

    List<BorrowRecord> findByUserId(Long userId);

    List<BorrowRecord> findByUserIdAndStatusIn(Long userId, List<Integer> statuses);

    long countByBorrowDateBetween(LocalDateTime start, LocalDateTime end);

    List<BorrowRecord> findTop5ByOrderByBorrowDateDesc();

    @Query(value = """
        select br.book_id, count(*) as borrow_count
        from borrow_record br
        group by br.book_id
        order by borrow_count desc
        limit 6
        """, nativeQuery = true)
    List<Object[]> findTopBorrowedBookStats();

        Optional<BorrowRecord> findFirstByUserIdAndBookIdAndStatusInOrderByBorrowDateDesc(
            Long userId,
            Long bookId,
            List<Integer> statuses);

    Page<BorrowRecord> findByUserIdAndReturnDateIsNotNullOrderByBorrowDateDesc(Long userId, Pageable pageable);

    long countByDueDateBeforeAndReturnDateIsNull(java.time.LocalDateTime now);

    long countDistinctUserIdByDueDateBeforeAndReturnDateIsNull(java.time.LocalDateTime now);

    @Query("select coalesce(sum(br.fine), 0) from BorrowRecord br where br.fine is not null and br.fine > 0")
    BigDecimal sumPositiveFine();
}
