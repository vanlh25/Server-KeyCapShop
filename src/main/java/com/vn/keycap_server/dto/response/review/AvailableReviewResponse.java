package com.vn.keycap_server.dto.response.review;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableReviewResponse {
    private Long id;
    private Long productId;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Boolean canEdit;
    private Long remainingDays;
}
