package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import forfun.williamcolton.c.inclove.dto.interest.req.CreateInterestsDto;
import forfun.williamcolton.c.inclove.dto.interest.resp.GetAllInterestsResp;
import forfun.williamcolton.c.inclove.entity.UserInterest;
import forfun.williamcolton.c.inclove.mapper.UserInterestMapper;
import forfun.williamcolton.c.inclove.service.InterestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class InterestServiceImpl extends BaseServiceImpl<UserInterestMapper, UserInterest> implements InterestService {

    @Override
    public void createInterests(CreateInterestsDto createInterestsDto, String userId) {
        var time = LocalDateTime.now();
        ArrayList<UserInterest> userInterests = new ArrayList<>();
        for (String interest : createInterestsDto.interestNames()) {
            var userInterest = new UserInterest();
            userInterest.setUserId(userId);
            userInterest.setInterest(interest);
            userInterest.setCreatedAt(time);
            userInterests.add(userInterest);
        }
        saveBatch(userInterests);
    }

    @Override
    public GetAllInterestsResp getAllInterests(String userId) {
        var result = list(new LambdaQueryWrapper<UserInterest>().eq(UserInterest::getUserId, userId)).stream().map(UserInterest::getInterest).toList();
        return new GetAllInterestsResp(result);
    }

}
