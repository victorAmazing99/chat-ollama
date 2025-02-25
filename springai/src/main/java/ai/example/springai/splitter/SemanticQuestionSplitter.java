package ai.example.springai.splitter;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SemanticQuestionSplitter {

    private final OllamaChatModel chatModel;

    @Autowired
    public SemanticQuestionSplitter(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public List<String> splitQuestions(String question) {
        // 构建提示词
        String prompt = """
            请仔细分析以下问题，并将其分割成多个语义相关的子问题。每个子问题前用 <split> 标记。
            
            例如：
            问题：如何设计一个高性能的数据库系统？如何优化查询性能？如何确保数据安全？
            分割后的子问题：<split>如何设计一个高性能的数据库系统？<split>如何优化查询性能？<split>如何确保数据安全？
            
            用户的问题：
             %s
            """.formatted(question);

        // 调用模型识别分割点
        String response = chatModel.call(prompt);

        // 解析模型返回的分割点
        return parseSplitPoints(response);
    }

    private List<String> parseSplitPoints(String response) {
        // 按 <split> 分割并去除空字符串
        return Arrays.stream(response.split("<split>"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}