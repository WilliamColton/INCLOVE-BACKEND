package forfun.williamcolton.c.inclove.dto.chat.req;

import forfun.williamcolton.c.inclove.enums.PackageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserStatusDto {

    PackageType packageType = PackageType.USER_STATUS;
    Boolean isOnline;
    Boolean isTyping;
    LocalDateTime lastActiveTime;
    String peerId;

}
