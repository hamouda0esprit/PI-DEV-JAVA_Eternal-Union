package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Discussion {
    private String authorName;
    private String authorImageUrl;
    private String content;
    private LocalDateTime timestamp;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public Discussion(String authorName, String authorImageUrl, String content) {
        this.authorName = authorName;
        this.authorImageUrl = authorImageUrl;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorImageUrl() {
        return authorImageUrl;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(formatter);
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorImageUrl(String authorImageUrl) {
        this.authorImageUrl = authorImageUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 