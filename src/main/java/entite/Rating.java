package entite;

import java.util.Date;

/**
 * Entité qui stocke les évaluations (notes) des étudiants sur les examens
 */
public class Rating {
    private int id;
    private Integer examen_id;
    private Integer user_id;
    private Integer stars;
    private Date created_at;
    
    // Objets liés pour faciliter l'accès aux données associées
    private Examen examen;
    private User user;
    
    /**
     * Constructeur par défaut
     */
    public Rating() {
    }
    
    /**
     * Constructeur avec ID pour les récupérations depuis la BD
     */
    public Rating(int id, Integer examen_id, Integer user_id, Integer stars, Date created_at) {
        this.id = id;
        this.examen_id = examen_id;
        this.user_id = user_id;
        this.stars = stars;
        this.created_at = created_at;
    }
    
    /**
     * Constructeur sans ID pour les nouveaux enregistrements
     */
    public Rating(Integer examen_id, Integer user_id, Integer stars, Date created_at) {
        this.examen_id = examen_id;
        this.user_id = user_id;
        this.stars = stars;
        this.created_at = created_at;
    }
    
    /**
     * Constructeur avec objets liés pour les relations
     */
    public Rating(Examen examen, User user, Integer stars, Date created_at) {
        this.examen = examen;
        this.examen_id = examen != null ? examen.getId() : null;
        this.user = user;
        this.user_id = user != null ? user.getId() : null;
        this.stars = stars;
        this.created_at = created_at;
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
    
    public Integer getStars() {
        return stars;
    }
    
    public void setStars(Integer stars) {
        this.stars = stars;
    }
    
    public Date getCreated_at() {
        return created_at;
    }
    
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
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
        return "Rating{" +
               "id=" + id +
               ", examen_id=" + examen_id +
               ", user_id=" + user_id +
               ", stars=" + stars +
               ", created_at=" + created_at +
               '}';
    }
} 