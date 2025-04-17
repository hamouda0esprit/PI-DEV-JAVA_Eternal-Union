package entite;

public class Question {
    private int id;
    private Integer examen_id;
    private int nbr_points;
    private String question;
    
    // Constructeurs
    public Question() {
    }
    
    public Question(Integer examen_id, int nbr_points, String question) {
        this.examen_id = examen_id;
        this.nbr_points = nbr_points;
        this.question = question;
    }
    
    public Question(int id, Integer examen_id, int nbr_points, String question) {
        this.id = id;
        this.examen_id = examen_id;
        this.nbr_points = nbr_points;
        this.question = question;
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
    
    public int getNbr_points() {
        return nbr_points;
    }
    
    public void setNbr_points(int nbr_points) {
        this.nbr_points = nbr_points;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    @Override
    public String toString() {
        return "Question{" + 
               "id=" + id + 
               ", examen_id=" + examen_id + 
               ", nbr_points=" + nbr_points + 
               ", question='" + question + '\'' + 
               '}';
    }
} 