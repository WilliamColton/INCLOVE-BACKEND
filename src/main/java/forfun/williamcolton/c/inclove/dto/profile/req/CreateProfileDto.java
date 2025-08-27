package forfun.williamcolton.c.inclove.dto.profile.req;

import forfun.williamcolton.c.inclove.enums.Gender;

import java.time.LocalDate;

public record CreateProfileDto(String userName, Gender gender, LocalDate birthday, String intro) {

}
