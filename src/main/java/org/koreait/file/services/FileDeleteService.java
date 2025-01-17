package org.koreait.file.services;

import lombok.RequiredArgsConstructor;
import org.koreait.file.constants.FileStatus;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.repositories.FileInfoRepository;
import org.koreait.global.exceptions.UnAuthorizedException;
import org.koreait.member.MemberUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class FileDeleteService {
    private final FileInfoService infoService;
    private final FileInfoRepository infoRepository;
    private final MemberUtil memberUtil;

    // 삭제한다음에 그 데이터를 반환값으로 넣을 꺼
    // 파일삭제할때 데이터를 확인해야하는 경우도 있으니 seq가 들어간거
    public FileInfo delete(Long seq) {
        FileInfo item = infoService.get(seq);
        String filePath = item.getFilePath();
        // 0. 파일 소유자만 삭제 가능하게 통제 - 다만 관리자는 가능
        // 관리자는 만능! 다 가능함!
        String createdBy = item.getCreatedBy();
        if (!memberUtil.isAdmin() && StringUtils.hasText(createdBy)
                && (!memberUtil.isLogin() || !memberUtil.getMember().getEmail().equals(createdBy))) {
            throw new UnAuthorizedException(); // 파일소유자가 맞는지 확인 아니면 401응답코드 나오게.
        }

        // 삭제할려면
        // 1. DB에서 정보를 제거
        infoRepository.delete(item);
        infoRepository.flush();

        // 2. 파일이 서버에 존재하면 파일도 삭제 / 없을 때 삭제하면 오류발생함
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        } // 예를 들면 폴더안에 파일을 삭제해서 빈폴더 만드는 것

        // 3. 삭제된 파일 정보를 반환
        return item;
    }

    public List<FileInfo> deletes(String gid, String location) {
        List<FileInfo> items = infoService.getList(gid, location, FileStatus.ALL);
        items.forEach(i -> delete(i.getSeq()));

        return items;
    }

    public List<FileInfo> deletes(String gid) {
        return deletes(gid, null);
    }
}
