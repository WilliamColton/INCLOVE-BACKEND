package forfun.williamcolton.c.inclove.global;

import lombok.Data;

@Data
public class GlobalApiResponse<T> {
    private int code;        // 200 represents success, others are business error codes
    private String message;  // Friendly prompt
    private T data;          // Business data

    public static <T> GlobalApiResponse<T> success(T data) {
        GlobalApiResponse<T> r = new GlobalApiResponse<>();
        r.code = 200;
        r.message = "OK";
        r.data = data;
        return r;
    }

    public static <T> GlobalApiResponse<T> error(int code, String msg) {
        GlobalApiResponse<T> r = new GlobalApiResponse<>();
        r.code = code;
        r.message = msg;
        return r;
    }
}
