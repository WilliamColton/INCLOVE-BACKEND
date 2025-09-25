package forfun.williamcolton.c.inclove.dto.chat.resp;

import forfun.williamcolton.c.inclove.enums.PackageType;

public record ReturnMessageDto(PackageType packageType, String sid, String content, String recipientID, String senderId,
                               String conversationId) {
    public ReturnMessageDto(String sid, String content, String recipientID, String senderId,
                            String conversationId) {
        this(PackageType.MESSAGE, sid, content, recipientID, senderId,
                conversationId);
    }
}
