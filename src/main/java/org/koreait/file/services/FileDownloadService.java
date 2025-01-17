package org.koreait.file.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.exceptions.FileNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Lazy
@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final FileInfoService infoService;
    private final HttpServletResponse response;

    public void process(Long seq) {

        FileInfo item = infoService.get(seq); // 파일 조회부터 먼저!

        // String filePath = item.getFilePath(); - 안써서 지웠음 / 걍 남겨둔거!
        String fileName = item.getFileName();
        // 윈도우에서 한글 깨짐 방지 / 인코딩을 변경할 꺼
        fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1); // 8859_1 이게 유니코드 2바이트 형태로 바꿔줬음(2바이트 유니코드를 지원해줌)/ 윈도우는 3~4바이트임
        // 맥은 크게 문제는 없음 , 윈도우가 문제!! 문자표가 달라서 깨짐
        // getBytes(), StandardCharsets.ISO_8859_1 // 바이트를 받아와서 변경해줌

        String contentType = item.getContentType(); // 혹시나 비어있을 경우 누락이라던지
        contentType = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";// 이건 기본값

        File file = new File(item.getFilePath());
        if (!file.exists()) {
            throw new FileNotFoundException();
        } // 파일이 없을 수도 있기 때문에 예외처리를 한거

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            // 바디의 출력을 filename에 지정된 파일로 변경
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 콘텍트타입이 응답 바디인데 현재 상태같은걸 알려주는 거고 그걸 이걸로 변경해주는 거임
            response.setContentType(contentType); // 응답바디에 html 이라고 되어있으면 응답을 보면 html로 나옴
            // contentType도 다운받는 형식에 넣은 이유는 예를 들면 pdf파일을 다운받으면 pdf파일이 열리는데 일반파일은 그냥다운만 받고... 그런식으로 콘텐트타입을 보고 알아서 맞춤형을 해주기 위해서!
            response.setHeader("Cache-Control", "no-cache"); // 다운받는데 이미 있으면 안되니까 // 같은주소로 다운받은때는 저장을 하고 있음 그래서 같은 주소로 다운받더라도 캐시하지않고 새로 받을 수 있게 노캐시해줘야함
            // 나는 다운받았었는데 왜 지금꺼랑 다르지 이런상황발생할 수 있음
            response.setHeader("Pragma", "no-cache"); // 옛날 브라우저용 Pragma
            response.setIntHeader("Expires", 0); // 만료시간을 없앤다. / 용량이 너무많으면 영영 못받음 // 오링기간? 넘으면 서버를 끊어버림..
            response.setContentLengthLong(file.length()); // 파일 용량도 알려줌
            // 유저가 파일을 다운받는 거니까 response 헤더에다가 넣어주는거

            OutputStream out = response.getOutputStream(); // OutputStream으로 넣어주고
            out.write(bis.readAllBytes()); // 모든 형식은(파일에 대한걸) 바이트 형식이므로 그 형식으로 써주는 거

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
