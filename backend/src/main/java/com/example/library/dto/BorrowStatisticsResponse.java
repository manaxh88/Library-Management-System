package com.example.library.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借阅统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowStatisticsResponse {
    // 关键指标
    private Long totalBorrowCount;           // 总借阅数
    private BigDecimal dailyAverageBorrow;   // 日均借阅数
    private BigDecimal overdueRate;          // 逾期率
    private BigDecimal repeatBorrowRate;     // 重复借阅率

    // 借阅走势 (按日期)
    private List<Map<String, Object>> borrowTrend;

    // 热门分类 (饼图数据)
    private List<Map<String, Object>> categoryDistribution;

    // 活跃用户排行
    private List<Map<String, Object>> activeUsers;

    // 逾期控制面板
    private Long overdueUserCount;           // 逾期用户数
    private Long overdueBookCount;           // 逾期图书数
    private List<Map<String, Object>> overdueRanking;

    // 罚款统计
    private BigDecimal totalFineAmount;      // 总罚款金额
    private Integer unpaidFineCount;         // 未支付罚款数
}
