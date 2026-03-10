package com.carsales.util;

import com.carsales.enums.RoleType;

public class AppConstants {
    
    // Pagination
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    
    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    
    // Roles
    public static final RoleType ROLE_CUSTOMER = RoleType.ROLE_CUSTOMER;
    public static final RoleType ROLE_ADMIN = RoleType.ROLE_ADMIN;
    
    // Cache Keys
    public static final String CACHE_CAR_PREFIX = "car:";
    public static final String CACHE_BRAND_PREFIX = "brand:";
    public static final String CACHE_CATEGORY_PREFIX = "category:";
    
    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}
