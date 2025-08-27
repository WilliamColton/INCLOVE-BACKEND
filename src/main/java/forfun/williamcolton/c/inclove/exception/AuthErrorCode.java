package forfun.williamcolton.c.inclove.exception;

public enum AuthErrorCode implements ErrorCode {
    USER_NOT_FOUND(1000, "User not found"),
    WRONG_CREDENTIALS(1001, "Wrong email or password"),
    USER_NOT_VERIFIED(1002, "User is not verified"),
    USER_OR_EMAIL_EXISTS(1003, "Username or email already exists"),
    USER_ALREADY_VERIFIED(1004, "User already verified"),
    INVALID_VERIFICATION_CODE(1005, "Invalid verification code"),
    EMAIL_SEND_FAIL(1006, "Email send fail"),
    GOOGLE_INVALID_ID_TOKEN(1100, "Invalid Google ID token"),
    GOOGLE_VERIFICATION_FAILED(1101, "Google verification failed"),
    GOOGLE_EMAIL_NOT_VERIFIED(1102, "Google email is not verified"),
    OAUTH_ACCOUNT_CONFLICT(1103, "This email is already linked to another account"),
    UNAUTHORIZED(10000, "Unauthorized"),
    NO_PERMISSION(10001, "No permission");

    private final Integer code;
    private final String message;

    AuthErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}