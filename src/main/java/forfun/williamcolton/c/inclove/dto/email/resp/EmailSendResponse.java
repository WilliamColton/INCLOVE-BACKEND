package forfun.williamcolton.c.inclove.dto.email.resp;

import java.time.LocalDateTime;

public record EmailSendResponse(String messageId, boolean accepted, LocalDateTime acceptedAt) {

}
