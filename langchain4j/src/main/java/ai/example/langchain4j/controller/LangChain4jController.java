package ai.example.langchain4j.controller;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/langchain4jChat")
public class LangChain4jController {
    OllamaChatModel chatModel;

    LangChain4jController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return chatModel.generate(new SystemMessage("请全部用英语回答"),new UserMessage(message)).content().text();
    }

}
