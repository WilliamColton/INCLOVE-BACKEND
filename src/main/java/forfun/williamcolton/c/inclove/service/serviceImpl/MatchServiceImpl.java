package forfun.williamcolton.c.inclove.service.serviceImpl;

import forfun.williamcolton.c.inclove.entity.UserProfile;
import forfun.williamcolton.c.inclove.service.MatchService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    private final MilvusClientV2 milvusClientV2;

    @Value("${milvus.dbName}")
    private String dbName;
    @Value("${milvus.queryCollectionName}")
    private String queryCollectionName;
    @Value("${milvus.profileCollectionName}")
    private String profileCollectionName;

    public MatchServiceImpl(MilvusClientV2 milvusClientV2) {
        this.milvusClientV2 = milvusClientV2;
    }

    @Override
    public List<UserProfile> match(String userId) {
        var q = milvusClientV2.query(io.milvus.v2.service.vector.request.QueryReq.builder()
                .collectionName(queryCollectionName)
                .filter("user_id == \"" + userId + "\"")
                .outputFields(List.of("embedding"))
                .limit(1)
                .build());

        List<QueryResp.QueryResult> rows = q.getQueryResults();
        if (rows == null || rows.isEmpty()) return List.of();
        log.info("{}", rows);

        FloatVec searchVector = new FloatVec((List<Float>) rows.getFirst().getEntity().get("embedding"));
        var searchReq = SearchReq.builder()
                .collectionName(profileCollectionName)
                .data(Collections.singletonList(searchVector))
                .topK(3)
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        SearchResp searchResp = milvusClientV2.search(searchReq);

        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        for (List<SearchResp.SearchResult> results : searchResults) {
            log.info("TopK results:");
            for (SearchResp.SearchResult result : results) {
                log.info(String.valueOf(result));
            }
        }

        return null;
    }

}
