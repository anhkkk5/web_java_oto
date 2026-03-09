package com.carsales.service;

import com.carsales.dto.request.CarRequest;
import com.carsales.dto.response.CarResponse;
import com.carsales.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface CarService {
    PageResponse<CarResponse> searchCars(String name, Long brandId, Long categoryId, 
                                        BigDecimal minPrice, BigDecimal maxPrice, 
                                        Integer modelYear, String status,
                                        int page, int size, String sortBy, String sortDir);
    CarResponse getCarById(Long id);
    CarResponse createCar(CarRequest request);
    CarResponse updateCar(Long id, CarRequest request);
    void deleteCar(Long id);
    String uploadCarImage(Long carId, MultipartFile file, boolean isPrimary);
}
