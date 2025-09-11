package forfun.williamcolton.c.inclove.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import forfun.williamcolton.c.inclove.global.GlobalApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.zalando.logbook.*;
import org.zalando.logbook.servlet.LogbookFilter;

import java.io.IOException;
import java.util.Set;

import static org.zalando.logbook.BodyFilter.merge;
import static org.zalando.logbook.core.BodyFilters.defaultValue;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LogbookMaskConfig {

    private final ObjectMapper objectMapper;

    @Bean
    BodyFilter jsonMaskingBodyFilter() {
        return merge(
                defaultValue(),
                replaceJsonStringProperty(
                        Set.of("password", "rawPassword", "token", "authorization"), "***"
                )
        );
    }

    @Bean
    public FilterRegistrationBean<LogbookFilter> logbookFilter() {
        var frb = new FilterRegistrationBean<>(new LogbookFilter());
        frb.setName("logbookFilter");
        frb.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return frb;
    }

    @Bean
    public Strategy buildStrategy() {
        return new Strategy() {
            @Override
            public void write(@NotNull Correlation correlation, @NotNull HttpRequest request, @NotNull HttpResponse response, @NotNull Sink sink) throws IOException {
                var respBody = response.getBodyAsString();
                var jsonNode = objectMapper.readTree(respBody);
                if (jsonNode.isObject()) {
                    var globalApiResponse = objectMapper.readValue(respBody, GlobalApiResponse.class);
                    if (globalApiResponse.getCode() > 200) {
                        sink.writeBoth(correlation, request, response);
                    } else {
                        sink.write(correlation, request);
                    }
                } else {
                    log.warn("返回体应尽可能为json");
                    sink.write(correlation, request);
                }
            }
        };
    }

}
