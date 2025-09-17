package forfun.williamcolton.c.inclove.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationRespDto {

    String conversationId;
    String firstUserId;
    String SecondUserId;

}
