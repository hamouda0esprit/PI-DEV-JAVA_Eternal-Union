package entite;

public class Reponse {
    private int id;
    private Integer questions_id;
    private String reponse;
    private int etat;
    
    // Constructeurs
    public Reponse() {
    }
    
    public Reponse(Integer questions_id, String reponse, int etat) {
        this.questions_id = questions_id;
        this.reponse = reponse;
        this.etat = etat;
    }
    
    public Reponse(int id, Integer questions_id, String reponse, int etat) {
        this.id = id;
        this.questions_id = questions_id;
        this.reponse = reponse;
        this.etat = etat;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Integer getQuestions_id() {
        return questions_id;
    }
    
    public void setQuestions_id(Integer questions_id) {
        this.questions_id = questions_id;
    }
    
    public String getReponse() {
        return reponse;
    }
    
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }
    
    public int getEtat() {
        return etat;
    }
    
    public void setEtat(int etat) {
        this.etat = etat;
    }
    
    @Override
    public String toString() {
        return "Reponse{" + 
               "id=" + id + 
               ", questions_id=" + questions_id + 
               ", reponse='" + reponse + '\'' + 
               ", etat=" + etat + 
               '}';
    }
}
