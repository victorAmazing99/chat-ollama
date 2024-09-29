package ai.example.langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoStoreConfig {

    private final StoreProperties storeProperties;

    public AutoStoreConfig(StoreProperties storeProperties) {
        this.storeProperties = storeProperties;
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return   RedisEmbeddingStore.builder()
                .host(storeProperties.getRedis().getHost())
                .port(storeProperties.getRedis().getPort())
                .dimension(384)
                .build();
    }


}
