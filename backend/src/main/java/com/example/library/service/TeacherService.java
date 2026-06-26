package com.example.library.service;

import com.example.library.dto.BorrowRequest;
import com.example.library.dto.RecommendationResponse;
import com.example.library.entity.BookInfo;
import com.example.library.entity.BookRenewRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.RecommendationList;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BookRenewRequestRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.RecommendationListRepository;
import com.example.library.repository.SysUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * 教师服务
 */
@Service
public class TeacherService {

    private static final int BORROW_STATUS_BORROWING = 0;
    private static final int BORROW_STATUS_OVERDUE = 2;

    private final BorrowService borrowService;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BookRenewRequestRepository renewRequestRepository;
    private final RecommendationListRepository recommendationListRepository;
    private final SysUserRepository sysUserRepository;

    public TeacherService(
        BorrowService borrowService,
        BorrowRecordRepository borrowRecordRepository,
        BookInfoRepository bookInfoRepository,
        BookRenewRequestRepository renewRequestRepository,
        RecommendationListRepository recommendationListRepository,
        SysUserRepository sysUserRepository) {
        this.borrowService = borrowService;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.renewRequestRepository = renewRequestRepository;
        this.recommendationListRepository = recommendationListRepository;
        this.sysUserRepository = sysUserRepository;
    }

    /**
     * 批量借阅（教师专用）
     */
    public boolean batchBorrow(Long userId, List<Long> bookIds) {
        // 检查用户是否为教师（角色ID=1）
        Optional<com.example.library.entity.SysUser> userOpt = sysUserRepository.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != 1) {
            return false;
        }

        // 检查借阅数量是否超过限制
        if (bookIds.size() > 20) {
            return false;
        }

        // 批量借阅
        for (Long bookId : bookIds) {
            try {
                BorrowRequest request = new BorrowRequest();
                request.setUserId(userId);
                request.setBookId(bookId);
                borrowService.borrowBook(request);
            } catch (Exception e) {
                // 继续借下一本
                continue;
            }
        }

        return true;
    }

    /**
     * 获取推荐列表（教师）
     */
    public Page<RecommendationResponse> getRecommendations(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return recommendationListRepository.findByUserIdOrderByScoreDescCreateTimeDesc(userId, pageable)
            .map(this::toRecommendationResponse);
    }

    /**
     * 获取热门推荐
     */
    public List<RecommendationResponse> getTrendingRecommendations(Long userId) {
        List<RecommendationList> recommendations = recommendationListRepository.findByUserIdAndRecommendType(userId, 1);
        return recommendations.stream()
            .map(this::toRecommendationResponse)
            .collect(Collectors.toList());
    }

    /**
     * 获取相关推荐
     */
    public List<RecommendationResponse> getRelatedRecommendations(Long userId, Long bookId) {
        List<RecommendationList> recommendations = recommendationListRepository.findByUserIdAndRecommendType(userId, 2);
        return recommendations.stream()
            .map(this::toRecommendationResponse)
            .collect(Collectors.toList());
    }

    /**
     * 申请续借
     */
    public boolean requestRenewal(Long userId, Long recordId, Long borrowDays) {
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return false;
        }

        BorrowRecord record = recordOpt.get();

        if (!record.getUserId().equals(userId)) {
            return false;
        }

        if (record.getStatus() != BORROW_STATUS_BORROWING && record.getStatus() != BORROW_STATUS_OVERDUE) {
            return false;
        }

        if (record.getReturnDate() != null) {
            return false;
        }

        if (renewRequestRepository.findByRecordId(recordId).filter(req -> req.getStatus() == 0).isPresent()) {
            return false;
        }

        // 检查是否已续借过
        if (record.getIsRenew() == 1) {
            return false;
        }

        // 创建续借申请
        BookRenewRequest request = new BookRenewRequest();
        request.setRecordId(recordId);
        request.setUserId(userId);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(0);  // 待审批
        if (record.getDueDate() != null) {
            request.setNewDueDate(record.getDueDate().plusDays(borrowDays != null ? borrowDays : 14L));
        }

        renewRequestRepository.save(request);
        return true;
    }

    /**
     * 查看续借申请状态
     */
    public List<BookRenewRequest> getRenewalRequests(Long userId) {
        return renewRequestRepository.findByUserIdOrderByRequestDateDesc(userId);
    }

    /**
     * 审批续借申请（管理员）
     */
    public boolean approveRenewal(Long renewId, Long adminId) {
        Optional<BookRenewRequest> requestOpt = renewRequestRepository.findById(renewId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        BookRenewRequest renewRequest = requestOpt.get();
        if (renewRequest.getStatus() == null || renewRequest.getStatus() != 0) {
            return false;
        }
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(renewRequest.getRecordId());
        if (recordOpt.isEmpty()) {
            return false;
        }

        BorrowRecord record = recordOpt.get();

        // 更新续借申请状态
        renewRequest.setStatus(1);  // 已批准
        renewRequest.setAdminId(adminId);
        renewRequest.setReviewDate(LocalDateTime.now());
        renewRequest.setNewDueDate(record.getDueDate().plusDays(14));

        // 更新借阅记录
        record.setIsRenew(1);
        record.setDueDate(renewRequest.getNewDueDate());
        record.setRenewRequestId(renewId);

        renewRequestRepository.save(renewRequest);
        borrowRecordRepository.save(record);

        return true;
    }

    /**
     * 拒绝续借申请
     */
    public boolean rejectRenewal(Long renewId, String reason, Long adminId) {
        Optional<BookRenewRequest> requestOpt = renewRequestRepository.findById(renewId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        BookRenewRequest renewRequest = requestOpt.get();
        if (renewRequest.getStatus() == null || renewRequest.getStatus() != 0) {
            return false;
        }
        renewRequest.setStatus(2);  // 已拒绝
        renewRequest.setAdminId(adminId);
        renewRequest.setReviewDate(LocalDateTime.now());
        renewRequest.setReviewRemarks(reason);

        renewRequestRepository.save(renewRequest);
        return true;
    }

    private RecommendationResponse toRecommendationResponse(RecommendationList recommendation) {
        Optional<BookInfo> bookOpt = bookInfoRepository.findById(recommendation.getBookId());
        if (bookOpt.isEmpty()) {
            return null;
        }

        BookInfo book = bookOpt.get();
        return new RecommendationResponse(
            recommendation.getRecommendId(),
            recommendation.getBookId(),
            book.getTitle(),
            book.getAuthor(),
            book.getCoverUrl(),
            recommendation.getRecommendReason(),
            recommendation.getScore(),
            recommendation.getRecommendType()
        );
    }
}
