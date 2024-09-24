package ai.example.langchain4j.cofing;


import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class OllamaConfig {

    private final OllamaProperties ollamaProperties;

    public OllamaConfig(OllamaProperties ollamaProperties) {
        this.ollamaProperties = ollamaProperties;
    }

    @Bean
    public OllamaChatModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaProperties.getChatModel().getBaseUrl())
                .modelName(ollamaProperties.getChatModel().getModelName())
                .topK(ollamaProperties.getChatModel().getTopK())
                .topP(ollamaProperties.getChatModel().getTopP())
                .temperature(ollamaProperties.getChatModel().getTemperature())
                .maxRetries(3)
                .build();
    }

    @Bean
    public OllamaStreamingChatModel ollamaStreamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaProperties.getStreamingChatModel().getBaseUrl())
                .modelName(ollamaProperties.getStreamingChatModel().getModelName())
                .topK(ollamaProperties.getStreamingChatModel().getTopK())
                .topP(ollamaProperties.getStreamingChatModel().getTopP())
                .temperature(ollamaProperties.getStreamingChatModel().getTemperature())
                .build();
    }

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaProperties.getEmbeddingModel().getBaseUrl())
                .modelName(ollamaProperties.getEmbeddingModel().getModelName())
                .maxRetries(3)
                .build();
    }

}
