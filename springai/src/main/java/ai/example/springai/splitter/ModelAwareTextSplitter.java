package ai.example.springai.splitter;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ModelAwareTextSplitter extends TextSplitter {

    private final OllamaChatModel chatModel;

    @Autowired
    public ModelAwareTextSplitter(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public List<String> split(String text) {
        // 调用模型识别分割点（示例提示词）
        String prompt = """
            请仔细分析以下文本，识别出自然段落或语义边界，并在每个分割点前用 <split> 标记。
            
            例如：
            这是一个段落。<split>这是另一个段落。<split>这是第三个段落。
            
            文本如下：
            %s
            """.formatted(text);
        String response = chatModel.call(prompt.replace("${text}", text));

        // 解析模型返回的分割点
        return parseSplitPoints(response);
    }

    private List<String> parseSplitPoints(String response) {
        // 解析逻辑（如按 <split> 分割）
        return Arrays.stream(response.split("<split>"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    protected List<String> splitText(String text) {
        return split(text);
    }

}
