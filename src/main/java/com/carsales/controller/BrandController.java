package com.carsales.controller;

import com.carsales.dto.response.ApiResponse;
import com.carsales.entity.Brand;
import com.carsales.repository.BrandRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brands")
@Tag(name = "Brand Management", description = "Brand APIs")
public class BrandController {

    private final BrandRepository brandRepository;

    public BrandController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @GetMapping
    @Operation(summary = "Get all brands")
    public ResponseEntity<ApiResponse<List<Brand>>> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(brands));
    }
}
