package forfun.williamcolton.c.inclove.component;

import forfun.williamcolton.c.inclove.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class TextBuilder {

    public String buildProfileText(UserProfile userProfile, List<String> interests, List<String> traits) {
        return "Self-introduction:" + nz(userProfile.getIntro()) + "\n" + "Interests:" + join(interests) + "\n" + "Traits:" + join(traits);
    }

    public String buildQueryText(Set<String> preferredTraits,
                                 Set<String> preferredHobbies) {
        return "Preferred partner traits:" + join(preferredTraits) + "\n"
                + "Preferred hobbies:" + join(preferredHobbies) + "\n";
    }

    private String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private String join(Collection<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().collect(java.util.stream.Collectors.joining("，"));
    }

}
