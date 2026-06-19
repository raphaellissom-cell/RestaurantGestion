package org.example.restaurantgestion.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private int id;

    @Column(name = "nom_ingredient", nullable = false)
    private String nomIngredient;

    @Column(nullable = false)
    private double quantite;

    @Column(name = "seuil_alerte", nullable = false)
    private double seuilAlerte;
}
