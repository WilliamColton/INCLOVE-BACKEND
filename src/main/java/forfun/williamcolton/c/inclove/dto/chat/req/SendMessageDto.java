package forfun.williamcolton.c.inclove.dto.chat.req;

import forfun.williamcolton.c.inclove.enums.PackageType;

import java.time.LocalDateTime;

public record SendMessageDto(PackageType packageType, String recipientId, String conversationId, String content,
                             LocalDateTime createdAt) {
}
// 此时设计一下会话id，防止以后可能出现群聊这种鬼东西
