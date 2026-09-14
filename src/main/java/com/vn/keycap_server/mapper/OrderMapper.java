package com.vn.keycap_server.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vn.keycap_server.dto.response.order.OrderAdminResponse;
import org.springframework.stereotype.Component;

import com.vn.keycap_server.dto.response.order.OrderItemResponse;
import com.vn.keycap_server.dto.response.order.OrderResponse;
import com.vn.keycap_server.dto.response.order.OrderStatusHistoryResponse;
import com.vn.keycap_server.modal.Order;
import com.vn.keycap_server.modal.OrderItem;
import com.vn.keycap_server.modal.OrderStatusHistory;
import com.vn.keycap_server.modal.Product;
import com.vn.keycap_server.modal.ProductImage;
import com.vn.keycap_server.modal.ProductVariant;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final AddressMapper addressMapper;

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .address(order.getAddress() != null
                        ? addressMapper.toAddressResponse(order.getAddress())
                        : null)
                .statusHistory(mapStatusHistories(order.getStatusHistory()))
                .items(mapOrderItems(order.getItems()))
                .build();
    }

    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderAdminResponse toOrderAdminResponse(Order order) {
        return OrderAdminResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .address(order.getAddress().getFullAddress())
                .statusHistory(mapStatusHistories(order.getStatusHistory()))
                .items(mapOrderItems(order.getItems()))
                .build();
    }

    // OrderItem mapping
    private List<OrderItemResponse> mapOrderItems(List<OrderItem> items) {
        if (items == null)
            return Collections.emptyList();
        return items.stream().map(this::mapOrderItem).collect(Collectors.toList());
    }

    private OrderItemResponse mapOrderItem(OrderItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        // Lấy ảnh primary, fallback ảnh đầu tiên
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().stream()
                    .filter(ProductImage::getPrimary)
                    .map(ProductImage::getUrl)
                    .findFirst()
                    .orElse(product.getImages().get(0).getUrl());
        }

        // Map variant attributes
        List<OrderItemResponse.AttributeResponse> attributes = variant.getAttributes() != null
                ? variant.getAttributes().stream()
                        .map(attr -> OrderItemResponse.AttributeResponse.builder()
                                .name(attr.getName())
                                .value(attr.getValue())
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .variantId(variant.getId())
                .productName(product.getName())
                .productImage(imageUrl)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .attributes(attributes)
                .build();
    }

    // StatusHistory mapping
    private List<OrderStatusHistoryResponse> mapStatusHistories(
            List<OrderStatusHistory> histories) {
        if (histories == null)
            return Collections.emptyList();
        return histories.stream()
                .map(h -> OrderStatusHistoryResponse.builder()
                        .id(h.getId())
                        .orderId(h.getOrder().getId())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .note(h.getNote())
                        .createdAt(h.getCreatedAt())
                        .createdBy(h.getCreatedBy() != null
                                ? h.getCreatedBy().toString()
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

}
