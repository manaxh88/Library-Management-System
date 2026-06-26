package com.example.library.service;

import com.example.library.entity.RecommendationList;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.RecommendationListRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * 推荐服务
 * 包含协同过滤、个性化推荐等算法逻辑
 */
@Service
public class RecommendationService {

    private final RecommendationListRepository recommendationListRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public RecommendationService(
        RecommendationListRepository recommendationListRepository,
        BorrowRecordRepository borrowRecordRepository) {
        this.recommendationListRepository = recommendationListRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    /**
     * 生成个性化推荐
     * 基于用户借阅历史和相似用户的行为
     */
    public void generatePersonalizedRecommendations(Long userId) {
        // 这是一个简化的实现
        // 真实环境中应该调用python微服务进行复杂的协同过滤算法

        // 示例：为用户userId生成5条个性化推荐
        for (int i = 1; i <= 5; i++) {
            RecommendationList rec = new RecommendationList();
            rec.setUserId(userId);
            rec.setBookId((long) (i + 100));  // 这只是示例ID
            rec.setRecommendType(0);  // 个性化推荐
            rec.setRecommendReason("基于您的阅读兴趣推荐");
            rec.setScore(new BigDecimal(0.85));
            rec.setCreateTime(LocalDateTime.now());
            recommendationListRepository.save(rec);
        }
    }

    /**
     * 生成热门推荐
     * 基于最近一个月的借阅排行
     */
    public void generateTrendingRecommendations(Long userId) {
        // 获取本月热门图书并为用户生成推荐
        // 这是一个简化的实现

        for (int i = 1; i <= 5; i++) {
            RecommendationList rec = new RecommendationList();
            rec.setUserId(userId);
            rec.setBookId((long) (i + 200));  // 这只是示例ID
            rec.setRecommendType(1);  // 热门推荐
            rec.setRecommendReason("本月热门图书");
            rec.setScore(new BigDecimal(0.90));
            rec.setCreateTime(LocalDateTime.now());
            recommendationListRepository.save(rec);
        }
    }

    /**
     * 生成相关推荐
     * 基于分类相同和内容相似性
     */
    public void generateRelatedRecommendations(Long userId, Long bookId) {
        // 找出与bookId相关的其他图书并为用户推荐
        // 这是一个简化的实现

        for (int i = 1; i <= 5; i++) {
            RecommendationList rec = new RecommendationList();
            rec.setUserId(userId);
            rec.setBookId(bookId + i);
            rec.setRecommendType(2);  // 相关推荐
            rec.setRecommendReason("与您浏览的图书相关");
            rec.setScore(new BigDecimal(0.80));
            rec.setCreateTime(LocalDateTime.now());
            recommendationListRepository.save(rec);
        }
    }

    /**
     * 清除用户的旧推荐，重新计算
     */
    public void recalculateRecommendations(Long userId) {
        // 清除旧推荐
        recommendationListRepository.deleteByUserId(userId);

        // 生成新推荐
        generatePersonalizedRecommendations(userId);
        generateTrendingRecommendations(userId);
    }

    /**
     * 协同过滤算法（简化版）
     * 找出与用户相似的用户，推荐他们喜欢的书
     */
    private double calculateUserSimilarity(Long userId1, Long userId2) {
        // 这是一个占位符，真实实现应该计算用户之间的相似度
        // 可以基于是协同过滤（用户-物品矩阵）或基于内容的相似度
        return 0.85;
    }

    /**
     * 计算推荐评分
     */
    private BigDecimal calculateRecommendationScore(
        BigDecimal collaborativeWeight,
        BigDecimal personalizedWeight,
        BigDecimal trendingWeight,
        double collaborativeScore,
        double personalizedScore,
        double trendingScore) {

        double totalScore = collaborativeScore * collaborativeWeight.doubleValue()
            + personalizedScore * personalizedWeight.doubleValue()
            + trendingScore * trendingWeight.doubleValue();

        return new BigDecimal(String.format("%.4f", totalScore));
    }
}
