package forfun.williamcolton.c.inclove.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRespDto {

    private String id;
    private String firstUserId;
    private String secondUserId;

}
