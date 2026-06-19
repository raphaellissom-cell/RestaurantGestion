package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Stock;
import org.example.restaurantgestion.util.HibernateUtil;

public class IngredientDAO {

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
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de mettre à jour le stock", e);
        } finally {
            em.close();
        }
    }
}
