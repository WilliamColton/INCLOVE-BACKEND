package forfun.williamcolton.c.inclove.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import forfun.williamcolton.c.inclove.enums.Gender;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MilvusProfileRepository {

    private final MilvusClientV2 milvusClientV2;

    public MilvusProfileRepository(MilvusClientV2 milvusClientV2) {
        this.milvusClientV2 = milvusClientV2;
    }

    public void upsert(String collectionName, String userId,
                       List<Float> embedding,
                       Gender gender,
                       Integer age,
                       Long lastActiveAt,
                       Long updateAt) {

        var data = new JsonObject();
        data.addProperty("user_id", userId);
        data.add("embedding", new Gson().toJsonTree(embedding));
        data.addProperty("gender", gender.name());
        data.addProperty("age", age);
        data.addProperty("last_active_at", lastActiveAt);
        data.addProperty("update_at", updateAt);

        milvusClientV2.upsert(UpsertReq.builder()
                .collectionName(collectionName)
                .data(List.of(data))
                .build());
    }

}
