package forfun.williamcolton.c.inclove.exception;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    USER_NOT_FOUND(1000, "error.auth.user_not_found", HttpStatus.NOT_FOUND),
    WRONG_CREDENTIALS(1001, "error.auth.wrong_credentials", HttpStatus.UNAUTHORIZED),
    USER_NOT_VERIFIED(1002, "error.auth.user_not_verified", HttpStatus.FORBIDDEN),
    USER_OR_EMAIL_EXISTS(1003, "error.auth.user_or_email_exists", HttpStatus.CONFLICT),
    USER_ALREADY_VERIFIED(1004, "error.auth.user_already_verified", HttpStatus.BAD_REQUEST),
    INVALID_VERIFICATION_CODE(1005, "error.auth.invalid_verification_code", HttpStatus.BAD_REQUEST),
    GOOGLE_INVALID_ID_TOKEN(1100, "error.auth.google.invalid_id_token", HttpStatus.UNAUTHORIZED),
    GOOGLE_VERIFICATION_FAILED(1101, "error.auth.google.verification_failed", HttpStatus.SERVICE_UNAVAILABLE),
    GOOGLE_EMAIL_NOT_VERIFIED(1102, "error.auth.google.email_not_verified", HttpStatus.FORBIDDEN),
    OAUTH_ACCOUNT_CONFLICT(1103, "error.auth.oauth.account_conflict", HttpStatus.CONFLICT);

    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}