package forfun.williamcolton.c.inclove.dto.chat.req;

import forfun.williamcolton.c.inclove.enums.PackageType;

public record SendMessageDto(PackageType packageType, String recipientId, String conversationId, String content) {
    public SendMessageDto(String recipientId, String conversationId, String content) {
        this(PackageType.MESSAGE, recipientId, conversationId, content);
    }
}
// 此时设计一下会话id，防止以后可能出现群聊这种鬼东西
