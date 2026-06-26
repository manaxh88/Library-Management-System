package com.example.library.repository;

import com.example.library.entity.BorrowReminder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowReminderRepository extends JpaRepository<BorrowReminder, Long> {

    List<BorrowReminder> findByUserIdOrderByIsReadAscCreateTimeDesc(Long userId);

    Optional<BorrowReminder> findFirstByUserIdAndRecordIdAndReminderTypeOrderByReminderIdDesc(
        Long userId,
        Long recordId,
        Integer reminderType);

    @Modifying
    @Query("update BorrowReminder r set r.isRead = 1, r.updateTime = CURRENT_TIMESTAMP where r.reminderId = :reminderId and r.userId = :userId")
    int markReadByIdAndUserId(@Param("reminderId") Long reminderId, @Param("userId") Long userId);

    @Modifying
    @Query("update BorrowReminder r set r.isRead = 1, r.updateTime = CURRENT_TIMESTAMP where r.userId = :userId and r.isRead = 0")
    int markAllReadByUserId(@Param("userId") Long userId);
}
