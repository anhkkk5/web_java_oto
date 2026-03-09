package com.carsales.cache;

public class CacheKeyConstants {
    
    public static final String CAR_CACHE = "cars";
    public static final String BRAND_CACHE = "brands";
    public static final String CATEGORY_CACHE = "categories";
    public static final String USER_CACHE = "users";
    
    public static final long CACHE_TTL_HOURS = 24;
    
    private CacheKeyConstants() {
        throw new IllegalStateException("Constants class");
    }
}
