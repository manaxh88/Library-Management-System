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
 * 推荐列表表
 * recommend_type: 0:个性化, 1:热门, 2:相关
 */
@Data
@Entity
@Table(name = "recommendation_list")
public class RecommendationList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    private Long recommendId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "recommend_reason", length = 100)
    private String recommendReason;

    @Column(name = "score", precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "recommend_type", nullable = false)
    private Integer recommendType;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}
