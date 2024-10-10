package ai.example.langchain4j.service.impl;

import ai.example.langchain4j.service.LangChain4jService;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import dev.langchain4j.data.document.Document;
import static dev.langchain4j.data.document.Metadata.metadata;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class LangChain4jServiceImpl implements LangChain4jService {

    static OllamaEmbeddingModel embeddingModel;

    static OllamaChatModel chatModel;

    static EmbeddingStore<TextSegment> embeddingStore;

    public LangChain4jServiceImpl(OllamaEmbeddingModel embeddingModel, OllamaChatModel chatModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.embeddingStore = embeddingStore;
    }

    @Override
    public String upload(MultipartFile file) {
        AssistantRag assistant = createAssistant();

        createAssistant();


        return null;
    }

    @Override
    public String search(String message) {

        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embeddingModel.embed(message).content(), 5);
        TextSegment content = new TextSegment(message,metadata("nameSpace", "fileName"));

        embeddingStore.add(embeddingModel.embed(content).content(),content);
        embeddingStore.removeAll(metadataKey("nameSpace").isEqualTo("fileName"));

        for (EmbeddingMatch<TextSegment> embeddingMatch : relevant) {
            System.out.println("Embedding: " + embeddingMatch.embedding());
            System.out.println("Score: " + embeddingMatch.score());
            System.out.println("Text: " + embeddingMatch.embedded().text());
        }
        return null;
    }

    private static AssistantRag createAssistant() {

        EmbeddingStore<TextSegment> embeddingStore = embed(toPath("documents/"), embeddingModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // Let's create a query router.
        QueryRouter queryRouter = new QueryRouter() {

            private final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from(
                    "Is the following query related to the business of the car rental company? " +
                            "Answer only 'yes', 'no' or 'maybe'. " +
                            "Query: {{it}}"
            );

            @Override
            public Collection<ContentRetriever> route(Query query) {

                Prompt prompt = PROMPT_TEMPLATE.apply(query.text());

                AiMessage aiMessage = chatModel.generate(prompt.toUserMessage()).content();
                System.out.println("LLM decided: " + aiMessage.text());

                if (aiMessage.text().toLowerCase().contains("no")) {
                    return emptyList();
                }

                return singletonList(contentRetriever);
            }
        };

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        return AiServices.builder(AssistantRag.class)
                .chatLanguageModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    /**
     * 将文档嵌入到向量空间
     * @param documentPath
     * @param embeddingModel
     * @return
     */
    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
        DocumentParser documentParser = new TextDocumentParser();


        List<Document> documents = loadDocuments(documentPath, glob("*.txt"));

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = new ArrayList<TextSegment>();

        for(Document document : documents ){
            segments.addAll(splitter.split(document));
        }

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    @AiService
    interface AssistantRag {

        String chat(@MemoryId String memoryId, @dev.langchain4j.service.UserMessage String userMessage);
    }


    public static PathMatcher glob(String glob) {
        return FileSystems.getDefault().getPathMatcher("glob:" + glob);
    }

    public static Path toPath(String relativePath) {
        try {
            URL fileUrl = LangChain4jServiceImpl.class.getResource(relativePath);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
