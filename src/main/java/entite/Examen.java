package entite;

import java.util.Date;

public class Examen {
    private int id;
    private Integer idUser;
    private Double note;
    private String description;
    private Date date;
    private String matiere;
    private int duree;
    private int nbrEssai;
    private String type;
    private String titre;
    
    // Default constructor
    public Examen() {
    }
    
    // Constructor with all fields except id (for insertion)
    public Examen(Integer idUser, Double note, String description, Date date, 
                 String matiere, int duree, int nbrEssai, String type, String titre) {
        this.idUser = idUser;
        this.note = note;
        this.description = description;
        this.date = date;
        this.matiere = matiere;
        this.duree = duree;
        this.nbrEssai = nbrEssai;
        this.type = type;
        this.titre = titre;
    }
    
    // Constructor with all fields (for retrieval)
    public Examen(int id, Integer idUser, Double note, String description, Date date, 
                 String matiere, int duree, int nbrEssai, String type, String titre) {
        this.id = id;
        this.idUser = idUser;
        this.note = note;
        this.description = description;
        this.date = date;
        this.matiere = matiere;
        this.duree = duree;
        this.nbrEssai = nbrEssai;
        this.type = type;
        this.titre = titre;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public int getNbrEssai() {
        return nbrEssai;
    }

    public void setNbrEssai(int nbrEssai) {
        this.nbrEssai = nbrEssai;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    @Override
    public String toString() {
        return "Examen{" +
                "id=" + id +
                ", idUser=" + idUser +
                ", note=" + note +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", matiere='" + matiere + '\'' +
                ", duree=" + duree +
                ", nbrEssai=" + nbrEssai +
                ", type='" + type + '\'' +
                ", titre='" + titre + '\'' +
                '}';
    }
}
