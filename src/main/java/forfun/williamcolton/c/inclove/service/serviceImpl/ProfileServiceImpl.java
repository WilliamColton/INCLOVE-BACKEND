package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import forfun.williamcolton.c.inclove.component.TextBuilder;
import forfun.williamcolton.c.inclove.dto.profile.req.CreateProfileDto;
import forfun.williamcolton.c.inclove.entity.UserProfile;
import forfun.williamcolton.c.inclove.mapper.UserProfileMapper;
import forfun.williamcolton.c.inclove.repository.MilvusProfileRepository;
import forfun.williamcolton.c.inclove.service.InterestService;
import forfun.williamcolton.c.inclove.service.ProfileService;
import forfun.williamcolton.c.inclove.service.TraitService;
import forfun.williamcolton.c.inclove.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ProfileServiceImpl extends BaseServiceImpl<UserProfileMapper, UserProfile> implements ProfileService {

    private final ModelMapper modelMapper;
    private final OpenAIClient openAIClient;
    private final TextBuilder textBuilder;
    private final InterestService interestService;
    private final TraitService traitService;
    private final MilvusProfileRepository milvusProfileRepository;
    @Value("${embeddings.model}")
    private String model;
    @Value("${embeddings.vectorDimension}")
    private Long vectorDimension;
    @Value("${milvus.profileCollectionName}")
    private String profileCollectionName;

    public ProfileServiceImpl(ModelMapper modelMapper, OpenAIClient openAIClient, TextBuilder textBuilder, InterestService interestService, TraitService traitService, MilvusProfileRepository milvusProfileRepository) {
        this.modelMapper = modelMapper;
        this.openAIClient = openAIClient;
        this.textBuilder = textBuilder;
        this.interestService = interestService;
        this.traitService = traitService;
        this.milvusProfileRepository = milvusProfileRepository;
    }

    @Override
    public void createProfile(CreateProfileDto createProfileDto, String userId) {
        var profile = modelMapper.map(createProfileDto, UserProfile.class);
        profile.setUserId(userId);
        saveOrUpdate(profile, new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));

        String embeddingsText = textBuilder.buildProfileText(profile, interestService.getAllInterests(userId).interestNames(), traitService.getAllTraits(userId).traitNames());
        EmbeddingCreateParams createParams = EmbeddingCreateParams.builder().input(embeddingsText).model(model).dimensions(vectorDimension).build();
        var resp = openAIClient.embeddings().create(createParams);
        log.info(resp.toString());
        milvusProfileRepository.upsert(profileCollectionName,
                userId,
                resp.data().getFirst().embedding(),
                profile.getGender(),
                TimeUtil.countAge(profile.getBirthday(), LocalDate.now()),
                LocalDateTime.now().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli(),
                LocalDateTime.now().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        );
    }

}
