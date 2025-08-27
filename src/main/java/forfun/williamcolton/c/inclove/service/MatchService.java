package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.entity.UserProfile;

import java.util.List;

public interface MatchService {

    List<UserProfile> match(String userId);

}
