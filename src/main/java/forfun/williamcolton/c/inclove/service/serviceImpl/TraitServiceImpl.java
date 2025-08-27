package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import forfun.williamcolton.c.inclove.dto.trait.req.CreateTraitsDto;
import forfun.williamcolton.c.inclove.dto.trait.resp.GetAllTraitsResp;
import forfun.williamcolton.c.inclove.entity.UserTrait;
import forfun.williamcolton.c.inclove.mapper.UserTraitMapper;
import forfun.williamcolton.c.inclove.service.TraitService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class TraitServiceImpl extends BaseServiceImpl<UserTraitMapper, UserTrait> implements TraitService {

    @Override
    public void createTraits(CreateTraitsDto createTraitsDto, String userId) {
        var time = LocalDateTime.now();
        ArrayList<UserTrait> userTraits = new ArrayList<>();
        for (String trait : createTraitsDto.traitNames()) {
            var userTrait = new UserTrait();
            userTrait.setUserId(userId);
            userTrait.setTrait(trait);
            userTrait.setCreatedAt(time);
            userTraits.add(userTrait);
        }
        saveBatch(userTraits);
    }

    @Override
    public GetAllTraitsResp getAllTraits(String userId) {
        var result = list(new LambdaQueryWrapper<UserTrait>().eq(UserTrait::getUserId, userId)).stream().map(UserTrait::getTrait).toList();
        return new GetAllTraitsResp(result);
    }

}
