package ai.example.chatollama.springai.controller;


import ai.example.chatollama.springai.service.SpringAiService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/springChat")
public class SpringAIController {

    @Autowired
    SpringAiService service;

    @RequestMapping("/message")
    public String sendMessage(@RequestParam(value = "message") String message) {

         return service.sendMessage(message);
    }


    @RequestMapping("/message2")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return service.generateStream(message);
    }

    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public ResponseEntity upload(@RequestBody MultipartFile file) {
        service.uploadDocument(file);
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "搜索文档")
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDoc(@RequestParam String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }


    @Operation(summary = "Rag问答文档")
    @GetMapping("/chat")
    public ResponseEntity chat(@RequestParam String message) {
        return ResponseEntity.ok(service.ragChat(message));
    }



}
