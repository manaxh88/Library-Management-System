package com.example.library.repository;

import com.example.library.entity.RecommendationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationConfigRepository extends JpaRepository<RecommendationConfig, Long> {
}
