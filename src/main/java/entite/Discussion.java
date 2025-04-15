package entite;

import java.time.LocalDateTime;

public class Discussion {
    private int id;
    private int eventId;
    private String caption;
    private String media;
    private LocalDateTime createdAt;

    public Discussion() {
    }

    public Discussion(int id, int eventId, String caption, String media, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.caption = caption;
        this.media = media;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getEventId() {
        return eventId;
    }

    public String getCaption() {
        return caption;
    }

    public String getMedia() {
        return media;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // For backward compatibility with existing code
    public String getDescription() {
        return caption;
    }

    public void setDescription(String description) {
        this.caption = description;
    }

    public String getPhotoPath() {
        return media;
    }

    public void setPhotoPath(String photoPath) {
        this.media = photoPath;
    }
} 