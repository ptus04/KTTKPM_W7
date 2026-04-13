package com.sba.food.controller;

import com.sba.common.dto.FoodDto;
import com.sba.food.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/foods")
    public ResponseEntity<List<FoodDto>> getFoods() {
        return ResponseEntity.ok(foodService.getFoods());
    }

    @GetMapping("/foods/{id}")
    public ResponseEntity<FoodDto> getFood(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFood(id));
    }

    @PostMapping("/foods")
    public ResponseEntity<FoodDto> create(@Valid @RequestBody FoodDto dto) {
        return ResponseEntity.ok(foodService.create(dto));
    }

    @PutMapping("/foods/{id}")
    public ResponseEntity<FoodDto> update(@PathVariable Long id, @Valid @RequestBody FoodDto dto) {
        return ResponseEntity.ok(foodService.update(id, dto));
    }

    @DeleteMapping("/foods/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
