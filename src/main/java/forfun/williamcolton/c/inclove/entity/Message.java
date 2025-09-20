package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {

    private String id;
    private String content;
    private String conversationId;
    private String senderId;
    private String recipientId;
    private LocalDateTime createdAt;

}
