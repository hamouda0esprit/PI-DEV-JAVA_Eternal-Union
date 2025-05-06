package service;

import entite.Evenement;
import java.util.List;

public interface IEvenementService {
    void add(Evenement e);
    List<Evenement> getAll();
    Evenement getOne(int id);
    void update(Evenement e);
    void delete(int id);
    List<Evenement> getAllEvenements();
} 