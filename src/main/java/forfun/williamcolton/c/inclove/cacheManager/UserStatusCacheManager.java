package forfun.williamcolton.c.inclove.cacheManager;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import forfun.williamcolton.c.inclove.model.chat.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class UserStatusCacheManager {

    private final CacheManager cacheManager;

    @Bean(name = "userStatusCache")
    public Cache<String, UserStatus> init() {
        QuickConfig qc = QuickConfig.newBuilder("userStatusCache")
                .expire(Duration.ofSeconds(100))
                .cacheType(CacheType.BOTH)
                .localLimit(500)
                .syncLocal(true)
                .build();
        return cacheManager.getOrCreateCache(qc);
    }

}
