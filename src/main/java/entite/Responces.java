package entite;

import java.util.Date;

public class Responces {
    private int id;
    private Forum forum;
    private User user;
    private String Comment;
    private String media;
    private String type_media;
    private Date date_time;

    public Responces(){};

    public Responces(Forum forum, String comment, String media, String type_media, Date date_time) {
        this.forum = forum;
        this.Comment = comment;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
    }

    public Responces(int id ,Forum forum, int id_user_id, String comment, String media, String type_media, Date date_time) {
        this.id = id;
        this.forum = forum;
        this.Comment = comment;
        this.media = media;
        this.type_media = type_media;
        this.date_time = date_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public User getUser() {
        return user;
    }

    public void setId_user_id(User user) {
        this.user = user;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
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
}
