package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInterest {

    private Long id;
    private String userId;
    private String interest;
    private LocalDateTime createdAt;

}
