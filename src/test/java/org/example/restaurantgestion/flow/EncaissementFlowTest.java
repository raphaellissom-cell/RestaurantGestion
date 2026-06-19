package org.example.restaurantgestion.flow;

import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.FactureDAO;
import org.example.restaurantgestion.dao.PaiementDAO;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Facture;
import org.example.restaurantgestion.models.Paiement;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration du flow complet d'encaissement :
 * 1. Créer une commande
 * 2. L'encaisser (Paiement + Facture + TXT)
 * 3. Vérifier tous les résultats
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EncaissementFlowTest {

    private static CommandeDAO commandeDAO;
    private static PaiementDAO paiementDAO;
    private static FactureDAO factureDAO;

    private static int idTable;
    private static int idCommande;

    @BeforeAll
    static void setup() {
        commandeDAO = new CommandeDAO();
        paiementDAO = new PaiementDAO();
        factureDAO = new FactureDAO();

        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();

        TableRestaurant t = new TableRestaurant();
        t.setNumeroTable(996);
        t.setCapacite(4);
        t.setStatut("Libre");
        em.persist(t);
        idTable = t.getId();

        Commande c = new Commande();
        c.setTable(t);
        c.setDateCommande(LocalDateTime.now());
        c.setTotal(32000.0);
        c.setStatut("En cours");
        em.persist(c);
        idCommande = c.getId();

        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void cleanup() {
        java.io.File f = new java.io.File("facture_" + idCommande + ".txt");
        if (f.exists()) f.delete();

        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Facture f WHERE f.commande.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM Paiement p WHERE p.commande.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM Commande c WHERE c.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM TableRestaurant t WHERE t.id = :id").setParameter("id", idTable).executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    @Order(1)
    void etape1_commandeExiste() {
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.find(Commande.class, idCommande);
        em.close();
        assertNotNull(c, "La commande doit exister");
        assertEquals("En cours", c.getStatut(), "La commande doit être en cours");
        assertEquals(32000.0, c.getTotal(), 0.01, "Le total doit correspondre");
    }

    @Test
    @Order(2)
    void etape2_paiement() {
        paiementDAO.enregistrerPaiement(idCommande, "Carte Bancaire", 32000.0);

        EntityManager em = HibernateUtil.getEntityManager();
        TypedQuery<Paiement> q = em.createQuery(
            "SELECT p FROM Paiement p WHERE p.commande.id = :id", Paiement.class);
        q.setParameter("id", idCommande);
        List<Paiement> paiements = q.getResultList();
        em.close();

        assertFalse(paiements.isEmpty(), "Un paiement doit exister");
        Paiement p = paiements.get(0);
        assertEquals("Carte Bancaire", p.getModePaiement());
        assertEquals(32000.0, p.getMontantRecu(), 0.01);
    }

    @Test
    @Order(3)
    void etape3_statutPayee() {
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.find(Commande.class, idCommande);
        em.close();
        assertEquals("Payée", c.getStatut(), "Le statut doit être Payée après paiement");
    }

    @Test
    @Order(4)
    void etape4_facture() {
        factureDAO.creerFacture(idCommande, 32000.0);

        EntityManager em = HibernateUtil.getEntityManager();
        TypedQuery<Facture> q = em.createQuery(
            "SELECT f FROM Facture f WHERE f.commande.id = :id", Facture.class);
        q.setParameter("id", idCommande);
        List<Facture> factures = q.getResultList();
        em.close();

        assertFalse(factures.isEmpty(), "Une facture doit exister");
        Facture f = factures.get(0);
        assertEquals(32000.0, f.getTotal(), 0.01, "Le total facture doit correspondre");
        assertNotNull(f.getDateGeneration(), "La date de génération doit exister");
    }

    @Test
    @Order(5)
    void etape5_fichierTXT() {
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.createQuery(
            "SELECT c FROM Commande c JOIN FETCH c.table WHERE c.id = :id", Commande.class)
            .setParameter("id", idCommande)
            .getSingleResult();
        em.close();
        assertNotNull(c);

        String nom = CommandeDAO.genererFactureTXT(c);
        assertTrue(nom.endsWith(".txt"), "Le fichier doit être un .txt");

        String content = CommandeDAO.lireFactureTXT(idCommande);
        assertTrue(content.contains("FACTURE RESTAURANT"), "Doit contenir l'en-tête");
        assertTrue(content.contains("32 000 FCFA") || content.contains("32000 FCFA"), "Doit contenir le total");
        assertTrue(content.contains("Carte Bancaire") || content.contains("N° Commande"), "Doit contenir les infos");
    }
}
