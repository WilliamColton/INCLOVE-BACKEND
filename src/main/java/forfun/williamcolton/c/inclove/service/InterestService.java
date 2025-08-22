package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.interest.req.CreateInterestsDto;
import forfun.williamcolton.c.inclove.dto.interest.resp.GetAllInterestsResp;
import forfun.williamcolton.c.inclove.entity.UserInterest;

public interface InterestService extends IBaseService<UserInterest> {

    void createInterests(CreateInterestsDto createInterestsDto, String userId);

    GetAllInterestsResp getAllInterests(String userId);

}
