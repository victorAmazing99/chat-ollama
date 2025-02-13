package ai.example.springai.service.impl;


import ai.example.springai.Pojo.PowerPoint;
import ai.example.springai.service.PptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    OllamaChatModel chatModel;

    @Autowired
    public PptServiceImpl(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String createPpt(String message) {
        String userMessageTemplate = """
        你是一个专业的PPT生成助手，请根据主题《%S》生成一个包含完整结构的JSON数据。
        要求：
        1. 必须包含以下字段：title（标题）、author（作者）、slides（幻灯片列表）。
        2. slides中的每张幻灯片必须包含slideTitle（页标题）、content（内容要点列表，至少3点）。
        3. 使用中文生成内容，内容要简洁专业。
        4. 返回的JSON格式必须严格符合以下示例：
        
        示例：
        {{
          "title": "人工智能发展报告",
          "author": "张伟",
          "slides": [
            {{
              "slideTitle": "引言",
              "content": ["人工智能定义", "发展历史", "应用领域"]
            }},
            {{
              "slideTitle": "核心技术",
              "content": ["机器学习", "深度学习", "自然语言处理"]
            }}
          ]
        }}
        """;

        var outputConverter = new BeanOutputConverter<>(PowerPoint.class);
        OllamaOptions options = OllamaOptions.builder()
                .format(outputConverter.getJsonSchema()) // 强制指定JSON格式
                .temperature(0.3) // 降低随机性
                .numPredict(4096) // 允许更长的输出
                .build();

        Prompt prompt = new Prompt(
                String.format(userMessageTemplate, message),
                options
        );

        String rawJson = chatModel.call(prompt).getResult().getOutput().getText();

        // 后处理：检查JSON完整性（可选）
        if (!isValidJson(rawJson)) {
            logger.warn("Incomplete JSON detected: {}", rawJson);
            // 可添加重试逻辑或抛出异常
        }

        return rawJson;
    }

    // 简单的JSON完整性检查（示例）
    private boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
