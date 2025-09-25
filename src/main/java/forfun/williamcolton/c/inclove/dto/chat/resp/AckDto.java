package forfun.williamcolton.c.inclove.dto.chat.resp;

import forfun.williamcolton.c.inclove.enums.PackageType;
import lombok.Data;

@Data
public class AckDto {
    PackageType packageType = PackageType.ACK;
    String sid;
    String conversationId;
    String senderID;
}
