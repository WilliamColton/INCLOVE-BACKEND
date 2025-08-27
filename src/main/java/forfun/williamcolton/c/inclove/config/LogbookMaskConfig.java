package forfun.williamcolton.c.inclove.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import forfun.williamcolton.c.inclove.global.GlobalApiResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;

import java.io.IOException;
import java.util.Set;

import static org.zalando.logbook.BodyFilter.merge;
import static org.zalando.logbook.core.BodyFilters.defaultValue;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

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
    public Strategy buildStrategy() {
        return new Strategy() {
            @Override
            public void write(@NotNull Correlation correlation, @NotNull HttpRequest request, @NotNull HttpResponse response, @NotNull Sink sink) throws IOException {
                var respBody = response.getBodyAsString();
                if (objectMapper.readValue(respBody, GlobalApiResponse.class).getCode() != 200) {
                    sink.writeBoth(correlation, request, response);
                }
            }
        };
    }
    
}
