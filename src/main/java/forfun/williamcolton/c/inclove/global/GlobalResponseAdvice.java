package forfun.williamcolton.c.inclove.global;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> ret = returnType.getParameterType();

        if (GlobalApiResponse.class.isAssignableFrom(ret)) return false;
        if (ResponseEntity.class.isAssignableFrom(ret))   return false;
        if (CharSequence.class.isAssignableFrom(ret))     return false;

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  org.springframework.http.server.ServerHttpRequest req,
                                  org.springframework.http.server.ServerHttpResponse resp) {

        if (mediaType == null || !MediaType.APPLICATION_JSON.includes(mediaType)) {
            return body;
        }

        if (body instanceof GlobalApiResponse<?>) {
            return body;
        }

        if (body instanceof ResponseEntity<?>) {
            return body;
        }

        if (body == null) {
            return GlobalApiResponse.success(null);
        }

        return GlobalApiResponse.success(body);
    }
}
