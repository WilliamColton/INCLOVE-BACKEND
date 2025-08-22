package forfun.williamcolton.c.inclove.dto.email.resp;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class EmailSendResponse {

    String messageId;
    boolean accepted;
    LocalDateTime acceptedAt;

}
