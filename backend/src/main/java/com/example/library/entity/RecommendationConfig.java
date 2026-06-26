package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 推荐参数配置表
 * cold_start_strategy: TRENDING/CATEGORY/RANDOM
 */
@Data
@Entity
@Table(name = "recommendation_config")
public class RecommendationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "collaborative_weight", precision = 3, scale = 2)
    private BigDecimal collaborativeWeight;

    @Column(name = "personalized_weight", precision = 3, scale = 2)
    private BigDecimal personalizedWeight;

    @Column(name = "trending_weight", precision = 3, scale = 2)
    private BigDecimal trendingWeight;

    @Column(name = "min_similarity", precision = 3, scale = 2)
    private BigDecimal minSimilarity;

    @Column(name = "cold_start_strategy", length = 50)
    private String coldStartStrategy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "updated_by")
    private Long updatedBy;
}
