package ai.example.springai.service.impl;

import ai.example.springai.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class RagServiceImpl implements RagService {

    @Autowired
    private VectorStore vectorStore;

    @Override
    public void uploadDocument(MultipartFile file) throws IOException {
        List<Document> documents = new TokenTextSplitter().transform(new TextReader(file.getResource()).read());
        vectorStore.write(documents);
    }

    @Override
    public List<Document> search(String keyword) {

        return vectorStore.similaritySearch(SearchRequest.builder().query(keyword).topK(4).similarityThreshold(0.7f).build());

    }


}
