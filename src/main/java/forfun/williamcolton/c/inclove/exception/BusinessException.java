package forfun.williamcolton.c.inclove.exception;

public class BusinessException extends RuntimeException {

    private int code;
    private String message;
    ErrorCode errorCode;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
