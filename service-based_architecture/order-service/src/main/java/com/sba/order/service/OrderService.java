package com.sba.order.service;

import com.sba.common.dto.FoodDto;
import com.sba.common.dto.OrderDto;
import com.sba.common.dto.UpdateOrderStatusRequest;
import com.sba.common.enums.OrderStatus;
import com.sba.order.client.FoodClient;
import com.sba.order.client.UserClient;
import com.sba.order.dto.CreateOrderRequest;
import com.sba.order.entity.FoodOrder;
import com.sba.order.repository.FoodOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final FoodOrderRepository foodOrderRepository;
    private final UserClient userClient;
    private final FoodClient foodClient;

    public OrderDto createOrder(CreateOrderRequest request, String bearerToken) {
        if (!userClient.userExists(request.userId(), bearerToken)) {
            throw new IllegalArgumentException("User not found");
        }

        double total = request.items().stream()
                .mapToDouble(item -> {
                    FoodDto food = foodClient.getFoodById(item.foodId(), bearerToken);
                    if (food == null) {
                        throw new IllegalArgumentException("Food not found: " + item.foodId());
                    }
                    return food.price() * item.quantity();
                }).sum();

        FoodOrder order = new FoodOrder();
        order.setUserId(request.userId());
        order.setTotal(total);
        order.setStatus(OrderStatus.PENDING);
        FoodOrder saved = foodOrderRepository.save(order);
        return toDto(saved);
    }

    public List<OrderDto> getOrders() {
        return foodOrderRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<OrderDto> getOrdersByUserId(Long userId) {
        return foodOrderRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    public OrderDto updateStatus(Long id, UpdateOrderStatusRequest request) {
        FoodOrder order = foodOrderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(request.status());
        FoodOrder saved = foodOrderRepository.save(order);
        return toDto(saved);
    }

    private OrderDto toDto(FoodOrder order) {
        return new OrderDto(order.getId(), order.getUserId(), order.getTotal(), order.getStatus());
    }
}
