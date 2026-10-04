package com.vn.keycap_server.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateReviewRequest {

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(min = 10, max = 1000, message = "Nội dung đánh giá phải từ 10 đến 1000 ký tự")
    private String content;

    @Size(max = 5, message = "Tối đa 5 hình ảnh hoặc video đính kèm")
    private List<String> imageUrls;
}
