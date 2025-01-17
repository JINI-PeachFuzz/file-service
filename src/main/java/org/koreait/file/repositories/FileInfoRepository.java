package org.koreait.file.repositories;

import org.koreait.file.entities.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface FileInfoRepository extends JpaRepository<FileInfo, Long>, QuerydslPredicateExecutor<FileInfo> {

// Long 은 파일번호가 Long값으로 되어있어서임

}
