package org.example.restaurantgestion.views;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.restaurantgestion.util.HibernateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class VentesView extends VBox {

    private final BarChart<String, Number> barChart;
    private final PieChart pieChart;
    private final XYChart.Series<String, Number> series;
    private final ObservableList<PieChart.Data> pieChartData;

    public VentesView() {
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("📈 Analyse des Ventes");
        title.getStyleClass().add("section-title");

        HBox chartsLayout = new HBox(20);
        HBox.setHgrow(chartsLayout, Priority.ALWAYS);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Jour");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Chiffre d'Affaires (FCFA)");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Chiffre d'Affaires Hebdomadaire");
        barChart.setLegendVisible(false);
        barChart.setPrefWidth(400);

        series = new XYChart.Series<>();

        pieChartData = FXCollections.observableArrayList();

        pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Répartition des Ventes par Catégorie");
        pieChart.setLabelsVisible(true);
        pieChart.setPrefWidth(350);

        barChart.getData().add(series);
        chartsLayout.getChildren().addAll(barChart, pieChart);
        this.getChildren().addAll(title, chartsLayout);

        rafraichir();
    }

    public void rafraichir() {
        series.getData().clear();
        chargerDonneesCA(series);
        pieChartData.clear();
        chargerDonneesCategories(pieChartData);
    }

    private void chargerDonneesCA(XYChart.Series<String, Number> series) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Tuple> resultats = em.createQuery(
                    "SELECT FUNCTION('DATE', c.dateCommande) AS jour, SUM(c.total) AS ca " +
                    "FROM Commande c WHERE c.statut = 'Payée' " +
                    "GROUP BY FUNCTION('DATE', c.dateCommande) " +
                    "ORDER BY FUNCTION('DATE', c.dateCommande) DESC", Tuple.class)
                    .setMaxResults(7)
                    .getResultList();

            Map<LocalDate, Double> caParJour = new TreeMap<>();
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) caParJour.put(today.minusDays(i), 0.0);

            for (Tuple t : resultats) {
                LocalDate jour = t.get(0, LocalDate.class);
                Double ca = t.get(1, Double.class);
                if (jour != null && ca != null) caParJour.put(jour, ca);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            for (Map.Entry<LocalDate, Double> e : caParJour.entrySet()) {
                series.getData().add(new XYChart.Data<>(e.getKey().format(fmt), e.getValue()));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement données CA : " + e.getMessage());
            series.getData().add(new XYChart.Data<>("Aucune", 0));
        } finally {
            em.close();
        }
    }

    private void chargerDonneesCategories(ObservableList<PieChart.Data> pieData) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<Tuple> resultats = em.createQuery(
                    "SELECT p.categorie, SUM(lc.quantite) AS total " +
                    "FROM LigneCommande lc JOIN lc.produit p JOIN lc.commande c " +
                    "WHERE c.statut = 'Payée' " +
                    "GROUP BY p.categorie " +
                    "ORDER BY total DESC", Tuple.class)
                    .getResultList();

            if (resultats.isEmpty()) {
                pieData.add(new PieChart.Data("Aucune vente", 1));
                return;
            }

            for (Tuple t : resultats) {
                String cat = t.get(0, String.class);
                Number total = t.get(1, Number.class);
                pieData.add(new PieChart.Data(cat != null ? cat : "Autre", total != null ? total.doubleValue() : 0));
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement catégories : " + e.getMessage());
            pieData.add(new PieChart.Data("Aucune donnée", 1));
        } finally {
            em.close();
        }
    }
}
