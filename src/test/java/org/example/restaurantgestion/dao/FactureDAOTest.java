package org.example.restaurantgestion.dao;

import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.Facture;
import org.example.restaurantgestion.models.TableRestaurant;
import org.example.restaurantgestion.util.HibernateUtil;
import org.junit.jupiter.api.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FactureDAOTest {

    private static FactureDAO dao;
    private static int idTable;
    private static int idCommande;
    private static int idFacture;

    @BeforeAll
    static void setup() {
        dao = new FactureDAO();
        EntityManager em = HibernateUtil.getEntityManager();
        em.getTransaction().begin();

        TableRestaurant t = new TableRestaurant();
        t.setNumeroTable(997);
        t.setCapacite(4);
        t.setStatut("Libre");
        em.persist(t);
        idTable = t.getId();

        Commande c = new Commande();
        c.setTable(t);
        c.setDateCommande(LocalDateTime.now());
        c.setTotal(25000.0);
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
        em.createQuery("DELETE FROM Facture f WHERE f.id = :id").setParameter("id", idFacture).executeUpdate();
        em.createQuery("DELETE FROM Commande c WHERE c.id = :id").setParameter("id", idCommande).executeUpdate();
        em.createQuery("DELETE FROM TableRestaurant t WHERE t.id = :id").setParameter("id", idTable).executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    @Test
    @Order(1)
    void testCreerFacture() {
        dao.creerFacture(idCommande, 25000.0);

        EntityManager em = HibernateUtil.getEntityManager();
        TypedQuery<Facture> q = em.createQuery(
            "SELECT f FROM Facture f WHERE f.commande.id = :id", Facture.class);
        q.setParameter("id", idCommande);
        List<Facture> result = q.getResultList();
        em.close();

        assertFalse(result.isEmpty(), "Une facture doit exister");
        Facture f = result.get(0);
        idFacture = f.getId();
        assertEquals(25000.0, f.getTotal(), 0.01, "Le total doit correspondre");
        assertNotNull(f.getDateGeneration(), "La date doit être générée");
    }

    @Test
    @Order(2)
    void testGetFactureById() {
        assertTrue(idFacture > 0, "L'ID facture doit être défini");
        Facture f = dao.getFactureById(idFacture);
        assertNotNull(f, "La facture doit être trouvée");
        assertEquals(idCommande, f.getCommande().getIdCommande(), "La commande liée doit correspondre");
    }

    @Test
    @Order(3)
    void testGetAllFactures() {
        List<Facture> list = dao.getAllFactures();
        assertNotNull(list, "La liste ne doit pas être null");
        assertFalse(list.isEmpty(), "La liste ne doit pas être vide");
        boolean found = list.stream().anyMatch(f -> f.getId() == idFacture);
        assertTrue(found, "La facture créée doit être dans la liste");
    }
}
