package entite;

import java.util.Date;

/**
 * Entité qui stocke les résultats des quiz complétés par les étudiants
 */
public class ResultatQuiz {
    private int id;
    private Examen examen;
    private Integer examen_id; // Pour la BD
    private int score;
    private int totalPoints;
    private Date datePassage;
    private User idUser;
    private Integer id_user_id; // Pour la BD
    private int nbrEssai; // Nombre d'essais restants
    
    /**
     * Constructeur par défaut
     */
    public ResultatQuiz() {
    }
    
    /**
     * Constructeur avec l'id pour les récupérations depuis la BD
     */
    public ResultatQuiz(int id, Integer examen_id, Integer id_user_id, int score, int totalPoints, Date datePassage, int nbrEssai) {
        this.id = id;
        this.examen_id = examen_id;
        this.id_user_id = id_user_id;
        this.score = score;
        this.totalPoints = totalPoints;
        this.datePassage = datePassage;
        this.nbrEssai = nbrEssai;
    }
    
    /**
     * Constructeur sans id pour les nouveaux enregistrements
     */
    public ResultatQuiz(Integer examen_id, Integer id_user_id, int score, int totalPoints, Date datePassage, int nbrEssai) {
        this.examen_id = examen_id;
        this.id_user_id = id_user_id;
        this.score = score;
        this.totalPoints = totalPoints;
        this.datePassage = datePassage;
        this.nbrEssai = nbrEssai;
    }
    
    /**
     * Constructeur avec objets Examen et User pour les relations
     */
    public ResultatQuiz(Examen examen, User idUser, int score, int totalPoints, Date datePassage, int nbrEssai) {
        this.examen = examen;
        this.examen_id = examen != null ? examen.getId() : null;
        this.idUser = idUser;
        this.id_user_id = idUser != null ? idUser.getId() : null;
        this.score = score;
        this.totalPoints = totalPoints;
        this.datePassage = datePassage;
        this.nbrEssai = nbrEssai;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Examen getExamen() {
        return examen;
    }
    
    public void setExamen(Examen examen) {
        this.examen = examen;
        this.examen_id = examen != null ? examen.getId() : null;
    }
    
    public Integer getExamen_id() {
        return examen_id;
    }
    
    public void setExamen_id(Integer examen_id) {
        this.examen_id = examen_id;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Date getDatePassage() {
        return datePassage;
    }
    
    public void setDatePassage(Date datePassage) {
        this.datePassage = datePassage;
    }
    
    public User getIdUser() {
        return idUser;
    }
    
    public void setIdUser(User idUser) {
        this.idUser = idUser;
        this.id_user_id = idUser != null ? idUser.getId() : null;
    }
    
    public Integer getId_user_id() {
        return id_user_id;
    }
    
    public void setId_user_id(Integer id_user_id) {
        this.id_user_id = id_user_id;
    }
    
    public int getNbrEssai() {
        return nbrEssai;
    }
    
    public void setNbrEssai(int nbrEssai) {
        this.nbrEssai = nbrEssai;
    }
    
    @Override
    public String toString() {
        return "ResultatQuiz{" + 
               "id=" + id + 
               ", examen_id=" + examen_id + 
               ", id_user_id=" + id_user_id + 
               ", score=" + score + 
               ", totalPoints=" + totalPoints + 
               ", datePassage=" + datePassage + 
               ", nbrEssai=" + nbrEssai +
               '}';
    }
} 