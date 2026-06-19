package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Produit;
import org.example.restaurantgestion.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    public List<Produit> getAllProduits() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Produit p ORDER BY p.categorie, p.nom",
                    Produit.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur ProduitDAO.getAllProduits : " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public void ajouterProduit(Produit produit) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(produit);
            tx.commit();
            System.out.println("Produit ajouté avec succès.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible d'ajouter le produit", e);
        } finally {
            em.close();
        }
    }
}
