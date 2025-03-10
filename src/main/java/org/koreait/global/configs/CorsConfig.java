package org.koreait.global.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

// 도메인별로 출처를 공유할 수 있게 해주는 연결고리같은거 // 안될경우 데이터를 차단
@Configuration
public class CorsConfig {

    @Value("${cors.allowed}")
    private String allowedOrigin;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        if (StringUtils.hasText(allowedOrigin)) {
            List<String> origins = Arrays.stream(allowedOrigin.split(",")).toList();
            config.setAllowedOrigins(origins);
            config.setAllowCredentials(true);
        } else {
            config.addAllowedOrigin("*");
        }

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}