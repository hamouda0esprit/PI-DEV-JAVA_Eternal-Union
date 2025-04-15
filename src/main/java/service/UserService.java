package service;

import entite.Responces;
import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.List;

public class UserService implements IService<User>{

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public UserService(){
        cnx= DataSource.getInstance().getConncetion();
    }

    @Override
    public void create(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public List<User> readAll() {
        return List.of();
    }

    @Override
    public User readById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setImg(rs.getString("img"));
                // Set other user properties as needed
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void createPst(User forum) {

    }
}
