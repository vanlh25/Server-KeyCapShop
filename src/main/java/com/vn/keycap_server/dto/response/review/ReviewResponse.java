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
public class ReviewResponse {
    private Long id;
    private UserReviewInfo user;
    private Integer rating;
    private String content;
    private LocalDate createdAt;
    private List<String> imageUrls;
    private ReplyResponse reply;
    private Boolean isHidden;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyResponse {
        private Long id;
        private String adminName;
        private String content;
        private LocalDate createdAt;
    }
}
