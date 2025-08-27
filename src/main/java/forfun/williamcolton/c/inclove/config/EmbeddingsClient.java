package forfun.williamcolton.c.inclove.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingsClient {

    @Value("${embeddings.apiKey}")
    private String apiKey;

    @Value("${embeddings.baseUrl}")
    private String baseUrl;

    @Bean
    public OpenAIClient creatEmbeddingsClient() {
        return new OpenAIOkHttpClient.Builder().apiKey(apiKey).baseUrl(baseUrl).build();
    }

}
