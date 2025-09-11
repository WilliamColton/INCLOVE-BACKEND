package forfun.williamcolton.c.inclove.dto.chat.resp;

import lombok.Data;

@Data
public class AckDto {
    String sid;
    String conversationId;
    String senderID;
}
