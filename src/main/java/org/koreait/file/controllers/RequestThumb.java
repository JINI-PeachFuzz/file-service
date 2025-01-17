package org.koreait.file.controllers;

import lombok.Data;

@Data
public class RequestThumb {
    private Long seq;
    private String url; // 원격 이미지 URL / Long값의 seq이든 url이든 둘중에 하나는 있어야함ㅁ
    private int width;
    private int height; // 너비와 높이를 정확하게 하는게 아니고 둘중에 큰걸 기준으로 삼음
// 사이트에서 이미지보면 뒤에 type이라던지 그런거 뒤에 숫자있는데 그게 이런식으로 해서 사이즈를 정의해논거
    // 지우면 원래 사이즈로 이미지가 나옴 / 우리는 짤리는걸 방지하기 위해서 맞추는 거였음
    // 이런식으로 워터마크도 가능 ? (맞나?)
}
