package ai.example.springai.controller;


import ai.example.springai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@CrossOrigin
@RestController
@RequestMapping(value = "/springaiChat")
public class ChatController {

    @Autowired
    ChatService service;

    /**
     * 基础的模型访问
     */
    @RequestMapping("/message")
    public String sendMessage(@RequestParam(value = "message") String message) {

        return service.sendMessage(message);
    }

    /**
     * 带记忆的基础访问
     * @param message
     * @return
     */
    @RequestMapping("/message2")
    public String sendMessageWithHistory(@RequestParam(value = "sessionId",defaultValue = "1") String sessionId, @RequestParam(value = "message") String message) {
        return  service.sendMessageWithHistory(sessionId,message);
    }

    /**
     * 流式访问
     * @param message
     * @return
     */
    @RequestMapping(value = "/message3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return service.generateStream(message);
    }

    
    @Operation(summary = "Rag问答文档")
    @GetMapping(value = "/chat")
    public ResponseEntity chat(@RequestParam String message) {
        return ResponseEntity.ok(service.ragChat(message));
    }


    @Operation(summary = "Rag记忆问答")
    @RequestMapping(value="/chat2",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatRag(@RequestParam("uuid")String uuid,@RequestParam("message") String message) {
        if(uuid == null){
            uuid = "1";
        }
        return service.chatRag(uuid,message);
    }

    @Operation(summary = "图片解答")
    @PostMapping("/chatImage")
    public ResponseEntity chatImage( @RequestParam MultipartFile file, @RequestParam String message) {
        return ResponseEntity.ok(service.chatImage(file,message));
    }

    @Operation(summary = "测试mcp")
    @GetMapping("/testMcp")
    public ResponseEntity testMcp(){
        return ResponseEntity.ok(service.testChatClient());
    }


}
