package com.example.library.repository;

import com.example.library.entity.BookRenewRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRenewRequestRepository extends JpaRepository<BookRenewRequest, Long> {

    Optional<BookRenewRequest> findByRecordId(Long recordId);

    List<BookRenewRequest> findByUserIdOrderByRequestDateDesc(Long userId);

    List<BookRenewRequest> findByStatusOrderByRequestDateDesc(Integer status);
}
