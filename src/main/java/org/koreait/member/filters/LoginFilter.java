package org.koreait.member.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.koreait.global.libs.Utils;
import org.koreait.global.rests.JSONData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
// 토큰이 넘어오면 로그인처리까지 가능하게 하는거 구현할거 / 회원정보 유지
@Component
@RequiredArgsConstructor
public class LoginFilter extends GenericFilterBean {

    private final Utils utils;
    private final RestTemplate restTemplate;
    private final ObjectMapper om;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String token = utils.getAuthToken();
        loginProcess(token);

        chain.doFilter(request, response);
    }

    /***
     * 로그인 처리
     * @param token
     */
    private void loginProcess(String token) {
        if (!StringUtils.hasText(token)) {
            return; // 토큰이 없는 일반 요청인 경우는 처리 X
        }

        /***
         * 1. 토큰이 있으면, member-service 인스턴스에서 회원 정보 조회
         * 2. 로그인 처리 // 정보를 api로 보내는거임
         */
        try {
            String apiUrl = utils.serviceUrl("member-service", "/");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<JSONData> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, JSONData.class);

            System.out.println("--------response-----------");
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}