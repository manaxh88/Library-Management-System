package com.example.library.repository;

import com.example.library.entity.BookInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {
    Page<BookInfo> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Optional<BookInfo> findFirstByIsbn(String isbn);

    List<BookInfo> findTop5ByOrderByCreateTimeDesc();

    @Query("select coalesce(sum(b.availableStock), 0) from BookInfo b")
    Long sumAvailableStock();
}
