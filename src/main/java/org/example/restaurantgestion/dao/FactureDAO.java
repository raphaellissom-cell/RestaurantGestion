package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Facture;
import org.example.restaurantgestion.util.HibernateUtil;

import java.util.List;

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

    public List<Facture> getAllFactures() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f JOIN FETCH f.commande c LEFT JOIN FETCH c.table ORDER BY f.dateGeneration DESC",
                Facture.class
            );
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Facture getFactureById(int id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Facture> q = em.createQuery(
                "SELECT f FROM Facture f JOIN FETCH f.commande c LEFT JOIN FETCH c.table WHERE f.id = :id",
                Facture.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
