package org.example.restaurantgestion.dao;

import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandeDAOTest {

    private static CommandeDAO dao;
    private static int idTable;
    private static int idCommande;

    @BeforeAll
    static void setup() {
        dao = new CommandeDAO();
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        TableRestaurant t = new TableRestaurant();
        t.setNumeroTable(999);
        t.setCapacite(4);
        t.setStatut("Libre");
        em.persist(t);
        em.getTransaction().commit();
        idTable = t.getId();
        em.close();
    }

    @AfterAll
    static void cleanup() {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Commande c WHERE c.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM TableRestaurant t WHERE t.id = :id").setParameter("id", idTable).executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    @Order(1)
    void testCreerCommande() {
        idCommande = dao.creerCommande(idTable);
        assertTrue(idCommande > 0, "L'ID de la commande doit être > 0");
    }

    @Test
    @Order(2)
    void testMettreAJourStatut() {
        dao.mettreAJourStatut(idCommande, "Payée");
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.find(Commande.class, idCommande);
        em.close();
        assertNotNull(c, "La commande doit exister");
        assertEquals("Payée", c.getStatut(), "Le statut doit être Payée");
    }

    @Test
    @Order(3)
    void testGetHistoriqueVentes() {
        List<Commande> list = dao.getHistoriqueVentes();
        assertNotNull(list, "La liste ne doit pas être null");
        assertFalse(list.isEmpty(), "La liste ne doit pas être vide");
    }

    @Test
    @Order(4)
    void testGenererFactureTXT() {
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.createQuery(
            "SELECT c FROM Commande c JOIN FETCH c.table WHERE c.id = :id", Commande.class)
            .setParameter("id", idCommande)
            .getSingleResult();
        em.close();
        assertNotNull(c, "La commande doit exister");

        String nom = CommandeDAO.genererFactureTXT(c);
        assertTrue(nom.endsWith(".txt"), "Le fichier doit être un .txt");

        String content = CommandeDAO.lireFactureTXT(c.getIdCommande());
        assertTrue(content.contains("L'ÉLIXIR GOURMAND"), "Le contenu doit contenir l'en-tête");
        assertTrue(content.contains("FCFA"), "Le contenu doit mentionner FCFA");
    }
}
