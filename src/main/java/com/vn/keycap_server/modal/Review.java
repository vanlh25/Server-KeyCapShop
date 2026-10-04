package com.vn.keycap_server.modal;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Review là Entity đại diện cho bảng 'reviews' trong database.
 * Lưu trữ đánh giá của người dùng về một sản phẩm cụ thể.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
@ToString(exclude = { "product", "user" }) // Tránh lỗi vòng lặp
public class Review extends AbstractEntity {

    // Số sao đánh giá (từ 1 đến 5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    // Nội dung đánh giá
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Danh sách các link ảnh đính kèm của đánh giá
    // JPA sẽ tự động tạo một bảng phụ tên là 'review_images' để lưu trữ danh sách
    // này
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_images", // Tên bảng phụ
            joinColumns = @JoinColumn(name = "review_id") // Khoá ngoại liên kết về bảng reviews
    )
    @Column(name = "image_url") // Tên cột lưu link ảnh
    private List<String> imageUrls;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    // Liên kết với sản phẩm được đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Liên kết với người dùng đã viết đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Trạng thái ẩn/hiện của đánh giá (admin/staff quản lý)
    @Builder.Default
    @Column(name = "is_hidden", nullable = false, columnDefinition = "boolean default false")
    private Boolean isHidden = false;

    // Phản hồi của admin/staff
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ReviewReply reply;
}
