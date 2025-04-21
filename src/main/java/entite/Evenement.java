package entite;

import java.sql.Date;
import java.sql.Time;

public class Evenement {
    private int id;
    private String name;
    private String description;
    private Date dateevent;
    private String location;
    private Time time;
    private int iduser;
    private String photo;

    // Default constructor
    public Evenement() {
    }

    // Parameterized constructor
    public Evenement(String name, String description, Date dateevent, String location, Time time, int iduser, String photo) {
        this.name = name;
        this.description = description;
        this.dateevent = dateevent;
        this.location = location;
        this.time = time;
        this.iduser = iduser;
        this.photo = photo;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateevent() {
        return dateevent;
    }

    public void setDateevent(Date dateevent) {
        this.dateevent = dateevent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public int getIduser() {
        return iduser;
    }

    public void setIduser(int iduser) {
        this.iduser = iduser;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dateevent=" + dateevent +
                ", location='" + location + '\'' +
                ", time=" + time +
                ", iduser=" + iduser +
                ", photo='" + photo + '\'' +
                '}';
    }
}
