package com.vn.keycap_server.service.customer;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.vn.keycap_server.dto.request.customer.AdminCustomerListRequest;
import com.vn.keycap_server.dto.request.customer.AdminUpdateCustomerRequest;
import com.vn.keycap_server.dto.response.customer.AdminCustomerResponse;
import com.vn.keycap_server.exception.ResourceNotFoundException;
import com.vn.keycap_server.modal.User;
import com.vn.keycap_server.repository.OrderRepository;
import com.vn.keycap_server.repository.UserRepository;
import com.vn.keycap_server.utils.ERole;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý nghiệp vụ quản lý khách hàng cho admin.
 * Chỉ thao tác với user có role USER, không bao giờ lộ ADMIN hoặc STAFF.
 */
@Service
@RequiredArgsConstructor
public class AdminCustomerService implements IAdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCustomerResponse> getCustomers(AdminCustomerListRequest request) {
        String search = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getLimit(),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<User> userPage = StringUtils.hasText(search)
                ? userRepository.searchByRoleAndKeyword(ERole.USER, search, pageable)
                : userRepository.findByRole(ERole.USER, pageable);

        return userPage.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCustomerResponse getCustomerById(Long customerId) {
        User customer = getUserEntity(customerId);
        return toResponse(customer);
    }

    @Override
    @Transactional
    public AdminCustomerResponse updateCustomer(Long customerId, AdminUpdateCustomerRequest request) {
        User customer = getUserEntity(customerId);

        if (request.getLocked() != null) {
            customer.setLocked(request.getLocked());
        }

        userRepository.save(customer);
        return toResponse(customer);
    }

    /**
     * Load user đảm bảo chỉ lấy USER, không nhầm ADMIN/STAFF.
     */
    private User getUserEntity(Long customerId) {
        return userRepository.findByIdAndRole(customerId, ERole.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
    }

    /**
     * Map User entity sang AdminCustomerResponse kèm thống kê đơn hàng.
     * ponytail: totalOrders đếm tất cả trạng thái, totalSpent chỉ tính SUCCESS.
     * Upgrade khi cần batch-load để tránh N+1 trên danh sách lớn.
     */
    private AdminCustomerResponse toResponse(User user) {
        long totalOrders = orderRepository.countByUserId(user.getId());
        BigDecimal totalSpent = orderRepository.sumTotalSpentByUserId(user.getId());

        String avatarUrl = user.getAvatarMedia() != null ? user.getAvatarMedia().getSecureUrl() : null;

        return AdminCustomerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(avatarUrl)
                .locked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .build();
    }
}
