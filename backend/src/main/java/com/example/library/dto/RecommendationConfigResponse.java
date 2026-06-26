package com.example.library.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐参数配置响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationConfigResponse {
    private Long configId;
    private BigDecimal collaborativeWeight;
    private BigDecimal personalizedWeight;
    private BigDecimal trendingWeight;
    private BigDecimal minSimilarity;
    private String coldStartStrategy;
}
