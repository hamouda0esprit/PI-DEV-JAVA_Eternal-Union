package entite;

import java.util.Date;

/**
 * Entité qui stocke les feedbacks des étudiants sur les examens
 */
public class Feedback {
    private int id;
    private Integer examen_id;
    private Integer user_id;
    private String contenu;
    private Date date_creation;
    
    // Objets liés pour faciliter l'accès aux données associées
    private Examen examen;
    private User user;
    
    /**
     * Constructeur par défaut
     */
    public Feedback() {
    }
    
    /**
     * Constructeur avec ID pour les récupérations depuis la BD
     */
    public Feedback(int id, Integer examen_id, Integer user_id, String contenu, Date date_creation) {
        this.id = id;
        this.examen_id = examen_id;
        this.user_id = user_id;
        this.contenu = contenu;
        this.date_creation = date_creation;
    }
    
    /**
     * Constructeur sans ID pour les nouveaux enregistrements
     */
    public Feedback(Integer examen_id, Integer user_id, String contenu, Date date_creation) {
        this.examen_id = examen_id;
        this.user_id = user_id;
        this.contenu = contenu;
        this.date_creation = date_creation;
    }
    
    /**
     * Constructeur avec objets liés pour les relations
     */
    public Feedback(Examen examen, User user, String contenu, Date date_creation) {
        this.examen = examen;
        this.examen_id = examen != null ? examen.getId() : null;
        this.user = user;
        this.user_id = user != null ? user.getId() : null;
        this.contenu = contenu;
        this.date_creation = date_creation;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Integer getExamen_id() {
        return examen_id;
    }
    
    public void setExamen_id(Integer examen_id) {
        this.examen_id = examen_id;
    }
    
    public Integer getUser_id() {
        return user_id;
    }
    
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    
    public String getContenu() {
        return contenu;
    }
    
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
    
    public Date getDate_creation() {
        return date_creation;
    }
    
    public void setDate_creation(Date date_creation) {
        this.date_creation = date_creation;
    }
    
    public Examen getExamen() {
        return examen;
    }
    
    public void setExamen(Examen examen) {
        this.examen = examen;
        this.examen_id = examen != null ? examen.getId() : null;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        this.user_id = user != null ? user.getId() : null;
    }
    
    @Override
    public String toString() {
        return "Feedback{" +
               "id=" + id +
               ", examen_id=" + examen_id +
               ", user_id=" + user_id +
               ", contenu='" + contenu + '\'' +
               ", date_creation=" + date_creation +
               '}';
    }
} 