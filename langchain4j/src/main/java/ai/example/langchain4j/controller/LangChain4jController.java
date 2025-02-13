package ai.example.langchain4j.controller;

import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.spring.AiService;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/langchain4jChat")
public class LangChain4jController {

    OllamaChatModel chatModel;

    OllamaEmbeddingModel embeddingModel;

    OllamaStreamingChatModel streamingChatModel;

    Assistant assistant;

    SteamAssistant steamAssistant;


    LangChain4jController(OllamaChatModel chatModel, OllamaEmbeddingModel embeddingModel, OllamaStreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.streamingChatModel = streamingChatModel;
        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
        this.steamAssistant = AiServices.builder(SteamAssistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    @AiService
    interface Assistant {

        String chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }

    @AiService
    interface SteamAssistant {

        void chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }


    /**
     * 聊天
     *
     * @param message
     * @return
     */
    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return chatModel.generate(new SystemMessage("请全部用英语回答"), new UserMessage(message)).content().text();
    }

    /**
     * 流式访问
     *
     * @return
     */
    @GetMapping(value = "/steamAssistant", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> steamAssistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return Flux.create(emitter -> {
            streamingChatModel.generate(message, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    emitter.next(token); // 发送
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    emitter.complete(); // 完成流
                }

                @Override
                public void onError(Throwable error) {
                    emitter.error(error);
                }
            });
        });
    }


    /**
     * 有记忆的问答
     *
     * @param message
     * @return
     */
    @GetMapping("/assistant2")
    public String assistant2(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        chatMemory.add(new UserMessage(message));
        String result = chatModel.generate(chatMemory.messages()).content().text();
        chatMemory.add(new AiMessage(result));
        return result;
    }


    @PostMapping("/assistant4")

    public String assistant4(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "message", defaultValue = "What is the time now?") String message) {

        String Base64Image = null;
        try {
            Base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Base64Image != null) {
            List<Content> contents = new java.util.ArrayList<>();
            contents.add(new TextContent(message));
            // 文件的格式
            String mimeType="jpg";
            contents.add(new ImageContent(Base64Image,mimeType));

            return chatModel.generate(new UserMessage(contents)).content().text();
        }


        return "success";
    }


    @GetMapping("/assistant3")
    public String assistant3(@RequestParam(value = "memoryId", defaultValue = "1") String memoryId, @RequestParam(value = "message", defaultValue = "What is the time now?") String message) {

        String result = assistant.chat(memoryId, message);

        return result;
    }


    /**
     * 生成embedding 属据
     *
     * @param message
     * @return
     */
    @GetMapping("/embd")
    public String embd(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return embeddingModel.embed(message).content().toString();
    }

}
