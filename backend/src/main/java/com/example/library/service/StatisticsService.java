package com.example.library.service;

import com.example.library.dto.BorrowStatisticsResponse;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.BookInfoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 统计分析服务
 */
@Service
public class StatisticsService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;

    public StatisticsService(
        BorrowRecordRepository borrowRecordRepository,
        BookInfoRepository bookInfoRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookInfoRepository = bookInfoRepository;
    }

    /**
     * 获取借阅统计数据
     */
    public BorrowStatisticsResponse getBorrowStatistics() {
        BorrowStatisticsResponse response = new BorrowStatisticsResponse();

        // 计算总借阅数
        long totalBorrowCount = borrowRecordRepository.count();
        response.setTotalBorrowCount(totalBorrowCount);

        // 计算日均借阅数（简化计算，实际应该看具体日期范围）
        long daysPassed = 30;  // 假设30天
        BigDecimal dailyAverage = new BigDecimal(totalBorrowCount).divide(new BigDecimal(daysPassed), 2, BigDecimal.ROUND_HALF_UP);
        response.setDailyAverageBorrow(dailyAverage);

        // 計算逾期率
        response.setOverdueRate(calculateOverdueRate());

        // 计算重复借阅率
        response.setRepeatBorrowRate(calculateRepeatBorrowRate());

        // 生成借阅走势数据（示例）
        response.setBorrowTrend(generateBorrowTrend());

        // 生成分类分布数据（示例）
        response.setCategoryDistribution(generateCategoryDistribution());

        // 生成活跃用户排行（示例）
        response.setActiveUsers(generateActiveUsers());

        // 生成逾期控制面板数据
        response.setOverdueUserCount(0L);
        response.setOverdueBookCount(0L);
        response.setOverdueRanking(new ArrayList<>());

        // 罚款统计
        response.setTotalFineAmount(BigDecimal.ZERO);
        response.setUnpaidFineCount(0);

        return response;
    }

    /**
     * 计算逾期率
     */
    private BigDecimal calculateOverdueRate() {
        // 这是一个简化的实现
        // 真实实现应该计算逾期借阅 / 总借阅
        return new BigDecimal("0.05");  // 5%
    }

    /**
     * 计算重复借阅率
     */
    private BigDecimal calculateRepeatBorrowRate() {
        // 这是一个简化的实现
        // 真实实现应该计算重复借阅的用户数 / 总用户数
        return new BigDecimal("0.15");  // 15%
    }

    /**
     * 生成借阅走势数据
     */
    private List<Map<String, Object>> generateBorrowTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();

        // 示例数据，真实应该从数据库按日期统计
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("date", "2024-02-" + String.format("%02d", i));
            data.put("count", (int) (Math.random() * 50) + 10);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 生成分类分布数据
     */
    private List<Map<String, Object>> generateCategoryDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();

        String[] categories = {"计算机", "文学", "历史", "科学", "艺术"};
        int[] counts = {150, 120, 100, 80, 60};

        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", categories[i]);
            data.put("value", counts[i]);
            distribution.add(data);
        }

        return distribution;
    }

    /**
     * 生成活跃用户排行
     */
    private List<Map<String, Object>> generateActiveUsers() {
        List<Map<String, Object>> activeUsers = new ArrayList<>();

        // 示例数据
        String[] usernames = {"user1", "user2", "user3", "user4", "user5"};
        int[] borrowCounts = {45, 38, 32, 28, 25};

        for (int i = 0; i < usernames.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", usernames[i]);
            data.put("borrowCount", borrowCounts[i]);
            activeUsers.add(data);
        }

        return activeUsers;
    }
}
