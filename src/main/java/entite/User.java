package entite;

import java.util.Date;

public class User {
    private int id;
    private String name;
    private String email;
    private int phone;
    private double rate;
    private String type;
    private Date date_of_birth;
    private String password;
    private String img;
    private int score;
    private String bio;
    private String verified;
    private String google_id;
    
    public User() {
    }
    
    public User(String name, String email, int phone, String type, 
                Date date_of_birth, String password, String img, int score, 
                String bio, String verified, String google_id, double rate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.rate = rate;
        this.date_of_birth = date_of_birth;
        this.password = password;
        this.img = img;
        this.score = score;
        this.bio = bio;
        this.verified = verified;
        this.google_id = google_id;
    }
    
    public User(int id, String name, String email, int phone, String type, 
                Date date_of_birth, String password, String img, int score, 
                String bio, String verified, String google_id, double rate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.rate = rate;
        this.type = type;
        this.date_of_birth = date_of_birth;
        this.password = password;
        this.img = img;
        this.score = score;
        this.bio = bio;
        this.verified = verified;
        this.google_id = google_id;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getGoogle_id() {
        return google_id;
    }

    public void setGoogle_id(String google_id) {
        this.google_id = google_id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", type='" + type + '\'' +
                ", date_of_birth=" + date_of_birth +
                ", password='" + password + '\'' +
                ", img='" + img + '\'' +
                ", score=" + score +
                ", bio='" + bio + '\'' +
                ", verified='" + verified + '\'' +
                ", google_id='" + google_id + '\'' +
                '}';
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}