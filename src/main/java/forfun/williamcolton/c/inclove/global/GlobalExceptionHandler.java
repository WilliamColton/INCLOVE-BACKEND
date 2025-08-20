package forfun.williamcolton.c.inclove.global;

import forfun.williamcolton.c.inclove.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：自定义
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleBiz(BusinessException businessException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.error(businessException.getCode(), businessException.getMessage()));
    }

    // 认证/授权
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<GlobalApiResponse<Void>> handleDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(GlobalApiResponse.error(10403, "No permission"));
    }

    // 兜底
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleAll(Exception ex) {
        // 记录错误日志 + traceId
        log.error("unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiResponse.error(10500, "The server is offside"));
    }
}
