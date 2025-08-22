package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import forfun.williamcolton.c.inclove.component.TextBuilder;
import forfun.williamcolton.c.inclove.dto.preference.req.CreatePreferenceDto;
import forfun.williamcolton.c.inclove.entity.UserPreference;
import forfun.williamcolton.c.inclove.mapper.UserPreferenceMapper;
import forfun.williamcolton.c.inclove.service.PreferenceService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PreferenceServiceImpl extends BaseServiceImpl<UserPreferenceMapper, UserPreference> implements PreferenceService {

    private final ModelMapper modelMapper;
    private final MilvusClientV2 milvusClientV2;
    private final OpenAIClient openAIClient;
    private final TextBuilder textBuilder;
    @Value("${embeddings.model}")
    private String model;
    @Value("${embeddings.vectorDimension}")
    private Long vectorDimension;
    @Value("${milvus.queryCollectionName}")
    private String queryCollectionName;

    public PreferenceServiceImpl(ModelMapper modelMapper, MilvusClientV2 milvusClientV2, OpenAIClient openAIClient, TextBuilder textBuilder) {
        this.modelMapper = modelMapper;
        this.milvusClientV2 = milvusClientV2;
        this.openAIClient = openAIClient;
        this.textBuilder = textBuilder;
    }

    @Override
    public void createPreference(CreatePreferenceDto createPreferenceDto, String userId) {
        var userPreference = modelMapper.map(createPreferenceDto, UserPreference.class);
        userPreference.setUserId(userId);
        userPreference.setUpdatedAt(LocalDateTime.now());
        saveOrUpdate(userPreference, new LambdaUpdateWrapper<UserPreference>().eq(UserPreference::getUserId, userId));
        String embeddingsText = textBuilder.buildQueryText(userPreference.getPreferredTraits(), userPreference.getPreferredHobbies());
        EmbeddingCreateParams createParams = EmbeddingCreateParams.builder().input(embeddingsText).model(model).dimensions(vectorDimension).build();
        var resp = openAIClient.embeddings().create(createParams);
        log.info(String.valueOf(resp));

        var data = new JsonObject();
        data.addProperty("user_id", userId);
        data.add("embedding", new Gson().toJsonTree(resp.data().getFirst().embedding()));

        milvusClientV2.upsert(UpsertReq.builder()
                .collectionName(queryCollectionName)
                .data(List.of(data))
                .build());
    }

}
