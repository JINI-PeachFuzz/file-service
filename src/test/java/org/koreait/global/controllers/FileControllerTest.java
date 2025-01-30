package org.koreait.global.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.koreait.global.libs.Utils;
import org.koreait.member.contants.Authority;
import org.koreait.member.test.annotations.MockMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FileControllerTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Utils utils;

    @Autowired
    private MockMvc mockMvc;

    private String token;

//    @BeforeEach
//    void init() throws Exception {
//
//    }

    @Test
    @MockMember(authority = {Authority.USER, Authority.ADMIN})
    void uploadTest() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.png", "image/png", "abc".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.png", "image/png", "abc".getBytes());

        mockMvc.perform(multipart("/upload")
                .file(file1)
                .file(file2)
                .header("Authentication", "Bearer " + token)
                .param("gid", "testgid")
                .param("testlocation", "testlocation")
        ).andDo(print());
    }
}