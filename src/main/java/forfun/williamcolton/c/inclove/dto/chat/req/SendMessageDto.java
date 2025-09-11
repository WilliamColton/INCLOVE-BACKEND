package forfun.williamcolton.c.inclove.dto.chat.req;

public record SendMessageDto(String recipientId, Long conversationId, String content) {
}
// 此时设计一下会话id，防止以后可能出现群聊这种鬼东西
