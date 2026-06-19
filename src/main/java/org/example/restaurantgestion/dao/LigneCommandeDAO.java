package org.example.restaurantgestion.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.LigneCommande;
import org.example.restaurantgestion.models.Produit;
import org.example.restaurantgestion.models.ProduitIngredient;
import org.example.restaurantgestion.models.Stock;
import org.example.restaurantgestion.util.HibernateUtil;

import java.util.List;

public class LigneCommandeDAO {

    public void ajouterLigneCommande(int idCommande, String nomMenu, int quantite) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Produit produit = em.createQuery(
                            "SELECT p FROM Produit p WHERE LOWER(p.nom) = LOWER(:nom)",
                            Produit.class
                    )
                    .setParameter("nom", nomMenu)
                    .setMaxResults(1)
                    .getSingleResult();
            ajouterLigneCommande(idCommande, produit.getId(), quantite);
        } finally {
            em.close();
        }
    }

    public void ajouterLigneCommande(int idCommande, int idProduit, int quantite) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Commande commande = em.find(Commande.class, idCommande);
            Produit produit = em.find(Produit.class, idProduit);
            if (commande == null) {
                throw new IllegalArgumentException("Commande introuvable : " + idCommande);
            }
            if (produit == null) {
                throw new IllegalArgumentException("Produit introuvable : " + idProduit);
            }

            em.persist(new LigneCommande(commande, produit, quantite, produit.getPrix()));
            commande.setTotal(commande.getTotal() + (produit.getPrix() * quantite));

            deduireStock(em, produit, quantite);

            tx.commit();
            System.out.println(quantite + "x " + produit.getNom() + " ajoutés à la commande n°" + idCommande);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Impossible d'ajouter la ligne de commande", e);
        } finally {
            em.close();
        }
    }

    private void deduireStock(EntityManager em, Produit produit, int quantiteCommandee) {
        List<ProduitIngredient> ingredients = em.createQuery(
                        "SELECT pi FROM ProduitIngredient pi JOIN FETCH pi.ingredient WHERE pi.produit.id = :id",
                        ProduitIngredient.class)
                .setParameter("id", produit.getId())
                .getResultList();

        for (ProduitIngredient pi : ingredients) {
            Stock stock = pi.getIngredient();
            double quantiteRetiree = pi.getQuantite() * quantiteCommandee;
            stock.setQuantite(stock.getQuantite() - quantiteRetiree);
            System.out.println("  Stock déduit : " + stock.getNomIngredient() + " -" + quantiteRetiree);
        }
    }
}
