package forfun.williamcolton.c.inclove.dto.email;

import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class EmailSendResponse {

    String messageId;
    boolean accepted;
    LocalDateTime acceptedAt;

}
