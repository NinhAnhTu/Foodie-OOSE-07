package com.oose.restaurant_mis.config;

import com.oose.restaurant_mis.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadPath);
        // Chuyển đổi path thành chuẩn URI (sẽ tự động có dạng file:///...)
        String uploadUri = path.toAbsolutePath().toUri().toString();

        // Ánh xạ URL /uploads/** vào thư mục vật lý trên ổ cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**", "/waiter/**", "/chef/**")
                .excludePathPatterns("/auth/**", "/css/**", "/js/**", "/images/**", "/uploads/**"); // Thêm /uploads/** vào đây
    }
}