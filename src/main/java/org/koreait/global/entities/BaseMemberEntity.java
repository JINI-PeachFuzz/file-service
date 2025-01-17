package org.koreait.global.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // 자동으로 회원정보가 들어가야해서 넣었음
public abstract class BaseMemberEntity extends BaseEntity { // 날짜시간도 상속받게

    @CreatedBy
    @Column(length = 60, updatable = false) // 수정도 불가하게 설정
    private String createdBy;

    @LastModifiedBy
    @Column(length = 60, insertable = false)
    private String modifiedBy;

}
