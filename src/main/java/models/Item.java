package models;

public class Item {
    private int id;
    private int lessonId;
    private String typeItem;
    private String content;

    public Item() {}

    public Item(int id, int lessonId, String typeItem, String content) {
        this.id = id;
        this.lessonId = lessonId;
        this.typeItem = typeItem;
        this.content = content;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getTypeItem() { return typeItem; }
    public void setTypeItem(String typeItem) { this.typeItem = typeItem; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
