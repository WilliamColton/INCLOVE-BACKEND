package forfun.williamcolton.c.inclove.dto.preference.req;

import forfun.williamcolton.c.inclove.enums.Gender;

import java.util.Set;

public record CreatePreferenceDto(Integer preferredAgeMin,
                                  Integer preferredAgeMax,
                                  Set<Gender> acceptedGenders,
                                  Set<String> preferredTraits,
                                  Set<String> preferredHobbies) {
}
