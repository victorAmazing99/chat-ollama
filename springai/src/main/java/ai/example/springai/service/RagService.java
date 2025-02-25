package ai.example.springai.service;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RagService {

    void uploadDocument(MultipartFile file) throws IOException;

    List<Document> search(String keyword);

    void test();
}
