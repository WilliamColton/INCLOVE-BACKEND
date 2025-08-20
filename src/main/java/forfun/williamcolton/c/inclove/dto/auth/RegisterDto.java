package forfun.williamcolton.c.inclove.dto.auth;

import lombok.Value;

@Value
public class RegisterDto {

    String email;
    String userId;
    String rawPassword;

}
