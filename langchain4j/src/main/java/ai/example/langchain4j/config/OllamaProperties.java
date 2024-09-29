package ai.example.langchain4j.config;

//@Component
//@ConfigurationProperties(prefix = "langchain4j.ollama")
public class OllamaProperties {

    private ChatModelProperties chatModel;
    private StreamingChatModelProperties streamingChatModel;
    private EmbeddingModelProperties embeddingModel;

    public ChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(ChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public StreamingChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(StreamingChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public EmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public static class ChatModelProperties {
        private String modelName;
        private String baseUrl;
        private int topK;
        private double topP;
        private double temperature;

        // Getters and Setters
        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getTopP() {
            return topP;
        }

        public void setTopP(double topP) {
            this.topP = topP;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    public static class StreamingChatModelProperties extends ChatModelProperties {
        // Inherit properties from ChatModelProperties
    }

    public static class EmbeddingModelProperties {
        private String modelName;
        private String baseUrl;

        // Getters and Setters
        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
