package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {

    private String messageId;
    private String content;
    private Long conversationId;
    private String senderId;
    private String recipientId;
    private LocalDateTime createdAt;

}
