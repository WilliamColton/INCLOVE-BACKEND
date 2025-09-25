package forfun.williamcolton.c.inclove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class StringRedisTemplateConfig {

    @Bean
    public StringRedisTemplate buildStringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

}
