package ai.example.springai.service.impl;

import ai.example.springai.read.ParagraphTextReader;
import ai.example.springai.service.SpringAiService;
import cn.hutool.core.util.ArrayUtil;
import jakarta.annotation.Resource;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ai.example.springai.read.ParagraphTextReader.END_PARAGRAPH_NUMBER;
import static ai.example.springai.read.ParagraphTextReader.START_PARAGRAPH_NUMBER;


@Service
public class SpringAiServiceImpl implements SpringAiService {

    @Resource
    OllamaChatModel chatModel;

    @Autowired
    private VectorStore vectorStore;

    private static final String PATH = "/Users/caoyang/Documents/workspace/chat-ollama/rag/";


    @Autowired
    public SpringAiServiceImpl(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }
    @Override
    public String sendMessage(String message) {
        ChatResponse response = chatModel.call(new Prompt(message, OllamaOptions.create()
                //  .withModel("llama3:8b")   //可动态选择模型
                .withTemperature(0.4f)));
        return response.getResult().getOutput().getContent();
    }

    @Override
    public Flux<ChatResponse> generateStream(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }

    @Override
    public void uploadDocument(MultipartFile file) {
        //保存file到本地
        String textResource = file.getOriginalFilename();
        //判断文件是否是TXT
        if (!textResource.endsWith(".txt")) {
            throw new RuntimeException("只支持txt格式文件");
        }
        String filepath = PATH + textResource;
        File file1 = new File(filepath);
        if (file1.exists()) {
            throw new RuntimeException("文件已存在");
        }
        try {
            file.transferTo(file1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Document> documentList = paragraphTextReader(file1);
        vectorStore.add(documentList);
    }

    public void uploadDocument2(MultipartFile file) throws IOException, TikaException {
        Tika tika = new Tika();
        Metadata metadata = new Metadata();
        String content = tika.parseToString(file.getInputStream(), metadata);

        // 按段落分割
        List<String> paragraphs = Arrays.stream(content.split("\n\n")).collect(Collectors.toList());


    }

    @Override
    public List<Document> search(String keyword) {
        return mergeDocuments(vectorStore.similaritySearch(SearchRequest.query(keyword)));
    }


    private List<Document> paragraphTextReader(File file) {
        List<Document> docs = null;
        try {
            ParagraphTextReader reader = new ParagraphTextReader(new FileUrlResource(file.toURI().toURL()), 5);
            reader.getCustomMetadata().put("filename", file.getName());
            reader.getCustomMetadata().put("filepath", file.getAbsolutePath());
            docs = reader.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docs;
    }


    private List<Document> mergeDocuments(List<Document> documentList) {
        List<Document> mergeDocuments = new ArrayList();
        //根据文档来源进行分组
        Map<String, List<Document>> documentMap = documentList.stream().collect(Collectors.groupingBy(item -> ((String) item.getMetadata().get("source"))));
        for (Map.Entry<String, List<Document>> docListEntry : documentMap.entrySet()) {
            //获取最大的段落结束编码
            int maxParagraphNum = (int) docListEntry.getValue()
                    .stream().max(Comparator.comparing(item -> ((int) item.getMetadata().get(END_PARAGRAPH_NUMBER)))).get().getMetadata().get(END_PARAGRAPH_NUMBER);
            //根据最大段落结束编码构建一个用于合并段落的空数组
            String[] paragraphs = new String[maxParagraphNum];
            //用于获取最小段落开始编码
            int minParagraphNum = maxParagraphNum;
            for (Document document : docListEntry.getValue()) {
                //文档内容根据回车进行分段
                String[] tempPs = document.getContent().split("\r\n");
                //获取文档开始段落编码
                int startParagraphNumber = (int) document.getMetadata().get(START_PARAGRAPH_NUMBER);
                if (minParagraphNum > startParagraphNumber) {
                    minParagraphNum = startParagraphNumber;
                }
                //将文档段落列表拷贝到合并段落数组中
                System.arraycopy(tempPs, 0, paragraphs, startParagraphNumber - 1, tempPs.length);
            }
            //合并段落去除空值,并组成文档内容
            Document mergeDoc = new Document(ArrayUtil.join(ArrayUtil.removeNull(paragraphs), "\n"));
            //合并元数据
            mergeDoc.getMetadata().putAll(docListEntry.getValue().get(0).getMetadata());
            //设置元数据:开始段落编码
            mergeDoc.getMetadata().put(START_PARAGRAPH_NUMBER, minParagraphNum);
            //设置元数据:结束段落编码
            mergeDoc.getMetadata().put(END_PARAGRAPH_NUMBER, maxParagraphNum);
            mergeDocuments.add(mergeDoc);
        }
        return mergeDocuments;
    }


    @Override
    public String ragChat(String message) {
        //查询获取文档信息
        List<Document> documents = search(message);

        //提取文本内容
        String content = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        String systemInfo = "根据以下提供的文档使用简体中文回答问题，" +
                "不要使用其他知识。如果文档中没有答案，请回复'文档中没有相关信息'。\n"+content;

        SystemMessage systemMessage = new SystemMessage(systemInfo);
        UserMessage userMessage = new UserMessage(message);
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        Prompt prompt = new Prompt(messages, OllamaOptions.create()
                //  .withModel("llama3:8b")
                .withMainGPU(1)
                .withTemperature(0.4f));

        ChatResponse chatResponse = chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getContent();
    }





}
