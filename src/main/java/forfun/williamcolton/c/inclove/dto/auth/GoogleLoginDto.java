package forfun.williamcolton.c.inclove.dto.auth;

import lombok.Value;

@Value
public class GoogleLoginDto {

    String userId;
    String idToken;
    String email;

}
