package org.koreait.file.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestUpload {
    @NotBlank
    private String gid;
    private String location;

    private boolean single; // 단일 파일
    private boolean imageOnly; // 이미지 형식 파일만 허용

    private boolean done; // 업로드 하자마자 완료 처리

    public MultipartFile[] files; // 세터를 통해서 또 주입할 거
    // Api파일 컨트롤 70줄쯤 form.setFiles(files); 이런식으로 주입!

    // 커맨드파일로 받아서 처리하고 있음
}