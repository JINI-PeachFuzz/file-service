package org.koreait.file.services;

import lombok.RequiredArgsConstructor;
import org.koreait.file.FileProperties;
import org.koreait.file.controllers.RequestUpload;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.repositories.FileInfoRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileUploadService {

    private final FileProperties properties; // 파일설정주입 - 업로드 경로가 필요해서
    private final FileInfoRepository fileInfoRepository;
    private final FileInfoService infoService;

    public List<FileInfo> upload(RequestUpload form){
        String gid = form.getGid();
        gid = StringUtils.hasText(gid) ? gid : UUID.randomUUID().toString();
        // 중복되지 않는 4글자를 유니크 ID로 만들어줌
        // UUID라는 유니크하게 만들어주는 편의기능! 4-4-4이런식으로 만들어줌
        String location = form.getLocation();
        MultipartFile[] files = form.getFiles();

        String rootPath = properties.getPath(); // 주경로 / 환경변수로 설정한 곳 // 설정에서 가져옴


        // 파일 업로드 성공 파일 정보
        List<FileInfo> uploadedItems = new ArrayList<>(); // 성공한 파일만 차곡차곡 넣어줌


        for (MultipartFile file : files){ // **향상된 for문(enhanced for-loop)**을 사용하여 files라는 컬렉션(Collection) 안의 모든 요소를 순회하는 코드입니다. 이 문장은 파일 업로드 처리에서 흔히 사용되며, 사용자가 여러 개의 파일을 업로드할 때 그 파일들을 하나씩 처리하는 로직
            String contentType = file.getContentType();
            // 이미지 형식의 파일만 허용하는 경우 - 이미지가 아닌 파일은 건너띄기
            if (form.isImageOnly() && contentType.indexOf("image/") == -1) {
                continue;
            }

            // 1. 파일 업로드 정보 - DB에 기록 S
            // 파일명.확장자 // model.weights.h5
            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf(".")); // substring은 자르겠다 / 마지막.에서 부터 확장자만 뽑아내는거

            FileInfo item = new FileInfo();
            item.setGid(gid);
            item.setLocation(location);
            item.setFileName(fileName);
            item.setExtension(extension);
            item.setContentType(contentType);

            fileInfoRepository.saveAndFlush(item);

            // 1. 파일 업로드 정보 - DB에 기록 E

            // 2. 파일 업로드 처리 S
            long seq = item.getSeq(); // 파일이름을 파일등록번호로 하기 때문에 그걸 가지고 나눌꺼
            String uploadFileName = seq + extension;
            long folder = seq % 10L; // 0 ~ 9 // 균등배분
            File dir = new File(rootPath + folder);
            // 디렉토리가 존재 하지 않거나 파일로만 있는 경우 생성
            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs(); // 하나의 디렉토리만 생성 / make directory
            }

            File _file = new File(dir, uploadFileName); // 서버쪽에 올라갈 파일명
            try {
                file.transferTo(_file); // 지정경로에다가 파일을 저장하겠다는 뜻
                // 여기서는 무조건 try / catch를 사용해야함 안쓰면 인텔리제이에서 오류발생함 너 써라 하는 느낌
                // 여기서 오류(트라이캐치)가 발생하면 파일업로드에 실패한거임 그래서 아래에 실패했을 때 삭제해버리는 것도 구현했음


                // 추가 정보 처리
                infoService.addInfo(item); // addInfo 는 추가로 가공할게 있을 때 사용함

                uploadedItems.add(item);

            } catch (IOException e) {
                // 파일 업로드 실패 -> DB 저장된 데이터를 삭제
                fileInfoRepository.delete(item);
                fileInfoRepository.flush();
            }
            // 2. 파일 업로드 처리 E
        }

        return uploadedItems;
    }
}
