package org.example.restaurantgestion.dao;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
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

    public void genererFacturePDF(Commande commande) {
        Document document = new Document();
        try {
            String nomFichier = "facture_" + commande.getId() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));

            document.open();
            document.add(new Paragraph("========================================="));
            document.add(new Paragraph("           FACTURE RESTAURANT            "));
            document.add(new Paragraph("========================================="));
            document.add(new Paragraph("Numéro de Commande : " + commande.getId()));
            document.add(new Paragraph("Date : " + commande.getDateCommande()));
            document.add(new Paragraph("Table concernée : " + commande.getIdTable()));
            document.add(new Paragraph("-----------------------------------------"));
            document.add(new Paragraph("TOTAL À PAYER : " + commande.getTotal() + " FCFA"));
            document.add(new Paragraph("========================================="));
            document.add(new Paragraph("Merci de votre visite et à bientôt !"));

            System.out.println("PDF facture généré : " + nomFichier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF : " + e.getMessage());
        } finally {
            document.close();
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
