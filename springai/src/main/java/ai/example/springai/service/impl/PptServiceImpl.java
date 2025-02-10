package ai.example.springai.service.impl;


import ai.example.springai.Pojo.PowerPoint;
import ai.example.springai.service.PptService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Map;

@Service
public class PptServiceImpl implements PptService {

    OllamaChatModel chatModel;

    @Autowired
    public PptServiceImpl(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String createPpt(String message) {


         String  userMessage = """
                 生成以《{0}》为主题的中文ppt的Json格式数据
                 """;

       //  String str = MessageFormat.format(userMessage, message);
        var outputConverter = new BeanOutputConverter<>(PowerPoint.class);
        OllamaOptions options = OllamaOptions.builder()
                .format(outputConverter.getJsonSchema())
                .build();


        return chatModel.call(new Prompt( MessageFormat.format(userMessage, message), options)).getResult().getOutput().getText();
    }

}
