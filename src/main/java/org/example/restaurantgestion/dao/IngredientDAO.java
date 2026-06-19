package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Stock;
import org.example.restaurantgestion.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {

    public List<Stock> getAllStocks() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Stock s ORDER BY s.nomIngredient",
                    Stock.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur IngredientDAO.getAllStocks : " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Stock getStockById(int id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(Stock.class, id);
        } finally {
            em.close();
        }
    }

    public long getNbAlertes() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(s) FROM Stock s WHERE s.quantite <= s.seuilAlerte",
                    Long.class
            ).getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur IngredientDAO.getNbAlertes : " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }

    public void approvisionner(int idStock, double quantiteAjout) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Stock stock = em.find(Stock.class, idStock);
            if (stock == null) {
                throw new IllegalArgumentException("Stock introuvable : " + idStock);
            }
            stock.setQuantite(stock.getQuantite() + quantiteAjout);
            tx.commit();
            System.out.println("Stock mis à jour : " + stock.getNomIngredient() + " +" + quantiteAjout);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new IllegalStateException("Impossible de mettre à jour le stock", e);
        } finally {
            em.close();
        }
    }

    public void ajouterIngredient(Stock stock) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(stock);
            tx.commit();
            System.out.println("Ingrédient ajouté : " + stock.getNomIngredient());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new IllegalStateException("Impossible d'ajouter l'ingrédient", e);
        } finally {
            em.close();
        }
    }

    public void mettreAJourStock(String nomIngredient, int quantiteRetiree) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Stock stock = em.createQuery(
                            "SELECT s FROM Stock s WHERE LOWER(s.nomIngredient) = LOWER(:nom)",
                            Stock.class
                    )
                    .setParameter("nom", nomIngredient)
                    .setMaxResults(1)
                    .getSingleResult();

            stock.setQuantite(stock.getQuantite() - quantiteRetiree);
            tx.commit();
            System.out.println("Stock mis à jour pour l'ingrédient : " + nomIngredient);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new IllegalStateException("Impossible de mettre à jour le stock", e);
        } finally {
            em.close();
        }
    }
}
