package org.koreait.global.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.koreait.file.entities.FileInfo;
import org.koreait.global.libs.Utils;
import org.koreait.global.rests.JSONData;
import org.koreait.member.contants.Authority;
import org.koreait.member.test.annotations.MockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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


    @DisplayName("파일 통합 테스트")
    @Test
    @MockMember(authority = {Authority.USER, Authority.ADMIN})
    void uploadTest() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.png", "image/png", "abc".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.png", "image/png", "abc".getBytes());

        String body = mockMvc.perform(multipart("/upload")
                .file(file1)
                .file(file2)
                .param("gid", "testgid")
                .param("testlocation", "testlocation")
        ).andDo(print()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        // 바디에 있는 데이터들을 모두 가져오고 그중에서 Response에서 찾고 그걸 스트링형으로 변경

        JSONData data = om.readValue(body, JSONData.class);
        System.out.println(data.getData()); // LinkedHashMap -> List<FileINfo> 변환 불가, LinkedHashmap -> string -> List<FileInfo>
        List<FileInfo> items = om.readValue(om.writeValueAsString(data.getData()), new TypeReference<>() {});
        // writeValueAsString -> 자바객체를 JSON형태로 직렬화 / ObjectMapper(om)이 있어야함.

        //System.out.println("----- 확인 -------");
        //items.forEach(System.out::println);

        /* 파일 단일 삭제 테스트 */
        mockMvc.perform(delete("/delete/" + items.get(0).getSeq())) // get(0)은 2개중(배열) seq가 1이니까 0을 넣어서 첫번째꺼를 가져옴
                .andDo(print());


        /* 파일 삭제 통합 테스트 */
        mockMvc.perform(delete("/deletes/testgid/testlocation"))
                .andDo(print());


        /* 파일 조회 통합 테스트 */
        mockMvc.perform(get("/list/testgid/testlocation"))
                .andDo(print());

    }
}
