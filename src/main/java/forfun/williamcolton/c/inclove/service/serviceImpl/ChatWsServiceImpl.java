package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.alicp.jetcache.Cache;
import forfun.williamcolton.c.inclove.component.SIDSnowflakeBuilder;
import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.AckDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.ReturnAckDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.ReturnMessageDto;
import forfun.williamcolton.c.inclove.model.chat.UserStatus;
import forfun.williamcolton.c.inclove.service.ChatWsService;
import forfun.williamcolton.c.inclove.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWsServiceImpl implements ChatWsService {

    public final static Long cacheExpirationTimeAfterLosingHeartbeat = 20L;
    private final Long batch = 100L;
    private final Integer maxRetry = 5;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;
    private final Cache<String, UserStatus> userStatusCache;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void send(SendMessageDto sendMessageDto) {
        messageService.saveMessage();
        String sid = SIDSnowflakeBuilder.getNextSID();
        if (isTheUserOnline(userStatusCache, sendMessageDto.recipientId())) {
            long nextCheckAt = System.currentTimeMillis() + 1000;
            redisTemplate.opsForZSet().add("ack_pending_list", sid, nextCheckAt);
            Map<String, String> value = Map.of("userId", String.valueOf(sendMessageDto.recipientId()), "content", sendMessageDto.content(), "retry", String.valueOf(0), "nextCheckAt", String.valueOf(nextCheckAt));
            redisTemplate.opsForHash().putAll("pending:" + sid, value);
            redisTemplate.expire("pending:" + sid, 10, TimeUnit.MINUTES);
            simpMessagingTemplate.convertAndSendToUser(sendMessageDto.recipientId(), "/queue/conversations", new ReturnMessageDto(sid, sendMessageDto.content(), sendMessageDto.recipientId()));
        }
    }

    @Scheduled(cron = "0/5 * * * * *")
    private void checkAckQueue() {
        var ackPendingList = redisTemplate.boundZSetOps("ack_pending_list");
        for (int i = 0; i < batch; i++) {
            var minAck = ackPendingList.popMin();
            if (minAck == null) {
                break;
            }
            var score = minAck.getScore();
            var sid = minAck.getValue();
            if (score > System.currentTimeMillis()) {
                if (sid != null) {
                    ackPendingList.add(sid, score);
                }
                break;
            }

            String key = "pending:" + sid;
            var penging = redisTemplate.boundHashOps(key).entries();
            if (penging == null || penging.isEmpty()) {
                continue;
            }

            var userId = String.valueOf(penging.get("userId"));
            var content = String.valueOf(penging.get("content"));
            simpMessagingTemplate.convertAndSendToUser(userId, "/queue/conversations", new ReturnMessageDto(sid, content, userId));

            var retry = Integer.valueOf((String) penging.get("retry"));
            if (retry + 1 > maxRetry) {
                // redisTemplate.opsForHash().delete(key);
                // 这是删除了单个哈希键呜呜呜
                redisTemplate.delete(key);
                log.warn("sid: " + sid + " ack failed");
            } else {
                var nextCheckAt = Long.parseLong((String) penging.get("nextCheckAt")) + 1000L;
                retry += 1;
                redisTemplate.opsForHash().put(key, "retry", String.valueOf(retry));
                redisTemplate.opsForHash().put(key, "nextCheckAt", String.valueOf(nextCheckAt));
                ackPendingList.add(sid, nextCheckAt);
            }
        }
    }

    private boolean isTheUserOnline(Cache<String, UserStatus> userStatusCache, String userId) {
        UserStatus userStatus = userStatusCache.get(userId);
        return userStatus != null && userStatus.isOnline();
    }

    @Override
    public void doHeartbeat(String userId) {
        userStatusCache.put(userId, new UserStatus(true, LocalDateTime.now()), cacheExpirationTimeAfterLosingHeartbeat, TimeUnit.SECONDS);
    }

    @Override
    public void ack(AckDto ackDto, String userId) {
        var sid = ackDto.getSid();
        redisTemplate.delete("pending:" + sid);
        redisTemplate.opsForZSet().remove("ack_pending_list", sid);
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/conversations", new ReturnAckDto(sid, ackDto.getConversationId()));
        simpMessagingTemplate.convertAndSendToUser(ackDto.getSenderID(), "/queue/conversations", new ReturnAckDto(sid, ackDto.getConversationId()));
    }

}
