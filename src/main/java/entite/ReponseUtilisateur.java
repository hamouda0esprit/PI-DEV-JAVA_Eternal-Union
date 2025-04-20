package entite;

import java.util.Date;

/**
 * Entité qui représente la réponse d'un utilisateur à une question spécifique
 */
public class ReponseUtilisateur {
    private int id;
    private int userId;
    private int questionId;
    private int reponseId;
    private Date dateReponse;
    
    /**
     * Constructeur par défaut
     */
    public ReponseUtilisateur() {
    }
    
    /**
     * Constructeur avec tous les champs
     */
    public ReponseUtilisateur(int id, int userId, int questionId, int reponseId, Date dateReponse) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.dateReponse = dateReponse;
    }
    
    /**
     * Constructeur sans id pour les nouveaux enregistrements
     */
    public ReponseUtilisateur(int userId, int questionId, int reponseId, Date dateReponse) {
        this.userId = userId;
        this.questionId = questionId;
        this.reponseId = reponseId;
        this.dateReponse = dateReponse;
    }
    
    // Getters et Setters
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
    
    public int getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    
    public int getReponseId() {
        return reponseId;
    }
    
    public void setReponseId(int reponseId) {
        this.reponseId = reponseId;
    }
    
    public Date getDateReponse() {
        return dateReponse;
    }
    
    public void setDateReponse(Date dateReponse) {
        this.dateReponse = dateReponse;
    }
    
    @Override
    public String toString() {
        return "ReponseUtilisateur{" +
                "id=" + id +
                ", userId=" + userId +
                ", questionId=" + questionId +
                ", reponseId=" + reponseId +
                ", dateReponse=" + dateReponse +
                '}';
    }
} 