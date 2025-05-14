package service;

import entite.Evenement;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IEvenementService {
    private Connection conn;
    private PreparedStatement pst;

    public EvenementService() {
        conn = DataSource.getInstance().getConnection();
    }

    @Override
    public void add(Evenement e) {
        String query = "INSERT INTO evenement (name, description, dateevent, location, time, iduser, photo, capacite) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, e.getName());
            pst.setString(2, e.getDescription());
            pst.setDate(3, e.getDateevent());
            pst.setString(4, e.getLocation());
            pst.setTime(5, e.getTime());
            pst.setInt(6, e.getIduser());
            pst.setString(7, e.getPhoto());
            pst.setInt(8, e.getCapacite());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        System.out.println("Fetching all events from database");
        String query = "SELECT * FROM evenement";
        List<Evenement> list = new ArrayList<>();
        try {
            pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setName(rs.getString("name"));
                e.setDescription(rs.getString("description"));
                e.setDateevent(rs.getDate("dateevent"));
                e.setLocation(rs.getString("location"));
                e.setTime(rs.getTime("time"));
                e.setIduser(rs.getInt("iduser"));
                e.setPhoto(rs.getString("photo"));
                e.setCapacite(rs.getInt("capacite"));
                list.add(e);
                System.out.println("Found event: " + e.getName());
            }
            System.out.println("Total events found: " + list.size());
        } catch (SQLException ex) {
            System.err.println("Error fetching events: " + ex.getMessage());
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public Evenement getOne(int id) {
        String query = "SELECT * FROM evenement WHERE id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setName(rs.getString("name"));
                e.setDescription(rs.getString("description"));
                e.setDateevent(rs.getDate("dateevent"));
                e.setLocation(rs.getString("location"));
                e.setTime(rs.getTime("time"));
                e.setIduser(rs.getInt("iduser"));
                e.setPhoto(rs.getString("photo"));
                e.setCapacite(rs.getInt("capacite"));
                return e;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    @Override
    public void update(Evenement e) {
        String query = "UPDATE evenement SET name=?, description=?, dateevent=?, location=?, time=?, iduser=?, photo=?, capacite=? WHERE id=?";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, e.getName());
            pst.setString(2, e.getDescription());
            pst.setDate(3, e.getDateevent());
            pst.setString(4, e.getLocation());
            pst.setTime(5, e.getTime());
            pst.setInt(6, e.getIduser());
            pst.setString(7, e.getPhoto());
            pst.setInt(8, e.getCapacite());
            pst.setInt(9, e.getId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM evenement WHERE id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public List<Evenement> getAllEvenements() {
        return getAll();
    }
} 