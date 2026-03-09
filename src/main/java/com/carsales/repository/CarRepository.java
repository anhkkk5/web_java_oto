package com.carsales.repository;

import com.carsales.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    @Query("SELECT c FROM Car c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:brandId IS NULL OR c.brand.id = :brandId) AND " +
           "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
           "(:modelYear IS NULL OR c.modelYear = :modelYear) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Car> searchCars(
            @Param("name") String name,
            @Param("brandId") Long brandId,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("modelYear") Integer modelYear,
            @Param("status") String status,
            Pageable pageable
    );
}
