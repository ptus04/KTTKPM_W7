package com.sba.order.repository;

import com.sba.order.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByUserId(Long userId);
}
