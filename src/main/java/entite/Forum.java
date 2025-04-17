package entite;

import java.util.Date;

public class Forum {
    private int id;
    private String title;
    private String description;
    private String media;
    private String type_media;
    private Date date_time;
    private String subject;
    //private User id_user_id;
    private User user;
    private String aiprompt_responce;

    public Forum(){}

    public Forum(String title, String description, String media, String type_media, Date date_time, String subject, String aiprompt_responce) {
        this.title = title;
        this.description = description;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
        this.subject = subject;
        this.aiprompt_responce = aiprompt_responce;
    }
    public Forum(int id, String title, String description, String media, String type_media, Date date_time, String subject, String aiprompt_responce) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
        this.subject = subject;
        this.aiprompt_responce = aiprompt_responce;
    }

    public Forum(int id, String title, String description, String media, String type_media, Date date_time, String subject, String aiprompt_responce, User user) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
        this.subject = subject;
        this.aiprompt_responce = aiprompt_responce;
        this.user = user;
    }

    /*public Forum(int id, String title, String description, String media, String type_media, Date date_time, String subject, int id_user_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
        this.subject = subject;
        this.id_user_id = id_user_id;
    }*/

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getMedia() {
        return media;
    }
    public void setMedia(String media) {
        this.media = media;
    }
    public String getType_media() {
        return type_media;
    }
    public void setType_media(String type_media) {
        this.type_media = type_media;
    }
    public Date getDate_time() {
        return date_time;
    }
    public void setDate_time(Date date_time) {
        this.date_time = date_time;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getAiprompt_responce() {
        return aiprompt_responce;
    }
    public void setAiprompt_responce(String aiprompt_responce) {
        this.aiprompt_responce = aiprompt_responce;
    }

    @Override
    public String toString() {
        return "Forum : id = " + id + " | Title = " + title + " | Description = " + description;
    }
}
