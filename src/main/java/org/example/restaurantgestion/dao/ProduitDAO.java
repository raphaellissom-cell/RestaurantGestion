package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Produit;
import org.example.restaurantgestion.models.ProduitIngredient;
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

    public Produit getProduitAvecIngredients(int id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Produit p LEFT JOIN FETCH p.ingredients i LEFT JOIN FETCH i.ingredient WHERE p.id = :id",
                Produit.class
            ).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur ProduitDAO.getProduitAvecIngredients : " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public void modifierProduit(Produit produit) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Produit managed = em.find(Produit.class, produit.getId());
            if (managed == null) {
                throw new IllegalArgumentException("Produit introuvable : " + produit.getId());
            }
            managed.setNom(produit.getNom());
            managed.setPrix(produit.getPrix());
            managed.setCategorie(produit.getCategorie());
            managed.setDescription(produit.getDescription());
            managed.setImagePath(produit.getImagePath());
            managed.setDisponible(produit.getDisponible());

            managed.getIngredients().clear();
            for (ProduitIngredient pi : produit.getIngredients()) {
                pi.setProduit(managed);
                managed.getIngredients().add(pi);
            }

            tx.commit();
            System.out.println("Produit modifié avec succès.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new IllegalStateException("Impossible de modifier le produit", e);
        } finally {
            em.close();
        }
    }

    public void supprimerProduit(int id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Produit produit = em.createQuery(
                "SELECT p FROM Produit p LEFT JOIN FETCH p.ingredients WHERE p.id = :id",
                Produit.class
            ).setParameter("id", id).getSingleResult();
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable : " + id);
            }
            produit.getIngredients().clear();
            em.remove(produit);
            tx.commit();
            System.out.println("Produit supprimé avec succès.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new IllegalStateException("Impossible de supprimer le produit", e);
        } finally {
            em.close();
        }
    }
}
