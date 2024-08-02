package be.iccbxl.tfe.Driveshare.classes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CacheInspector {

    @Autowired
    private CacheManager cacheManager;

    public void printCacheContents() {
        Cache cache = cacheManager.getCache("geocodeCache");
        if (cache != null && cache.getNativeCache() instanceof Map) {
            Map<Object, Object> cacheMap = (Map<Object, Object>) cache.getNativeCache();
            cacheMap.forEach((key, value) -> {
                System.out.println("Key: " + key + ", Value: " + value);
            });
        } else {
            System.out.println("Cache not found or not a ConcurrentMapCache.");
        }
    }
}
