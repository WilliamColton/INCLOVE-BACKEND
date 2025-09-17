package forfun.williamcolton.c.inclove.dto.chat.resp;

import forfun.williamcolton.c.inclove.enums.PackageType;

public record ReturnMessageDto(PackageType packageType, String sid, String content, String recipientID) {
    public ReturnMessageDto(String sid, String content, String recipientID) {
        this(PackageType.MESSAGE, sid, content, recipientID);
    }
}
