package forfun.williamcolton.c.inclove.global;

import forfun.williamcolton.c.inclove.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleBiz(BusinessException businessException) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GlobalApiResponse.error(businessException.getCode(), businessException.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<Void>> handleAll(Exception ex) {
        // 记录错误日志 + traceId
        log.error("unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiResponse.error(10500, "The server is offside"));
    }
}
