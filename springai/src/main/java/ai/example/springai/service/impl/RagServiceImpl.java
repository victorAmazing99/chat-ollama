package ai.example.springai.service.impl;

import ai.example.springai.service.RagService;
import ai.example.springai.splitter.VectorBasedSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
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

    private OllamaEmbeddingModel embeddingModel;
    @Autowired
    public RagServiceImpl(OllamaEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void uploadDocument(MultipartFile file) throws IOException {
        List<Document> documents = new TokenTextSplitter().transform(new TextReader(file.getResource()).read());
        vectorStore.write(documents);
    }

    @Override
    public List<Document> search(String keyword) {

        return vectorStore.similaritySearch(SearchRequest.builder().query(keyword).topK(4).similarityThreshold(0.7f).build());

    }


    public  void test() {
        String text = """
                标题：迷途之森           
                在遥远的地方，有一片被遗忘的古老森林，人们称它为“迷途之森”。传说，在这片森林里隐藏着无数的秘密与奇迹，但只有那些心怀纯真且勇敢无畏的人才能找到真正的出路，并发现那些不为人知的宝藏。                           
                故事的主角是一个名叫艾莉亚的女孩，她住在靠近森林边缘的小村庄里。艾莉亚从小就听闻关于迷途之森的各种传说，心中充满了对未知世界的好奇与向往。终于，在她十八岁生日那天，艾莉亚决定踏入那片神秘的森林，寻找属于自己的冒险。
                带着简单的行囊，艾莉亚告别了家人和朋友，独自一人走进了迷途之森。初入森林时，阳光透过茂密的树叶洒下，形成一片片光斑，四周静谧得只听见她自己的脚步声和偶尔传来的鸟鸣。然而，随着深入森林，环境逐渐变得阴暗潮湿，道路也愈发难以辨认。
                就在艾莉亚感到有些迷失方向的时候，她遇到了一位老者。老者穿着朴素的衣服，眼神中透露出深邃的智慧。“年轻人，为何独自一人来到这危险之地？”老者问道。艾莉亚向他讲述了自己对冒险的渴望以及想要探索未知的决心。听到这里，老者微微一笑，递给她一枚看似普通的石头，“带上这个，当你真正需要帮助时，它会指引你。”
                怀着感激的心情接过石头后，艾莉亚继续她的旅程。随着时间的推移，她经历了许多挑战，包括解决复杂的谜题、躲避危险的陷阱以及面对内心的恐惧。每一次困难都是对她意志的一次考验，而那枚石头也在关键时刻给予了她意想不到的帮助。
                经过数日的努力，艾莉亚终于找到了传说中的宝藏——并非金银财宝，而是关于自我认知和个人成长的知识。通过这次经历，她不仅学会了如何在复杂多变的环境中生存，更重要的是，她发现了内心深处未曾察觉的力量。              
                最终，艾莉亚带着满满的收获回到了村庄，但她知道，这只是她人生旅程的一个开始。未来还有更多的未知等待着她去探索。                       
                """;
        VectorBasedSplitter splitter = new VectorBasedSplitter(embeddingModel);
        List<String> result = splitter.splitAndMerge(text, 0.8);

        System.out.println("Resulting Segments:");
        for (String segment : result) {
            System.out.println("[split]"+segment+"[/split]");
        }
    }

}
