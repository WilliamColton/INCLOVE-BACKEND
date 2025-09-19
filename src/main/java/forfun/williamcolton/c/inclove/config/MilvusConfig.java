package forfun.williamcolton.c.inclove.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.exception.MilvusClientException;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class MilvusConfig {

    private static final String PK_FIELD = "user_id";
    private static final String FIELD_EMBEDDING = "embedding";
    private static final String FIELD_GENDER = "gender";
    private static final String FIELD_AGE = "age";
    private static final String FIELD_LAST_ACTIVE_AT = "last_active_at";
    private static final String FIELD_UPDATE_AT = "update_at";
    private static final Integer USERID_MAX_LENGTH = 100;
    private static final Integer GENDER_MAX_LENGTH = 100;

    @Value("${milvus.dbName}")
    private String dbName;

    @Value("${milvus.uri}")
    private String uri;

    @Value("${milvus.token}")
    private String token;

    @Value("${milvus.profileCollectionName}")
    private String profileCollectionName;

    @Value("${milvus.queryCollectionName}")
    private String queryCollectionName;

    @Value("${embeddings.vectorDimension}")
    private Integer vectorDimension;

    @Bean
    public MilvusClientV2 buildMilvusClient() {
        ConnectConfig connectConfig = ConnectConfig.builder().uri(uri).token(token).build();

        MilvusClientV2 client;
        try {
            client = new MilvusClientV2(connectConfig);
        } catch (Exception e) {
            throw new RuntimeException("MilvusClientV2 连接失败", e);
        }

        // check if there is a corresponding database
        List<String> dbNames = client.listDatabases().getDatabaseNames();
        if (!dbNames.contains(dbName)) {
            var createDatabaseReq = CreateDatabaseReq.builder().databaseName(dbName).build();
            client.createDatabase(createDatabaseReq);
        }
        try {
            client.useDatabase(dbName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // check if there is a corresponding collection
        var hasProfileCollectionReq = HasCollectionReq.builder().collectionName(profileCollectionName).build();
        var hasProfileCollection = client.hasCollection(hasProfileCollectionReq);
        if (!hasProfileCollection) {
            createProfileCollection(client);
        }
        ensureProfileIndex(client);
        client.loadCollection(LoadCollectionReq.builder().collectionName(profileCollectionName).build());

        // check if there is a corresponding collection
        var hasQueryCollectionReq = HasCollectionReq.builder().collectionName(queryCollectionName).build();
        var hasQueryCollection = client.hasCollection(hasQueryCollectionReq);
        if (!hasQueryCollection) {
            createQueryCollection(client);
        }
        ensureQueryIndex(client);
        client.loadCollection(LoadCollectionReq.builder().collectionName(queryCollectionName).build());

        return client;
    }

    public void createProfileCollection(MilvusClientV2 client) {
        AddFieldReq userId = AddFieldReq.builder().fieldName(PK_FIELD).dataType(DataType.VarChar).maxLength(USERID_MAX_LENGTH).isPrimaryKey(true).autoID(false).build();
        AddFieldReq embedding = AddFieldReq.builder().fieldName(FIELD_EMBEDDING).dataType(DataType.FloatVector).dimension(vectorDimension).build();
        AddFieldReq gender = AddFieldReq.builder().fieldName(FIELD_GENDER).dataType(DataType.VarChar).maxLength(GENDER_MAX_LENGTH).build();
        AddFieldReq age = AddFieldReq.builder().fieldName(FIELD_AGE).dataType(DataType.Int32).build();
        AddFieldReq lastActiveAt = AddFieldReq.builder().fieldName(FIELD_LAST_ACTIVE_AT).dataType(DataType.Int64).build();
        AddFieldReq updateAt = AddFieldReq.builder().fieldName(FIELD_UPDATE_AT).dataType(DataType.Int64).build();

        var schema = MilvusClientV2.CreateSchema();
        schema.addField(userId).addField(embedding).addField(gender).addField(age).addField(lastActiveAt).addField(updateAt);

        var createCollectionReq = CreateCollectionReq.builder().collectionName(profileCollectionName).collectionSchema(schema).build();
        client.createCollection(createCollectionReq);
    }

    private void ensureProfileIndex(MilvusClientV2 client) {
        var describeIndexReq1 = DescribeIndexReq.builder().databaseName(dbName).collectionName(profileCollectionName).fieldName(PK_FIELD).build();
        var describeIndexReq2 = DescribeIndexReq.builder().databaseName(dbName).collectionName(profileCollectionName).fieldName(FIELD_EMBEDDING).build();

        // Check whether the index exists. If it does not exist, MilvusException will be thrown.
        try {
            client.describeIndex(describeIndexReq1);
            client.describeIndex(describeIndexReq2);
        } catch (MilvusClientException m) {
            log.error(String.valueOf(m));
            List<IndexParam> indexParams = new ArrayList<>();

            IndexParam idIndex = IndexParam.builder().fieldName(PK_FIELD).indexType(IndexParam.IndexType.AUTOINDEX).build();

            IndexParam vectorIndex = IndexParam.builder().fieldName(FIELD_EMBEDDING).indexType(IndexParam.IndexType.AUTOINDEX).metricType(IndexParam.MetricType.COSINE).build();

            indexParams.add(idIndex);
            indexParams.add(vectorIndex);

            client.createIndex(io.milvus.v2.service.index.request.CreateIndexReq.builder().collectionName(profileCollectionName).indexParams(indexParams).build());
        }
    }

    public void createQueryCollection(MilvusClientV2 client) {
        AddFieldReq userId = AddFieldReq.builder()
                .fieldName(PK_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(USERID_MAX_LENGTH)
                .isPrimaryKey(true)
                .autoID(false)
                .build();

        AddFieldReq embedding = AddFieldReq.builder()
                .fieldName(FIELD_EMBEDDING)
                .dataType(DataType.FloatVector)
                .dimension(vectorDimension)
                .build();

        var schema = MilvusClientV2.CreateSchema();
        schema.addField(userId).addField(embedding);

        var createCollectionReq = CreateCollectionReq.builder()
                .collectionName(queryCollectionName)
                .collectionSchema(schema)
                .build();
        client.createCollection(createCollectionReq);
    }

    private void ensureQueryIndex(MilvusClientV2 client) {
        var describeIndexReq1 = DescribeIndexReq.builder()
                .databaseName(dbName)
                .collectionName(queryCollectionName)
                .fieldName(PK_FIELD)
                .build();
        var describeIndexReq2 = DescribeIndexReq.builder()
                .databaseName(dbName)
                .collectionName(queryCollectionName)
                .fieldName(FIELD_EMBEDDING)
                .build();

        try {
            client.describeIndex(describeIndexReq1);
            client.describeIndex(describeIndexReq2);
        } catch (MilvusClientException m) {
            log.error(String.valueOf(m));
            List<IndexParam> indexParams = new ArrayList<>();

            IndexParam idIndex = IndexParam.builder()
                    .fieldName(PK_FIELD)
                    .indexType(IndexParam.IndexType.AUTOINDEX)
                    .build();

            IndexParam vectorIndex = IndexParam.builder()
                    .fieldName(FIELD_EMBEDDING)
                    .indexType(IndexParam.IndexType.AUTOINDEX)
                    .metricType(IndexParam.MetricType.COSINE)
                    .build();

            indexParams.add(idIndex);
            indexParams.add(vectorIndex);

            client.createIndex(
                    io.milvus.v2.service.index.request.CreateIndexReq.builder()
                            .collectionName(queryCollectionName)
                            .indexParams(indexParams)
                            .build()
            );
        }
    }

}
