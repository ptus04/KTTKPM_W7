package com.sba.food.service;

import com.sba.common.dto.FoodDto;
import com.sba.food.entity.Food;
import com.sba.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;

    public List<FoodDto> getFoods() {
        return foodRepository.findAll().stream().map(this::toDto).toList();
    }

    public FoodDto getFood(Long id) {
        return toDto(foodRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Food not found")));
    }

    public FoodDto create(FoodDto dto) {
        Food food = new Food();
        food.setName(dto.name());
        food.setPrice(dto.price());
        return toDto(foodRepository.save(food));
    }

    public FoodDto update(Long id, FoodDto dto) {
        Food food = foodRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Food not found"));
        food.setName(dto.name());
        food.setPrice(dto.price());
        return toDto(foodRepository.save(food));
    }

    public void delete(Long id) {
        foodRepository.deleteById(id);
    }

    private FoodDto toDto(Food food) {
        return new FoodDto(food.getId(), food.getName(), food.getPrice());
    }
}
