package ai.example.langchain4j.controller;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/langchain4jChat")
public class LangChain4jController {
    OllamaChatModel chatModel;
    OllamaEmbeddingModel embeddingModel;
    LangChain4jController(OllamaChatModel chatModel, OllamaEmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return chatModel.generate(new SystemMessage("请全部用英语回答"),new UserMessage(message)).content().text();
    }

    @GetMapping("/embd")
    public String embd(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return embeddingModel.embed(message).content().toString();
    }

}
