package com.example.library.repository;

import com.example.library.entity.RecommendationList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationListRepository extends JpaRepository<RecommendationList, Long> {

    Page<RecommendationList> findByUserIdOrderByScoreDescCreateTimeDesc(Long userId, Pageable pageable);

    List<RecommendationList> findByUserIdAndRecommendType(Long userId, Integer recommendType);

    long countByUserId(Long userId);

    void deleteByUserIdAndRecommendType(Long userId, Integer recommendType);

    void deleteByUserId(Long userId);
}
