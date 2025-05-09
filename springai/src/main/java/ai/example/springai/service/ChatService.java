package ai.example.springai.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    public String sendMessage(String message);

    Flux<ChatResponse> generateStream(String message);

    String ragChat(String message);

    String sendMessageWithHistory(String sessionId,String message);

    Flux<ChatResponse> chatRag(String uuid, String message);

    String chatImage(MultipartFile file, String message);

    String testChatClient();
}
