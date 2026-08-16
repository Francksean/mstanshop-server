package com.mstan.shop.order.mapper;

import com.mstan.shop.order.dto.AddressResponse;
import com.mstan.shop.order.dto.OrderCustomerResponse;
import com.mstan.shop.order.dto.OrderItemResponse;
import com.mstan.shop.order.dto.OrderResponse;
import com.mstan.shop.order.dto.OrderSummaryResponse;
import com.mstan.shop.order.entity.Address;
import com.mstan.shop.order.entity.Order;
import com.mstan.shop.order.entity.OrderItem;
import com.mstan.shop.payment.entity.Payment;
import com.mstan.shop.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "customer", expression = "java(order.getUser() != null ? toCustomerResponse(order.getUser()) : com.mstan.shop.order.dto.OrderCustomerResponse.guest(order.getGuestEmail()))")
    @Mapping(target = "paymentMethod", expression = "java(payment != null ? payment.getMethod() : null)")
    @Mapping(target = "paymentReference", expression = "java(payment != null ? payment.getProviderReference() : null)")
    @Mapping(target = "paymentStatus", expression = "java(payment != null ? payment.getStatus() : null)")
    OrderResponse toResponse(Order order, Payment payment);

    @Mapping(target = "customer", expression = "java(order.getUser() != null ? toCustomerResponse(order.getUser()) : com.mstan.shop.order.dto.OrderCustomerResponse.guest(order.getGuestEmail()))")
    OrderSummaryResponse toSummaryResponse(Order order);

    AddressResponse toAddressResponse(Address address);

    OrderCustomerResponse toCustomerResponse(User user);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", expression = "java(item.getVariant() != null ? item.getVariant().getId() : null)")
    @Mapping(target = "lineTotal", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);
}
