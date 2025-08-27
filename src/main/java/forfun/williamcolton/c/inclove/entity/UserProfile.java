package forfun.williamcolton.c.inclove.entity;

import forfun.williamcolton.c.inclove.enums.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfile {

    private Long id;
    private String userId;
    private String userName;
    private Gender gender;
    private LocalDate birthday;
    private String intro;
    private LocalDateTime lastActiveAt;
    private LocalDateTime updateAt;

}
