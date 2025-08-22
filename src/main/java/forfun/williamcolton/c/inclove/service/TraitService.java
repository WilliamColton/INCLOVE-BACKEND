package forfun.williamcolton.c.inclove.service;

import forfun.williamcolton.c.inclove.dto.trait.req.CreateTraitsDto;
import forfun.williamcolton.c.inclove.dto.trait.resp.GetAllTraitsResp;
import forfun.williamcolton.c.inclove.entity.UserTrait;

public interface TraitService extends IBaseService<UserTrait> {

    void createTraits(CreateTraitsDto createTraitsDto, String userId);

    GetAllTraitsResp getAllTraits(String userId);

}
