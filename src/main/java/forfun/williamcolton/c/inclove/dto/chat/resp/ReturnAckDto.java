package forfun.williamcolton.c.inclove.dto.chat.resp;

import forfun.williamcolton.c.inclove.enums.PackageType;

public record ReturnAckDto(PackageType packageType, String sid, String conversationId) {
    public ReturnAckDto(String sid, String conversationId) {
        this(PackageType.ACK, sid, conversationId);
    }
}

