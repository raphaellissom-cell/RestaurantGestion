package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.LigneCommande;
import org.example.restaurantgestion.models.Paiement;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        StringBuilder sb = new StringBuilder();

        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<LigneCommande> lignes = em.createQuery(
                    "SELECT lc FROM LigneCommande lc JOIN FETCH lc.produit WHERE lc.commande.id = :id",
                    LigneCommande.class)
                    .setParameter("id", commande.getId())
                    .getResultList();

            Paiement paiement = null;
            try {
                paiement = em.createQuery(
                        "SELECT p FROM Paiement p WHERE p.commande.id = :id",
                        Paiement.class)
                        .setParameter("id", commande.getId())
                        .setMaxResults(1)
                        .getSingleResult();
            } catch (Exception ignored) {}

            String sep = "=========================================";
            String sep2 = "-----------------------------------------";
            String dateStr = commande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String tableStr = commande.getTable() != null
                    ? "Table " + commande.getTable().getNumeroTable()
                    : "À emporter";
            String statut = commande.getStatut();

            double sousTotal = lignes.stream()
                    .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire())
                    .sum();
            if (lignes.isEmpty()) {
                sousTotal = commande.getTotal();
            }
            double remise = commande.getRemise();
            double totalApresRemise = sousTotal - remise;

            sb.append(sep).append("\n");
            sb.append("         L'ÉLIXIR GOURMAND\n");
            sb.append("       RESTAURANT GASTRONOMIQUE\n");
            sb.append(sep).append("\n");
            sb.append("N° Commande  : #").append(commande.getId()).append("\n");
            sb.append("Date         : ").append(dateStr).append("\n");
            sb.append("Table        : ").append(tableStr).append("\n");
            sb.append("Statut       : ").append(statut).append("\n");
            sb.append(sep2).append("\n");
            sb.append(" PRODUITS CONSOMMÉS\n");
            sb.append(sep2).append("\n");

            for (LigneCommande l : lignes) {
                String nom = l.getProduit().getNom();
                int qte = l.getQuantite();
                double pu = l.getPrixUnitaire();
                double st = qte * pu;
                sb.append(String.format(Locale.US, "  %-24s %2d x %7.2f = %8.2f\n", nom, qte, pu, st));
            }

            sb.append(sep2).append("\n");
            sb.append(String.format(Locale.US, "Sous-total                 : %9.2f FCFA\n", sousTotal));
            if (remise > 0) {
                sb.append(String.format(Locale.US, "Remise                     : %9.2f FCFA\n", remise));
                sb.append(sep2).append("\n");
            }
            sb.append(String.format(Locale.US, "TOTAL                      : %9.2f FCFA\n", totalApresRemise));

            if (paiement != null) {
                sb.append(sep2).append("\n");
                sb.append("Paiement     : ").append(paiement.getModePaiement()).append("\n");
                sb.append(String.format(Locale.US, "Montant reçu  : %9.2f FCFA\n", paiement.getMontantRecu()));
                double monnaie = paiement.getMontantRecu() - totalApresRemise;
                if (monnaie >= 0) {
                    sb.append(String.format(Locale.US, "Monnaie       : %9.2f FCFA\n", monnaie));
                }
            }

            sb.append(sep).append("\n");
            sb.append("     Merci de votre visite !\n");
            sb.append("  À bientôt chez L'Élixir Gourmand\n");
            sb.append(sep).append("\n");

        } catch (Exception e) {
            System.err.println("Erreur génération facture TXT : " + e.getMessage());
            sb.append("Erreur lors de la génération de la facture.\n");
        } finally {
            em.close();
        }

        try (PrintWriter w = new PrintWriter(new FileWriter(nomFichier))) {
            w.print(sb.toString());
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
