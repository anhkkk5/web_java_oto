package com.carsales.service.impl;

import com.carsales.dto.request.CarRequest;
import com.carsales.dto.response.CarResponse;
import com.carsales.dto.response.PageResponse;
import com.carsales.entity.Brand;
import com.carsales.entity.Car;
import com.carsales.entity.CarImage;
import com.carsales.entity.Category;
import com.carsales.exception.ResourceNotFoundException;
import com.carsales.repository.BrandRepository;
import com.carsales.repository.CarRepository;
import com.carsales.repository.CategoryRepository;
import com.carsales.service.CarService;
import com.carsales.util.FileUploadUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadUtil fileUploadUtil;

    public CarServiceImpl(CarRepository carRepository, BrandRepository brandRepository,
                         CategoryRepository categoryRepository, FileUploadUtil fileUploadUtil) {
        this.carRepository = carRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.fileUploadUtil = fileUploadUtil;
    }

    @Override
    public PageResponse<CarResponse> searchCars(String name, Long brandId, Long categoryId,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Integer modelYear, String status,
                                               int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Car> carPage = carRepository.searchCars(name, brandId, categoryId, minPrice, maxPrice, 
                                                     modelYear, status, pageable);

        List<CarResponse> content = carPage.getContent().stream()
                .map(this::mapToCarResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, carPage.getNumber(), carPage.getSize(),
                carPage.getTotalElements(), carPage.getTotalPages(), carPage.isLast());
    }

    @Override
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        return mapToCarResponse(car);
    }

    @Override
    @Transactional
    public CarResponse createCar(CarRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Car car = new Car();
        car.setName(request.getName());
        car.setBrand(brand);
        car.setCategory(category);
        car.setModelYear(request.getModelYear());
        car.setPrice(request.getPrice());
        car.setColor(request.getColor());
        car.setMileage(request.getMileage());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setEngineCapacity(request.getEngineCapacity());
        car.setSeats(request.getSeats());
        car.setDescription(request.getDescription());
        car.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        car.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);

        Car savedCar = carRepository.save(car);
        return mapToCarResponse(savedCar);
    }

    @Override
    @Transactional
    public CarResponse updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        car.setName(request.getName());
        car.setBrand(brand);
        car.setCategory(category);
        car.setModelYear(request.getModelYear());
        car.setPrice(request.getPrice());
        car.setColor(request.getColor());
        car.setMileage(request.getMileage());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setEngineCapacity(request.getEngineCapacity());
        car.setSeats(request.getSeats());
        car.setDescription(request.getDescription());
        car.setStatus(request.getStatus());
        car.setStockQuantity(request.getStockQuantity());

        Car updatedCar = carRepository.save(car);
        return mapToCarResponse(updatedCar);
    }

    @Override
    @Transactional
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));
        
        // Delete associated images
        car.getImages().forEach(image -> fileUploadUtil.deleteFile(image.getImageUrl()));
        
        carRepository.delete(car);
    }

    @Override
    @Transactional
    public String uploadCarImage(Long carId, MultipartFile file, boolean isPrimary) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        String filename = fileUploadUtil.saveFile(file);

        CarImage carImage = new CarImage();
        carImage.setCar(car);
        carImage.setImageUrl(filename);
        carImage.setIsPrimary(isPrimary);

        car.getImages().add(carImage);
        carRepository.save(car);

        return filename;
    }

    private CarResponse mapToCarResponse(Car car) {
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setName(car.getName());
        response.setBrandId(car.getBrand().getId());
        response.setBrandName(car.getBrand().getName());
        response.setCategoryId(car.getCategory().getId());
        response.setCategoryName(car.getCategory().getName());
        response.setModelYear(car.getModelYear());
        response.setPrice(car.getPrice());
        response.setColor(car.getColor());
        response.setMileage(car.getMileage());
        response.setFuelType(car.getFuelType());
        response.setTransmission(car.getTransmission());
        response.setEngineCapacity(car.getEngineCapacity());
        response.setSeats(car.getSeats());
        response.setDescription(car.getDescription());
        response.setStatus(car.getStatus());
        response.setStockQuantity(car.getStockQuantity());
        response.setImageUrls(car.getImages().stream()
                .map(CarImage::getImageUrl)
                .collect(Collectors.toList()));
        return response;
    }
}
