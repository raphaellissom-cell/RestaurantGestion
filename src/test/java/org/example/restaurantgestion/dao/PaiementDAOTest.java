package org.example.restaurantgestion.dao;

import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Paiement;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaiementDAOTest {

    private static PaiementDAO dao;
    private static int idTable;
    private static int idCommande;

    @BeforeAll
    static void setup() {
        dao = new PaiementDAO();
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();

        TableRestaurant t = new TableRestaurant();
        t.setNumeroTable(998);
        t.setCapacite(4);
        t.setStatut("Libre");
        em.persist(t);
        idTable = t.getId();

        Commande c = new Commande();
        c.setTable(t);
        c.setDateCommande(LocalDateTime.now());
        c.setTotal(15000.0);
        c.setStatut("En cours");
        em.persist(c);
        idCommande = c.getId();

        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void cleanup() {
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Paiement p WHERE p.commande.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM Commande c WHERE c.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM TableRestaurant t WHERE t.id = :id").setParameter("id", idTable).executeUpdate();
        em.getTransaction().commit();
        em.close();
        PaiementDAOTest.cleanupFacture();
    }

    private static void cleanupFacture() {
        try {
            EntityManager em = HibernateUtil.getEntityManager();
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Facture f WHERE f.commande.id = :id").setParameter("id", idCommande).executeUpdate();
            em.getTransaction().commit();
            em.close();
        } catch (Exception ignored) {}
    }

    @Test
    @Order(1)
    void testEnregistrerPaiement() {
        dao.enregistrerPaiement(idCommande, "Espèces", 20000.0);

        EntityManager em = HibernateUtil.getEntityManager();
        TypedQuery<Paiement> q = em.createQuery(
            "SELECT p FROM Paiement p WHERE p.commande.id = :id", Paiement.class);
        q.setParameter("id", idCommande);
        List<Paiement> result = q.getResultList();
        em.close();

        assertFalse(result.isEmpty(), "Un paiement doit exister");
        Paiement p = result.get(0);
        assertEquals("Espèces", p.getModePaiement(), "Le mode doit être Espèces");
        assertEquals(20000.0, p.getMontantRecu(), 0.01, "Le montant doit correspondre");
    }

    @Test
    @Order(2)
    void testStatutDevientPayee() {
        EntityManager em = HibernateUtil.getEntityManager();
        Commande c = em.find(Commande.class, idCommande);
        em.close();
        assertNotNull(c, "La commande doit exister");
        assertEquals("Payée", c.getStatut(), "Le statut doit être Payée après paiement");
    }
}
