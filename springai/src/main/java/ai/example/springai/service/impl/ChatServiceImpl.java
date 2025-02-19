package ai.example.springai.service.impl;

import ai.example.springai.service.ChatService;
import ai.example.springai.service.RagService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final String SYSTEM_PROMPT_TEMPLATE ="""
    你是一个基于文档的问答助手。请严格根据以下提供的文档内容回答问题：
    {0}
    
    注意：
    1. 只能使用文档中的信息，禁止使用其他知识。
    2. 如果文档中没有相关信息，请回复“文档中没有相关信息”。
    3. 使用简体中文回答。
    """;

    private static final String IMAGE_PROMPT_PREFIX = "使用中文回答:";

    private final OllamaChatModel chatModel;
    private final RagService ragService;
    private final Map<String, List<Message>> chatHistory;

    @Value("${chat.history.max-size:10}")
    private int maxHistorySize;

    @Value("${ollama.model:llama3.8b}")
    private String defaultModel;

    @Autowired
    public ChatServiceImpl(OllamaChatModel chatModel, RagService ragService) {
        this.chatModel = chatModel;
        this.ragService = ragService;
        this.chatHistory = new ConcurrentHashMap<>();
    }

    @Override
    public String sendMessage(String message) {
        SystemMessage systemMessage = new SystemMessage("使用中文回答");
        Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(message)),
                OllamaOptions.builder().temperature(0.4).numGPU(0).build());
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @Override
    public String sendMessageWithHistory(String sessionId, String message) {
        List<Message> history = getOrCreateHistory(sessionId);
        addUserMessage(history, message);

        ChatResponse response = chatModel.call(new Prompt(history));
        String answer = response.getResult().getOutput().getText();

        addAssistantMessage(history, answer);
        return answer;
    }

    @Override
    public Flux<ChatResponse> chatRag(String sessionId, String message) {
        List<Message> history = getOrCreateHistory(sessionId);
        addUserMessage(history, message);

        List<Document> documents = ragService.search(message);
//        if (documents.isEmpty()) {
//            return Flux.just("文档中没有相关信息");
//        }

        String context = buildContext(documents);
        List<Message> promptMessages = buildRagPrompt(history, context);

        return chatModel.stream(new Prompt(promptMessages))
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    if (response.getResult().getOutput() instanceof AssistantMessage) {
                        addAssistantMessage(history, content);
                    }
                });
    }

    @Override
    public String chatImage(MultipartFile file, String message) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            Media imageMedia = Media.builder().data(base64Image).mimeType(MimeTypeUtils.ALL).build();
            UserMessage userMessage = new UserMessage(IMAGE_PROMPT_PREFIX + message, imageMedia);

            return chatModel.call(new Prompt(List.of(userMessage)))
                    .getResult()
                    .getOutput()
                    .getText();
        } catch (Exception e) {
            logger.error("图片处理失败", e);
            throw new RuntimeException("图片处理失败", e);
        }
    }

    @Override
    public Flux<ChatResponse> generateStream(String message) {
        return chatModel.stream(new Prompt(new UserMessage(message)));
    }

    @Override
    public String ragChat(String message) {
        List<Document> documents = ragService.search(message);
        if (documents.isEmpty()) {
            return "文档中没有相关信息";
        }

        String context = buildContext(documents);
        SystemMessage systemMessage = new SystemMessage(MessageFormat.format(SYSTEM_PROMPT_TEMPLATE, context));

        Prompt prompt = new Prompt(
                List.of(systemMessage, new UserMessage(message)),
                ChatOptions.builder()
                        .model(defaultModel)
                        .maxTokens(1024)
                        .build()
        );

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private List<Message> getOrCreateHistory(String sessionId) {
        return chatHistory.compute(sessionId, (k, v) -> {
            if (v == null) return Collections.synchronizedList(new ArrayList<>());
            if (v.size() > maxHistorySize) {
                return v.subList(v.size() - maxHistorySize, v.size());
            }
            return v;
        });
    }

    private void addUserMessage(List<Message> history, String message) {
        history.add(new UserMessage(message));
    }

    private void addAssistantMessage(List<Message> history, String message) {
        history.add(new AssistantMessage(message));
    }

    private String buildContext(List<Document> documents) {
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
    }

    private List<Message> buildRagPrompt(List<Message> history, String context) {
        List<Message> promptMessages = new ArrayList<>(history);
        promptMessages.add(0, new SystemMessage(MessageFormat.format(SYSTEM_PROMPT_TEMPLATE, context)));
        return promptMessages;
    }
}
