package forfun.williamcolton.c.inclove.global;

import lombok.Data;

@Data
public class GlobalWsResponse<T> {
    private int code;        // 200 represents success, others are business error codes
    private String message;  // Friendly prompt
    private T data;          // Business data

    public static <T> GlobalWsResponse<T> success(T data) {
        GlobalWsResponse<T> r = new GlobalWsResponse<>();
        r.code = 200;
        r.message = "OK";
        r.data = data;
        return r;
    }

    public static <T> GlobalWsResponse<T> error(int code, String msg) {
        GlobalWsResponse<T> r = new GlobalWsResponse<>();
        r.code = code;
        r.message = msg;
        return r;
    }
}
