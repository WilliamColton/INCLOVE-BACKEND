package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {

    private Long messageId;
    private String content;
    private Long conversationId;
    private Long senderId;
    private LocalDateTime createdAt;
    private Status status;

    public enum Status {SENT, DELIVERED, READ}

}
