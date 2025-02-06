package ai.example.springai.service.impl;

import ai.example.springai.service.ChatService;
import ch.qos.logback.core.net.SyslogOutputStream;
import jakarta.annotation.Resource;
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
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    OllamaChatModel chatModel;

    @Autowired
    RagServiceImpl ragService;

    private static HashMap<String, List<Message>> chatHistory = new HashMap<>();

    private static Integer maxHistorySize = 10;

    @Autowired
    public ChatServiceImpl(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String sendMessage(String message) {

        SystemMessage systemMessage = new SystemMessage("使用中文回答");

        ChatResponse response = chatModel.call(new Prompt(message, OllamaOptions.create()
                //  .withModel("llama3:8b")   //可动态选择模型
                .withTemperature(0.4)));
        return response.getResult().getOutput().getContent();
    }

    @Override
    public String sendMessage2(String sessionId, String message) {
        //根据不同会话Id，获取历史记忆
        List<Message> chatHistorys = getHistory(sessionId);
        //将提问进行记忆
        chatHistorys.add(chatHistorys.size(), new UserMessage(message));
        ChatResponse response = chatModel.call(new Prompt(chatHistorys));
        chatHistorys.add(chatHistorys.size(), new AssistantMessage(response.getResult().getOutput().getContent()));
        return response.getResult().getOutput().getContent();
    }

    @Override
    public Flux<String> chatRag(String uuid, String message) {

        //根据不同会话Id，获取历史记忆
        List<Message> chatHistorys = getHistory(uuid);

        //查询获取文档信息
        List<Document> documents = ragService.search(message);

        //提取文本内容
        String content = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        if (content.length() == 0) {
            return Flux.just("文档中没有相关信息");
        }

        String systemInfo = """
                根据以下提供的文档使用简体中文回答问题:{content}
                不要使用其他知识。如果文档中没有答案，请回复'文档中没有相关信息'。
                """;

        chatHistorys.add(chatHistorys.size(), new UserMessage(message));

        List<Message> messages = new ArrayList<>();
        messages.addAll(chatHistorys);
        messages.add(new SystemMessage(String.format(systemInfo, content)));

        //将提问进行记忆
        Flux<String> result = chatModel.stream(new Prompt(messages))
                .map(response -> response.getResult().getOutput().getContent());

        StringBuilder resultString = new StringBuilder();
        List<Message> resultMessage = chatHistorys;
        // 每接收到一个字符串就追加到 StringBuilder
        result.doOnNext(o -> resultString.append(o))
                .doOnComplete(() -> {
                    // 当 Flux 完成时，添加 AssistantMessage 到 chatHistorys
                    resultMessage.add(new AssistantMessage(resultString.toString()));
                })
                .subscribe();

        return result;
    }

    @Override
    public String chatImage(MultipartFile file, String message) {
        String Base64Image = null;
        try {
            Base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Base64Image != null) {
            //  UserMessage userMessage = new UserMessage("使用中文回答");
            UserMessage userMessage = new UserMessage("使用中文回答:" + message, Media.builder().data(Base64Image).mimeType(MimeTypeUtils.ALL).build());
            List<Message> messages = new ArrayList<>();
            // messages.add(userMessage);
            messages.add(userMessage);
            return chatModel.call(new Prompt(messages)).getResult().getOutput().getContent().toString();
        }

        return null;
    }


    @Override
    public Flux<ChatResponse> generateStream(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

    @Override
    public String ragChat(String message) {
        //查询获取文档信息
        List<Document> documents = ragService.search(message);

        //提取文本内容
        String content = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        String systemInfo = """
                根据以下提供的文档使用简体中文回答问题:\n" + content +
                "不要使用其他知识。如果文档中没有答案，回复'文档中没有相关信息'。
                """;

        SystemMessage systemMessage = new SystemMessage(systemInfo);
        UserMessage userMessage = new UserMessage(message);
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        Prompt prompt = new Prompt(messages, ChatOptions.builder()
                .model("llama3.8b")
                .maxTokens(1024)
                .build());

        ChatResponse chatResponse = chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getContent();
    }


    private List<Message> getHistory(String uuid) {
        //根据不同会话Id，获取历史记忆
        List<Message> chatHistorys = chatHistory.get(uuid);
        //判断chatHistoryMap是否为空
        if (chatHistorys == null) {
            //如果为空,则创建一个会话list
            chatHistorys = new ArrayList<>();
            chatHistory.put(uuid, chatHistorys);
        } else {

            if (chatHistorys.size() > maxHistorySize) {
                chatHistorys = chatHistorys.subList(chatHistorys.size() - maxHistorySize - 1, chatHistorys.size());
            }
        }

        return chatHistorys;
    }

    public static void main(String[] args) {

        ChatMemory chatMemory = new InMemoryChatMemory();
        OllamaChatModel chatModel1 = OllamaChatModel.builder()
                .ollamaApi(new OllamaApi("http://192.168.0.26:11434"))
                .defaultOptions(OllamaOptions.builder().model("llava").build())
                .build();

        var chatClient = ChatClient.builder(chatModel1).defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory,"UUID",100)).build();
        String response =chatClient.prompt(new Prompt("Tell me a joke")).call().chatResponse().getResult().getOutput().getContent();

        System.out.println(response);
        System.out.println(chatMemory.get("UUID",100));
    }

}
