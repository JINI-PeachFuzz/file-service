package org.koreait.file.exceptions;

import org.koreait.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class FileNotFoundException extends CommonException {
    public FileNotFoundException() {
        super("NotFound.file", HttpStatus.NOT_FOUND);
        setErrorCode(true);
    }
}
