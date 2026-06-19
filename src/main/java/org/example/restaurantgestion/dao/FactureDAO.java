package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Facture;
import org.example.restaurantgestion.util.HibernateUtil;

public class FactureDAO {

    public void creerFacture(int idCommande, double montantTotal) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Commande commande = em.find(Commande.class, idCommande);
            if (commande == null) {
                throw new IllegalArgumentException("Commande introuvable : " + idCommande);
            }
            em.persist(new Facture(commande, montantTotal));
            tx.commit();
            System.out.println("Facture enregistrée pour la commande n°" + idCommande);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de créer la facture", e);
        } finally {
            em.close();
        }
    }
}
