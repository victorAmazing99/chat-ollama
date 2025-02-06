package ai.example.springai.Pojo;

import java.util.List;

public class PowerPoint {
    private String title;          // PPT 标题
    private String author;         // 作者
    private List<Slide> slides;    // 幻灯片列表

    // 构造方法
    public PowerPoint() {}

    public PowerPoint(String title, String author, List<Slide> slides) {
        this.title = title;
        this.author = author;
        this.slides = slides;
    }

    // Getter 和 Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

    @Override
    public String toString() {
        return "PowerPoint [title=" + title + ", author=" + author + ", slides=" + slides + "]";
    }
}
