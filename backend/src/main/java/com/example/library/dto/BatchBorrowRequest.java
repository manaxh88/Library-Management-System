package com.example.library.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量借阅请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchBorrowRequest {
    private List<Long> bookIds;
    private String remark;
}
