package org.koreait.global.libs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("파일서비스 주소가져오는지테스트")
@SpringBootTest
@ActiveProfiles({"default", "test", "dev"})
public class ServiceUrlTest {

    @Autowired
    private Utils utils;

    @Test
    void urlTest() {
        String  url = utils.serviceUrl("file-service", "/upload"); // http://localhost:3002/upload 이렇게 나옴
//        System.out.println(url);
//        System.out.println("test");
    }
}
