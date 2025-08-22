package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.profile.req.CreateProfileDto;
import forfun.williamcolton.c.inclove.entity.UserProfile;

public interface ProfileService extends IBaseService<UserProfile> {

    void createProfile(CreateProfileDto createProfileDto, String userId);

}
