package com.zssystem.exception;

/**
 * 工艺文件业务异常
 */
public class ProcessFileException extends RuntimeException {
    
    public ProcessFileException(String message) {
        super(message);
    }
    
    public ProcessFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
