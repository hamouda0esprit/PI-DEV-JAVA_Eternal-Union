package service;

import entite.Forum;
import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService implements IService<Forum>{

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public ForumService(){
        cnx= DataSource.getInstance().getConncetion();
    }

    public void createPst(Forum p){
        String requete="insert into Forum (Title,Description,Media,Type_media,Date_Time,Subject,Id_User_Id,Aiprompt_responce) values (?,?,?,?,?,?,?,?)";
        try {
            pst=cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1,p.getTitle());
            pst.setString(2,p.getDescription());
            pst.setString(3,p.getMedia());
            pst.setString(4,p.getType_media());
            pst.setDate(5, new java.sql.Date(p.getDate_time().getTime()));
            pst.setString(6,p.getSubject());
            pst.setInt(7,p.getUser().getId());
            pst.setString(8,p.getAiprompt_responce());

            ;
            pst.executeUpdate();

            ResultSet rs=pst.getGeneratedKeys();

            if (rs.next()){
                p.setId(rs.getInt(1));
            }else{
                throw new SQLException("Erreur SQL Insertion");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ajouter(Forum forum) throws SQLException {

    }

    @Override
    public void modifier(Forum forum) throws SQLException {

    }

    @Override
    public void sipprimer(int id) throws SQLException {

    }

    @Override
    public List<Forum> recuperer() throws SQLException {
        return List.of();
    }


    //Not recommended because of SQL injections (Quoted by SOUSOU)
    @Override
    public void create(Forum forum) {
        String requete="insert into forum (title,description,media,type_media,date_time,subject,id_user_id,aiprompt_responce) values('"+forum.getTitle()+"','"+forum.getDescription()+"','"+forum.getMedia()+"','"+forum.getType_media()+"','"+forum.getDate_time()+"','"+forum.getSubject()+"','"+forum.getUser().getId()+"','"+forum.getAiprompt_responce()+"')";
        try {
            ste=cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Forum p) {
        String sql = "UPDATE `forum` SET `title`=?,`description`=?,`media`=?,`type_media`=?,`date_time`=?,`subject`=?,`id_user_id`=?,`aiprompt_responce`=? WHERE id=?";

        try {
            pst=cnx.prepareStatement(sql);

            pst.setString(1,p.getTitle());
            pst.setString(2,p.getDescription());
            pst.setString(3,p.getMedia());
            pst.setString(4,p.getType_media());
            pst.setDate(5, new java.sql.Date(p.getDate_time().getTime()));
            pst.setString(6,p.getSubject());
            pst.setInt(7,p.getUser().getId());
            pst.setString(8,p.getAiprompt_responce());
            pst.setInt(9,p.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Forum forum) {
        String requete="delete from forum where id ="+forum.getId();
        try {
            ste=cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Forum> readAll() {
        String requete="select * from forum";
        List<Forum> list=new ArrayList<>();
        try {
            ste=cnx.createStatement();
            rs=ste.executeQuery(requete);
            while(rs.next()){
                User u = new User();

                UserService us = new UserService();

                u = us.readById(rs.getInt("id_user_id"));

                list.add(new Forum(rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("media"),
                        rs.getString("type_media"),
                        rs.getDate("date_time"),
                        rs.getString("subject"),
                        rs.getString("aiprompt_responce")
                        ,u
                        ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Forum readById(int id) {
        String sql = "SELECT * FROM forum WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Forum forum = new Forum();
                forum.setId(rs.getInt("id"));
                forum.setTitle(rs.getString("title"));
                forum.setDescription(rs.getString("description"));
                forum.setDate_time(rs.getDate("date_time"));
                // Set other user properties as needed
                return forum;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Forum> readByUserId(int userId) {
        List<Forum> userForums = new ArrayList<>();
        String query = "SELECT * FROM forum WHERE id_user_id = ? ORDER BY date_time DESC";

        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, userId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Forum forum = new Forum();
                forum.setId(rs.getInt("id"));
                forum.setTitle(rs.getString("title"));
                forum.setDescription(rs.getString("description"));
                forum.setMedia(rs.getString("media"));
                forum.setType_media(rs.getString("type_media"));
                forum.setSubject(rs.getString("subject"));
                forum.setDate_time(rs.getTimestamp("date_time"));
                forum.setAiprompt_responce(rs.getString("aiprompt_responce"));

                // Get user object using UserService
                UserService userService = new UserService();
                User user = userService.readById(rs.getInt("id_user_id"));
                forum.setUser(user);

                userForums.add(forum);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving forums for user: " + e.getMessage());
            e.printStackTrace();
        }

        return userForums;
    }
}
