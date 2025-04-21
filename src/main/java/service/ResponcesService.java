package service;

import entite.Forum;
import entite.Responces;
import entite.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResponcesService implements IService<Responces>{

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public ResponcesService(){
        cnx= DataSource.getInstance().getConncetion();
    }

    public void createPst(Responces p){
        String requete="insert into Responces (id_forum_id,id_user_id,Comment,media,type_media,date_time) values (?,?,?,?,?,?)";
        try {
            pst=cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1,p.getForum().getId());
            pst.setInt(2,p.getUser().getId());
            pst.setString(3,p.getComment());
            pst.setString(4,p.getMedia());
            pst.setString(5,p.getType_media());
            pst.setDate(6, new java.sql.Date(p.getDate_time().getTime()));

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
    public void ajouter(Responces responces) throws SQLException {

    }

    @Override
    public void modifier(Responces responces) throws SQLException {

    }

    @Override
    public void sipprimer(int id) throws SQLException {

    }

    @Override
    public List<Responces> recuperer() throws SQLException {
        return List.of();
    }

    @Override
    public void create(Responces responces) {
        String requete="insert into Responces (title,description,media,type_media,date_time,subject,id_user_id) values('"+responces.getForum().getId()+"','"+responces.getUser().getId()+"','"+responces.getComment()+"','"+responces.getMedia()+"','"+responces.getType_media()+"','"+responces.getDate_time()+"')";
        try {
            ste=cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Responces p) {
        String sql = "UPDATE `responces` SET `id_forum_id`=?,`id_user_id`=?,`comment`=?,`media`=?,`type_media`=?,`date_time`=? WHERE id=?";

        try {
            pst=cnx.prepareStatement(sql);

            pst.setInt(1,p.getForum().getId());
            pst.setInt(2,p.getUser().getId());
            pst.setString(3,p.getComment());
            pst.setString(4,p.getMedia());
            pst.setString(5,p.getType_media());
            pst.setDate(6, new java.sql.Date(p.getDate_time().getTime()));
            pst.setInt(7,p.getId());

            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Responces responces) {
        String requete="delete from responces where id ="+responces.getId();
        try {
            ste=cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Responces> readAll() {
        String requete="select * from responces";
        List<Responces> list=new ArrayList<>();
        try {

            ste=cnx.createStatement();
            rs=ste.executeQuery(requete);
            while(rs.next()){

                Forum forum = new Forum();
                forum.setId(rs.getInt("id_forum_id"));

                list.add(new Responces(rs.getInt("id"),
                        forum,
                        rs.getInt("id_user_id"),
                        rs.getString("comment"),
                        rs.getString("media"),
                        rs.getString("type_media"),
                        rs.getDate("date_time")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Responces readById(int id) {
        return null;
    }

    public List<Responces> readByForumId(int id) {
        List<Responces> userResponces = new ArrayList<>();
        String query = "SELECT * FROM responces WHERE id_forum_id = ? ORDER BY date_time DESC";

        try {
            pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            rs = pst.executeQuery();

            while (rs.next()) {
                Responces Responce = new Responces();
                Responce.setId(rs.getInt("id"));
                Responce.setComment(rs.getString("comment"));
                Responce.setMedia(rs.getString("media"));
                Responce.setType_media(rs.getString("type_media"));
                Responce.setDate_time(rs.getTimestamp("date_time"));

                // Get user object using UserService
                UserService userService = new UserService();
                User user = userService.readById(rs.getInt("id_user_id"));
                Responce.setUser(user);

                userResponces.add(Responce);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving forums for user: " + e.getMessage());
            e.printStackTrace();
        }

        return userResponces;
    }
}
