package org.koreait.file.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.file.constants.FileStatus;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.services.*;
import org.koreait.global.exceptions.BadRequestException;
import org.koreait.global.libs.Utils;
import org.koreait.global.rests.JSONData;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

// API가 붙어있으면 레스트임 JSON형태를 많이 사용하기 떄문에 그 형태로 만들어주는거
@Tag(name="파일 API", description = "파일 업로드, 조회, 다운로드, 삭제기능을 제공합니다.")
@RestController
//@RequestMapping("/api/file") 이건 게이트웨이에서 설정하면 되므로 삭제했음
@RequiredArgsConstructor
public class FileController {

    private final Utils utils;

    private final FileUploadService uploadService;

    private final FileDownloadService downloadService;

    private final FileInfoService infoService;

    private final FileDeleteService deleteService;

    private final FileDoneService doneService;

    private final ThumbnailService thumbnailService;

    private final FileImageService imageService;

    /**
     * 파일 업로드
     */
    @Operation(summary = "파일 업로드 처리")
    @ApiResponse(responseCode = "201", description = "파일 업로드 처리, 업로드 성공시에는 업로드 완료된 파일 목록을 반환한다. 요청시 반드시 요청헤더에 multipart/form-data 형식으로 전송") // 응답코드 명시
    @Parameters({
            @Parameter(name="gid", description = "파일 그룹 ID", required = true), // 빨간색별표가 붙음 / 필수라고
            @Parameter(name = "location", description = "파일 그룹 내에서 위치 코드"),
            @Parameter(name = "file", description = "업로드 파일, 복수개 전송 가능", required = true)
    }) // 설명단거 / 다른사람들이 보기때문에 설명을 잘 달아줘야함 / 로컬호스트3000/apidocs/html 에 있음
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload")
    public JSONData upload(@RequestPart("file") MultipartFile[] files, @Valid RequestUpload form, Errors errors) {
        // MultipartFile[] files은 이미 정해진 인터페이스임! / 파일이름, 콘텐트타입, 용량등 그런거
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors)); // 2차가공 / getErrorMessages(errors)는 errors를 일괄적으로 가공하기 위해서 만든거 / map형태로 들어감
        }

        form.setFiles(files); // 이미지를 눌렀을 때 여기로 들어가는 거

        /**
         * 단일 파일 업로드
         *      - 기 업로드된 파일을 삭제하고 새로 추가
         */
        if (form.isSingle()) {
            deleteService.deletes(form.getGid(), form.getLocation());
        }

        List<FileInfo> uploadedFiles = uploadService.upload(form); // 업로드 처리!

        // 업로드 완료 하자마자 완료 처리
        if (form.isDone()) {
            doneService.process(form.getGid(), form.getLocation());
        }

        JSONData data = new JSONData(uploadedFiles); // 제이슨형태로 출력하기 위해 넣어줬음
        data.setStatus(HttpStatus.CREATED);

        return data;
    }

    // 파일 다운로드
    @GetMapping("/download/{seq}")
    public void download(@PathVariable("seq") Long seq) {// 파일다운로드는 직접하는거기 때문에 void로 한거
        downloadService.process(seq);
    } // 다운할때는 응답헤더가 중요함
    // 파일이름으로 출력되는걸 바꿔버리는데... seq만 가지고도 다운이 가능하게 만든거

    // 파일 단일 조회
    @GetMapping("/info/{seq}")
    public JSONData info(@PathVariable("seq") Long seq) {
        FileInfo item = infoService.get(seq);

        return new JSONData(item); // 200을 넣었고 JSON형태로 나올꺼
    }

    /**
     * 파일 목록 조회
     * gid, location
     */
    @GetMapping(path = {"/list/{gid}", "/list/{gid}/{location}"})
    public JSONData list(@PathVariable("gid") String gid,
                         @PathVariable(name = "location", required = false) String location, // required=false 필수아니면 false / gid는 필순데 이거는 필수아님!
                         @RequestParam(name = "status", defaultValue = "DONE") FileStatus status) { // "DONE"으로 하면 완료된것만 보임
// @RequestParam**은 Spring MVC에서 사용되는 어노테이션으로, HTTP 요청의 파라미터를 컨트롤러 메서드의 매개변수로 전달하는 데 사용됨. 주로 GET 요청의 쿼리 파라미터나 POST 요청의 폼 데이터를 처리할 때 사용. // FileStatus에 파일 완료와 미완에대해서 정의해놨음

        List<FileInfo> items = infoService.getList(gid, location, status);

        return new JSONData(items);

    } // 정보를 조회해야 삭제라던지 다운로드도 가능함

    // 파일 단일 삭제
    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {

        FileInfo item = deleteService.delete(seq);

        return new JSONData(item);
    }

    @DeleteMapping({"/deletes/{gid}","/deletes/{gid}/{location}"})
    public JSONData delete(@PathVariable("gid") String gid,
                           @PathVariable(name="location", required = false) String location) {

        List<FileInfo> items = deleteService.deletes(gid, location);

        return new JSONData(items);
    }
 // 썸네일! RequestThumb 을 들어가면 크기라던지 그런거 정의해놨음
    @GetMapping("/thumb")
    public void thumb(RequestThumb form, HttpServletResponse response) {
        String path = thumbnailService.create(form);
        if (!StringUtils.hasText(path)) {
            return;
        }

        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            String contentType = Files.probeContentType(file.toPath());
            response.setContentType(contentType);

            OutputStream out = response.getOutputStream();
            out.write(bis.readAllBytes());
            // 바디쪽에 출력해서 이미지가 바로 보이게 만듦
        } catch (IOException e) {}
    }

    @GetMapping("/select/{seq}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void select(@PathVariable("seq") Long seq) {
        imageService.select(seq);
    }
}
