package org.example.restaurantgestion.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une commande du restaurant.
 */
@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_table")
    private TableRestaurant table;

    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    @Column(nullable = false)
    private double total;

    @Column(nullable = false)
    private String statut;

    @Column(name = "remise")
    private Double remise = 0.0;

    @Column
    private String notes;

    @Column
    private String serveur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    public int getIdCommande() {
        return id;
    }

    public int getIdTable() {
        return table != null ? table.getId() : 0;
    }
}
