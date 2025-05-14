package services;

import models.Item;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private Connection connection;

    public ItemService() {
        connection = MyDabase.getInstance().getConnection();
    }

    public void ajouter(Item item) throws SQLException {
        String sql = "INSERT INTO item (lesson_id, type_item, content) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, item.getLessonId());
        ps.setString(2, item.getTypeItem());
        ps.setString(3, item.getContent());
        ps.executeUpdate();
    }




    // Modifier un item
    public void update(Item item) throws SQLException {
        String sql = "UPDATE item SET lesson_id = ?, type_item = ?, content = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, item.getLessonId());
        statement.setString(2, item.getTypeItem());
        statement.setString(3, item.getContent());
        statement.setInt(4, item.getId());
        statement.executeUpdate();
    }

    // Supprimer un item
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM item WHERE id = ?";

        try (Connection conn =  MyDabase.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    // Récupérer tous les items
    public List<Item> getAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM item";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            items.add(mapResultSetToItem(rs));
        }

        return items;
    }

    public List<Item> getItemsByLessonId(int lessonId) throws SQLException {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM item WHERE lesson_id = ?";
        try (Connection conn = MyDabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, lessonId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setLessonId(rs.getInt("lesson_id"));
                item.setTypeItem(rs.getString("type_item"));
                item.setContent(rs.getString("content"));
                items.add(item);
            }
        }
        return items;
    }


    // Méthode utilitaire pour créer un Item depuis un ResultSet
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setLessonId(rs.getInt("lesson_id"));
        item.setTypeItem(rs.getString("type_item"));
        item.setContent(rs.getString("content"));
        return item;
    }
}
