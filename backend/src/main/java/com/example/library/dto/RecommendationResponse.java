package com.example.library.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private Long recommendId;
    private Long bookId;
    private String title;
    private String author;
    private String coverUrl;
    private String recommendReason;
    private BigDecimal score;
    private Integer recommendType;  // 0:个性化, 1:热门, 2:相关
}
