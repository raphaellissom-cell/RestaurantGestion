package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Paiement;
import org.example.restaurantgestion.util.HibernateUtil;

public class PaiementDAO {

    public void enregistrerPaiement(int idCommande, String modePaiement, double montant) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Commande commande = em.find(Commande.class, idCommande);
            if (commande == null) {
                throw new IllegalArgumentException("Commande introuvable : " + idCommande);
            }

            em.persist(new Paiement(commande, modePaiement, montant));
            commande.setStatut("Payée");
            if (commande.getTable() != null) {
                commande.getTable().setStatut("Libre");
            }
            tx.commit();
            System.out.println("Paiement de " + montant + " FCFA validé par " + modePaiement);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible d'enregistrer le paiement", e);
        } finally {
            em.close();
        }
    }
}
