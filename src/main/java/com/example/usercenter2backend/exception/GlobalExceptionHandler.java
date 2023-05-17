    package com.example.usercenter2backend.exception;

    import com.example.usercenter2backend.common.BaseResponse;
    import com.example.usercenter2backend.common.ErrorCode;
    import com.example.usercenter2backend.common.ResultUtils;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;

    @RestControllerAdvice
    @Slf4j
    public class GlobalExceptionHandler {
        @ExceptionHandler(BusinessException.class)
        public BaseResponse businessExceptionHandler(BusinessException e){
            log.error("businessExceptionHandler" + e.getMessage(),e);
            return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
        }

        @ExceptionHandler(RuntimeException.class)
        public BaseResponse runtimeExceptionHandler(BusinessException e){
            log.error("runtimeException" ,e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
        }
    }
