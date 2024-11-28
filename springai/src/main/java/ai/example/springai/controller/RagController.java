package ai.example.springai.controller;


import ai.example.springai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/springaiRag")
public class RagController {

    @Autowired
    RagService service;


    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public ResponseEntity upload(@RequestBody MultipartFile file) throws IOException {
        service.uploadDocument(file);
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "搜索文档")
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchDoc(@RequestParam String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }
}
