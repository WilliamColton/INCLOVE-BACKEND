package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTrait {

    private Long id;
    private String userId;
    private String trait;
    private LocalDateTime createdAt;

}
