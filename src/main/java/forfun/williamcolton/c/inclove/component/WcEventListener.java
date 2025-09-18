package forfun.williamcolton.c.inclove.component;

import com.alicp.jetcache.Cache;
import forfun.williamcolton.c.inclove.model.chat.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static forfun.williamcolton.c.inclove.service.serviceImpl.ChatWsServiceImpl.cacheExpirationTimeAfterLosingHeartbeat;

@Slf4j
@Component
@RequiredArgsConstructor
public class WcEventListener {

    private final Cache<String, UserStatus> userStatusCache;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {  // 注意是已经连接事件而非连接事件
        if (event.getUser() == null) {
            return;
        }
        String userId = event.getUser().getName();
        log.info("用户ID: " + userId + " 已登录");
        userStatusCache.put(userId, new UserStatus(true, false, LocalDateTime.now()), cacheExpirationTimeAfterLosingHeartbeat, TimeUnit.SECONDS);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null) {
            return;
        }
        String userId = event.getUser().getName();
        log.info("用户ID: " + userId + " 已下线");
        userStatusCache.put(userId, new UserStatus(false, false, LocalDateTime.now()), cacheExpirationTimeAfterLosingHeartbeat, TimeUnit.SECONDS);
    }

}
