package be.iccbxl.tfe.Driveshare.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("geocodingCache");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(6000, TimeUnit.MINUTES)  // Cache expire après 10 minutes
                .maximumSize(1000));  // Taille maximum de 1000 entrées
        return cacheManager;
    }

}
