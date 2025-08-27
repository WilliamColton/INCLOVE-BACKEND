package forfun.williamcolton.c.inclove.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSONSerialization {

    @Bean
    public ObjectMapper buildObjectMapper() {
        return new ObjectMapper();
    }

}
