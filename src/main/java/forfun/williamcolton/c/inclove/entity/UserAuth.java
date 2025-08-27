package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAuth {

    private Long id;
    private String email;
    private String userId;
    private String encodedPassword;
    private Boolean verified;
    private String verificationCode;    //use string to prevent loss of leading 0
    private LocalDateTime createdAt;

}
