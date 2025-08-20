package forfun.williamcolton.c.inclove.entity;

import lombok.Data;

@Data
public class UserAuth {

    Long id;
    String email;
    String userId;
    String encodedPassword;
    Boolean verified;
    String verificationCode;    //use string to prevent loss of leading 0

}
