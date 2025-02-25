package ai.example.springai.splitter;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VectorBasedSplitter extends TextSplitter {


    private final OllamaEmbeddingModel embeddingModel;

    private static final String PARAGRAPH_DELIMITER = "\n"; // 假设段落之间用换行符分隔
    private static final String SENTENCE_DELIMITERS = "[.!?。！？]";
    private int windowSize= 3;

    @Autowired
    public VectorBasedSplitter(OllamaEmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<String> splitAndMerge(String text, double cosineThreshold) {
        // 根据段落分隔符将文本分割成段落列表，并去除前后空格和空段落
        List<String> paragraphs = Arrays.asList(text.split(PARAGRAPH_DELIMITER));
        paragraphs = paragraphs.stream().map(String::trim).filter(p -> !p.isEmpty()).collect(Collectors.toList());

        List<String> result = new ArrayList<>();

        // 处理每个段落
        for (String paragraph : paragraphs) {
            // 根据句子分隔符将段落分割成句子列表，并去除前后空格和空句子
            List<String> sentences = Arrays.asList(paragraph.split(SENTENCE_DELIMITERS));
            sentences = sentences.stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

            result.addAll(mergeSentences(sentences, cosineThreshold));

        }

        return result;
    }

    private List<String> mergeSentences(List<String> sentences, double cosineThreshold) {
        List<String> mergedSentences = new ArrayList<>();
        List<String> window = new ArrayList<>();
        StringBuilder currentMerged = new StringBuilder();

        for (String sentence : sentences) {
            if (window.size() == windowSize) {
                window.remove(0); // 移除最早的句子
            }
            window.add(sentence);

            if (currentMerged.length() > 0) {
                boolean shouldMerge = true;
                for (String w : window) {
                    if (!w.equals(sentence)) {
                        double similarity = cosineSimilarity(currentMerged.toString().trim(), w);
                        if (similarity < cosineThreshold) {
                            shouldMerge = false;
                            break;
                        }
                    }
                }
                if (!shouldMerge) {
                    mergedSentences.add(currentMerged.toString().trim());
                    currentMerged.setLength(0); // Clear the builder for the next segment
                }
            }
            currentMerged.append(sentence).append(" ");
        }

        if (currentMerged.length() > 0) {
            mergedSentences.add(currentMerged.toString().trim());
        }

        return mergedSentences;
    }

    private String joinSentences(List<String> sentences) {
        return String.join(" ", sentences);
    }

    // 余弦相似度计算
    private double cosineSimilarity(String s1, String s2) {

       float[] vecA = embeddingModel.embed(s1);
       float[] vecB = embeddingModel.embed(s2);

        if (vecA.length != vecB.length) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    @Override
    protected List<String> splitText(String text) {
        return splitAndMerge(text, 0.8);
    }



}