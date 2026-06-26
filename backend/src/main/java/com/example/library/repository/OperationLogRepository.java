package com.example.library.repository;

import com.example.library.entity.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<OperationLog> findByOperationTypeOrderByCreateTimeDesc(String operationType, Pageable pageable);

    List<OperationLog> findByUserIdOrderByCreateTimeDesc(Long userId);
}
