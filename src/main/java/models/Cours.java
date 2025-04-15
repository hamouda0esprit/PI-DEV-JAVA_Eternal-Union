package models;

import java.time.LocalDateTime;

public class Cours {
    private int id;
    private int userId;
    private String title;
    private String image;
    private String subject;
    private int rate;
    private LocalDateTime lastUpdate;

    public Cours() {}

    public Cours(int id, int userId, String title, String image, String subject, int rate, LocalDateTime lastUpdate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.image = image;
        this.subject = subject;
        this.rate = rate;
        this.lastUpdate = lastUpdate;
    }

    public Cours( int userId, String title, String image, String subject, int rate, LocalDateTime lastUpdate) {
        this.userId = userId;
        this.title = title;
        this.image = image;
        this.subject = subject;
        this.rate = rate;
        this.lastUpdate = lastUpdate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", subject='" + subject + '\'' +
                ", rate=" + rate +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
