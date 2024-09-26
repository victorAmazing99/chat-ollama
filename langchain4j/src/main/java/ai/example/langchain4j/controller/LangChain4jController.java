package ai.example.langchain4j.controller;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.spring.AiService;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/langchain4jChat")
public class LangChain4jController {

    OllamaChatModel chatModel;

    OllamaEmbeddingModel embeddingModel;

    Assistant assistant;

    LangChain4jController(OllamaChatModel chatModel, OllamaEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
    @AiService
    interface Assistant {

        String chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }



    /**
     * 聊天
     * @param message
     * @return
     */
    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return chatModel.generate(new SystemMessage("请全部用英语回答"),new UserMessage(message)).content().text();
    }

    /**
     * 有记忆的问答
     * @param message
     * @return
     */
    @GetMapping("/assistant2")
    public String assistant2(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
      ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
      chatMemory.add(new UserMessage(message));
      String  result = chatModel.generate(chatMemory.messages()).content().text();
      chatMemory.add(new AiMessage(result));
      return result;
    }

    @GetMapping("/assistant3")
    public String assistant3(@RequestParam(value = "memoryId", defaultValue = "1")String memoryId,@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {

        String result = assistant.chat(memoryId,message);

        return result;
    }


    /**
     * 生成embedding 属据
     * @param message
     * @return
     */
    @GetMapping("/embd")
    public String embd(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return embeddingModel.embed(message).content().toString();
    }

}
