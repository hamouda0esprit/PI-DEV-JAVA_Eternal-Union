package service;

import java.util.List;
import java.sql.SQLException;

public interface IService<T> {
    void create(T t);
    void update(T t);
    void delete(T t);
    List<T> readAll();
    T readById(int id);
    void createPst(T forum);
    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void sipprimer(int id) throws SQLException;
    List<T> recuperer() throws SQLException;
}