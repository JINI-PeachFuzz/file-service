package org.koreait.global.libs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Lazy
@Component
@RequiredArgsConstructor
public class Utils {
    private final HttpServletRequest request;
    private final MessageSource messageSource;
    private final DiscoveryClient discoveryClient; // 얘가 인스턴스 주소를 찾아줌 / 주소를 직접 명시하면 안되고 찾아주는걸로
    // 로컬호스트면 그걸 찾아오고 환경변수로 찾을 수 있게


    /**
     * 메서지 코드로 조회된 문구
     *
     * @param code
     * @return
     */
    public String getMessage(String code) {
        Locale lo = request.getLocale(); // 사용자 요청 헤더(Accept-Language)

        return messageSource.getMessage(code, null, lo);
    }

    public List<String> getMessages(String[] codes) {

        return Arrays.stream(codes).map(c -> {
            try {
                return getMessage(c);
            } catch (Exception e) {
                return "";
            }
        }).filter(s -> !s.isBlank()).toList();

    }

    /**
     * REST 커맨드 객체 검증 실패시에 에러 코드를 가지고 메세지 추출
     *
     * @param errors
     * @return
     */
    public Map<String, List<String>> getErrorMessages(Errors errors) {
        ResourceBundleMessageSource ms = (ResourceBundleMessageSource) messageSource;
        // 메세지는 항상필요하니까 false그건 지웠음

        // 필드별 에러코드 - getFieldErrors()
        // Collectors.toMap
        Map<String, List<String>> messages = errors.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, f -> getMessages(f.getCodes()), (v1, v2) -> v2));

        // 글로벌 에러코드 - getGlobalErrors()
        List<String> gMessages = errors.getGlobalErrors()
                .stream()
                .flatMap(o -> getMessages(o.getCodes()).stream())
                .toList();
        // 글로벌 에러코드 필드 - global
        if (!gMessages.isEmpty()) {
            messages.put("global", gMessages);
        }

        return messages;
    }

    /***
     * 유레카 서버 인스턴스 주소 검색
     *
     *       spring.profiles.active : dev - localhost로 되어 있는 주소를 반환
     *          - 예) member-service : 최대 2가지만 존재, 1 - 실 서비스 도메인 주소, 2. localhost ...
     * @param serviceId
     * @param url
     * @return
     */
    // 서비스 아이디가 필요함 (유레카에서 확인가능) // 유레카서버에서 가져온 주소를 가져오는 걸 하는거
    public String serviceUrl(String serviceId, String url) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            String profile = System.getenv("spring.profiles.active");
            boolean isDev = StringUtils.hasText(profile) && profile.contains("dev");
            String serviceUrl = null;
            for (ServiceInstance instance : instances) {
                String uri = instance.getUri().toString();
                if (isDev && uri.contains("://localhost")) { // 로컬호스트가 있는건 개발할때 사용함
                    serviceUrl = uri;
                } else if (!isDev && uri.contains("localhost")) {
                    serviceUrl = uri;
                }
            }

            if (StringUtils.hasText(serviceUrl)) {
                return serviceUrl + url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 요청 헤더 : Authorization : Bearer ...
     * @return
     */
    public String getAuthToken() {
        String auth = request.getHeader("Authorization"); // 이게 키면 값으로 토큰이 오는거

        return StringUtils.hasText(auth) ? auth.substring(7).trim() : null;
    }
}