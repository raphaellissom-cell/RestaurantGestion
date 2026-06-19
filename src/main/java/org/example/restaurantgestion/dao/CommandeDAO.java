package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAO {

    public int creerCommande(int idTable) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TableRestaurant table = em.find(TableRestaurant.class, idTable);
            if (table == null) {
                throw new IllegalArgumentException("Table introuvable : " + idTable);
            }

            Commande commande = new Commande();
            commande.setTable(table);
            commande.setDateCommande(LocalDateTime.now());
            commande.setTotal(0.0);
            commande.setStatut("En cours");
            table.setStatut("Occupée");
            em.persist(commande);
            tx.commit();
            System.out.println("Commande créée pour la table " + idTable);
            return commande.getId();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de créer la commande", e);
        } finally {
            em.close();
        }
    }

    public void mettreAJourStatut(int idCommande, String statut) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Commande commande = em.find(Commande.class, idCommande);
            if (commande == null) {
                throw new IllegalArgumentException("Commande introuvable : " + idCommande);
            }
            commande.setStatut(statut);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible de mettre à jour la commande", e);
        } finally {
            em.close();
        }
    }

    public static String genererFactureTXT(Commande commande) {
        String nomFichier = "facture_" + commande.getId() + ".txt";
        String sep = "=========================================";
        String content = sep + "\n"
            + "           FACTURE RESTAURANT\n"
            + sep + "\n"
            + "N° Commande : " + commande.getId() + "\n"
            + "Date        : " + commande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n"
            + "Table       : " + (commande.getTable() != null ? commande.getTable().getNumeroTable() : "À emporter") + "\n"
            + "-----------------------------------------\n"
            + "TOTAL       : " + String.format("%,.0f", commande.getTotal()) + " FCFA\n"
            + sep + "\n"
            + "Merci de votre visite et à bientôt !\n";
        try (PrintWriter w = new PrintWriter(new FileWriter(nomFichier))) {
            w.print(content);
            System.out.println("Facture TXT générée : " + nomFichier);
        } catch (IOException e) {
            System.err.println("Erreur écriture facture TXT : " + e.getMessage());
        }
        return nomFichier;
    }

    public static String lireFactureTXT(int idCommande) {
        String nomFichier = "facture_" + idCommande + ".txt";
        try (BufferedReader r = new BufferedReader(new FileReader(nomFichier))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "Facture introuvable : " + nomFichier;
        }
    }

    public List<Commande> getHistoriqueVentes() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Commande c LEFT JOIN FETCH c.table ORDER BY c.id DESC",
                    Commande.class
            ).getResultList();
        } catch (Exception e) {
            System.err.println("Erreur CommandeDAO.getHistoriqueVentes : " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
