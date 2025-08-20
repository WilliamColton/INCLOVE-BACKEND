package forfun.williamcolton.c.inclove.dto.auth;

import lombok.Value;

@Value
public class LoginDto {

    String email;
    String userId;
    String rawPassword;

}
