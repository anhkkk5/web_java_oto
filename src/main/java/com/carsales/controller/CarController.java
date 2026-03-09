package com.carsales.controller;

import com.carsales.dto.request.CarRequest;
import com.carsales.dto.response.ApiResponse;
import com.carsales.dto.response.CarResponse;
import com.carsales.dto.response.PageResponse;
import com.carsales.service.CarService;
import com.carsales.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cars")
@Tag(name = "Car Management", description = "Car Management APIs")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    @Operation(summary = "Search cars with filters and pagination")
    public ResponseEntity<ApiResponse<PageResponse<CarResponse>>> searchCars(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer modelYear,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        
        PageResponse<CarResponse> response = carService.searchCars(name, brandId, categoryId,
                minPrice, maxPrice, modelYear, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    public ResponseEntity<ApiResponse<CarResponse>> getCarById(@PathVariable Long id) {
        CarResponse response = carService.getCarById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create new car (ADMIN only)")
    public ResponseEntity<ApiResponse<CarResponse>> createCar(@Valid @RequestBody CarRequest request) {
        CarResponse response = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Car created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update car (ADMIN only)")
    public ResponseEntity<ApiResponse<CarResponse>> updateCar(@PathVariable Long id,
                                                              @Valid @RequestBody CarRequest request) {
        CarResponse response = carService.updateCar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Car updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete car (ADMIN only)")
    public ResponseEntity<ApiResponse<String>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok(ApiResponse.success("Car deleted successfully", null));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Upload car image (ADMIN only)")
    public ResponseEntity<ApiResponse<String>> uploadCarImage(@PathVariable Long id,
                                                              @RequestParam("file") MultipartFile file,
                                                              @RequestParam(defaultValue = "false") boolean isPrimary) {
        String filename = carService.uploadCarImage(id, file, isPrimary);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", filename));
    }
}
