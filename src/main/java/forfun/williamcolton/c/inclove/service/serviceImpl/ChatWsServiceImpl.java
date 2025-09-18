package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.alicp.jetcache.Cache;
import forfun.williamcolton.c.inclove.component.SIDSnowflakeBuilder;
import forfun.williamcolton.c.inclove.dto.chat.req.SendMessageDto;
import forfun.williamcolton.c.inclove.dto.chat.req.UserStatusDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.AckDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.ReturnAckDto;
import forfun.williamcolton.c.inclove.dto.chat.resp.ReturnMessageDto;
import forfun.williamcolton.c.inclove.entity.Message;
import forfun.williamcolton.c.inclove.model.chat.UserStatus;
import forfun.williamcolton.c.inclove.service.ChatWsService;
import forfun.williamcolton.c.inclove.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Override
    public void send(SendMessageDto sendMessageDto, String userId) {
        String sid = SIDSnowflakeBuilder.getNextSID();

        Message message = modelMapper.map(sendMessageDto, Message.class);
        message.setMessageId(sid);
        message.setSenderId(userId);
        messageService.saveMessage(message);

        // 在线就走正常的流程
        if (isTheUserOnline(userStatusCache, sendMessageDto.recipientId())) {
            long nextCheckAt = System.currentTimeMillis() + 1000;
            redisTemplate.opsForZSet().add("ack_pending_list", sid, nextCheckAt);
            Map<String, String> value = Map.of("userId", String.valueOf(userId), "peerUserId", sendMessageDto.recipientId(), "conversationId", sendMessageDto.conversationId(), "content", sendMessageDto.content(), "retry", String.valueOf(0), "nextCheckAt", String.valueOf(nextCheckAt));
            redisTemplate.opsForHash().putAll("pending:" + sid, value);
            redisTemplate.expire("pending:" + sid, 10, TimeUnit.MINUTES);
            simpMessagingTemplate.convertAndSendToUser(sendMessageDto.recipientId(), "/queue/conversations", new ReturnMessageDto(sid, sendMessageDto.content(), sendMessageDto.recipientId(), userId, sendMessageDto.conversationId()));
        } else {
            // 不在线就伪造一个ack包好咯
            simpMessagingTemplate.convertAndSendToUser(userId, "/queue/conversations", new ReturnAckDto(sid, sendMessageDto.conversationId()));
        }

    }

    // 根据服务端无状态的设计原则，该部分本来应该放到客户端
    // 但是鉴于压力全部给到客户端之后，服务端干的事情就太少了（主要是我不喜欢写客户端）
    // 因此该重试过程放到服务端执行
    // 此处引用一段参考文章：http://www.52im.net/thread-294-1-1.html
    // 1）上述设计理念，由客户端重传，可以保证服务端无状态性（架构设计基本准则）；
    // 2）如果client-B不在线，im-server保存了离线消息后，要伪造ack:N发送给client-A；
    // 3）离线消息的拉取，为了保证消息的可靠性，也需要有ack机制，但由于拉取离线消息不存在N报文，故实际情况要简单的多，即先发送offline:R报文拉取消息，收到offline:A后，再发送offlineack:R删除离线消息。
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
            var pending = redisTemplate.boundHashOps(key).entries();
            if (pending == null || pending.isEmpty()) {
                continue;
            }

            var userId = String.valueOf(pending.get("userId"));
            var content = String.valueOf(pending.get("content"));
            var peerUserId = String.valueOf(pending.get("peerUserId"));
            var conversationId = String.valueOf(pending.get("conversationId"));
            simpMessagingTemplate.convertAndSendToUser(peerUserId, "/queue/conversations", new ReturnMessageDto(sid, content, userId, peerUserId, conversationId));

            var retry = Integer.valueOf((String) pending.get("retry"));
            if (retry + 1 > maxRetry) {
                // redisTemplate.opsForHash().delete(key);
                // 这是删除了单个哈希键呜呜呜
                redisTemplate.delete(key);
                log.warn("sid: " + sid + " ack failed");
            } else {
                var nextCheckAt = Long.parseLong((String) pending.get("nextCheckAt")) + 1000L;
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
    public void doHeartbeat(UserStatusDto userStatusDto, String userId) {
        userStatusCache.put(userId, new UserStatus(true, userStatusDto.getIsTyping(), LocalDateTime.now()), cacheExpirationTimeAfterLosingHeartbeat, TimeUnit.SECONDS);
        simpMessagingTemplate.convertAndSendToUser(userStatusDto.getPeerId(), "/queue/conversations", userStatusDto);
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
