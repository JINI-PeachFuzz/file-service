package org.koreait.file.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.koreait.global.entities.BaseMemberEntity;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@Data
@Entity // 파일이 올라오면 거기에 대한 정보들을 기록해야함 / 엔티티를 정의했음
@Table(indexes = {
        @Index(name="idx_gid", columnList = "gid, listOrder, createdAt"),
        @Index(name="idx_gid_location", columnList = "gid, location, listOrder, createdAt")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo extends BaseMemberEntity implements Serializable {
    @Id @GeneratedValue // @GeneratedValue 로그인한 회원과 정보가 일치한지 확인
    private Long seq; // 파일 등록 번호

    @Column(length = 45, nullable = false)
    private String gid; // 파일 그룹

    @Column(length = 45)
    private String location; // 그룹 내에서 위치

    @Column(length = 100, nullable = false)
    private String fileName; // 업로드시 원 파일명

    @Column(length = 30)
    private String extension; // 확장자

    @Column(length = 65)
    private String contentType; // 파일 형식 image/png  application/..
    // DB에서 이미지라고 있는것만 검색하고 싶을 때 라던지 나누면 나중에 찾기 편하니까!

    @Transient // DB에 안넣음
    private String fileUrl; // URL로 파일 접근할 수 있는 주소 - 2차 가공

    @Transient
    private String filePath; // 파일이 서버에 있는 경로

    @Transient
    private String thumbUrl; // 썸네일 기본 URL

    private boolean done; // 파일과 연관된 작업이 완료되었는지 여부 체크 // 확정 같은 거!
    // 예를 들면 게시글 작성하다가 중간에 나가면 그건 이도저도 아닌거 그런것들은 모아서 한번에
    // 삭제한다던지 해야하는데 그런거 관련

    private boolean selected; // 노출을 1개 하는 경우 대표 이미지 선택
    private long listOrder; // 정렬 순서, 오름 차순 // 순서를 업데이트할때 사용할 예정

    // 이미지 형식 여부
    public boolean isImage() {

        return StringUtils.hasText(contentType) && contentType.contains("image/");
    } // 썸네일관련하여 추가한 거 / 콘텐트타입이 있으면 그안에 이미지가 있는지 없는지 판단하는거
}


// 엔티티만들고 나면 컨트롤로~