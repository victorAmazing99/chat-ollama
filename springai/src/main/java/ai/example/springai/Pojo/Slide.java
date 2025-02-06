package ai.example.springai.Pojo;

import java.util.List;

public class Slide {
    private String slideTitle;     // 幻灯片标题
    private String content;        // 正文内容
    private List<String> images;   // 图片路径列表
    private String layoutType;     // 布局类型（如标题页、内容页）

    // 构造方法
    public Slide() {}

    public Slide(String slideTitle, String content, List<String> images, String layoutType) {
        this.slideTitle = slideTitle;
        this.content = content;
        this.images = images;
        this.layoutType = layoutType;
    }

    // Getter 和 Setter
    public String getSlideTitle() {
        return slideTitle;
    }

    public void setSlideTitle(String slideTitle) {
        this.slideTitle = slideTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    @Override
    public String toString() {
        return "Slide [slideTitle=" + slideTitle + ", content=" + content + ", images=" + images + ", layoutType=" + layoutType + "]";
    }
}