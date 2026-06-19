package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    public List<TableRestaurant> getAllTables() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM TableRestaurant t ORDER BY t.numeroTable",
                    TableRestaurant.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur TableDAO.getAllTables : " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public TableRestaurant getById(int idTable) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(TableRestaurant.class, idTable);
        } finally {
            em.close();
        }
    }

    public void ajouterTable(TableRestaurant table) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TableRestaurant existing = em.createQuery(
                            "SELECT t FROM TableRestaurant t WHERE t.numeroTable = :numero",
                            TableRestaurant.class
                    )
                    .setParameter("numero", table.getNumeroTable())
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                throw new IllegalArgumentException("Une table avec ce numero existe deja");
            }
            em.persist(table);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible d'ajouter la table", e);
        } finally {
            em.close();
        }
    }

    public void modifierTable(TableRestaurant table) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TableRestaurant managed = em.find(TableRestaurant.class, table.getId());
            if (managed == null) {
                throw new IllegalArgumentException("Table introuvable : " + table.getId());
            }

            TableRestaurant duplicate = em.createQuery(
                            "SELECT t FROM TableRestaurant t WHERE t.numeroTable = :numero AND t.id <> :id",
                            TableRestaurant.class
                    )
                    .setParameter("numero", table.getNumeroTable())
                    .setParameter("id", table.getId())
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (duplicate != null) {
                throw new IllegalArgumentException("Une table avec ce numero existe deja");
            }

            managed.setNumeroTable(table.getNumeroTable());
            managed.setCapacite(table.getCapacite());
            managed.setStatut(table.getStatut());
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de modifier la table", e);
        } finally {
            em.close();
        }
    }

    public void supprimerTable(int idTable) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TableRestaurant table = em.find(TableRestaurant.class, idTable);
            if (table == null) {
                throw new IllegalArgumentException("Table introuvable : " + idTable);
            }
            em.remove(table);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de supprimer la table", e);
        } finally {
            em.close();
        }
    }

    public void mettreAJourStatut(int idTable, String statut) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TableRestaurant table = em.find(TableRestaurant.class, idTable);
            if (table == null) {
                throw new IllegalArgumentException("Table introuvable : " + idTable);
            }
            table.setStatut(statut);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de mettre a jour la table", e);
        } finally {
            em.close();
        }
    }
}
