package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VentesView extends VBox {

    public VentesView() {
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("📈 Analyse des Ventes");
        title.getStyleClass().add("section-title");

        HBox chartsLayout = new HBox(20);
        HBox.setHgrow(chartsLayout, Priority.ALWAYS);

        // 1. BarChart : Ventes de la semaine
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Jour");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Chiffre d'Affaires (FCFA)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Chiffre d'Affaires Hebdomadaire");
        barChart.setLegendVisible(false);
        barChart.setPrefWidth(400);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Lun", 420));
        series.getData().add(new XYChart.Data<>("Mar", 510));
        series.getData().add(new XYChart.Data<>("Mer", 600));
        series.getData().add(new XYChart.Data<>("Jeu", 490));
        series.getData().add(new XYChart.Data<>("Ven", 850));
        series.getData().add(new XYChart.Data<>("Sam", 1200));
        series.getData().add(new XYChart.Data<>("Dim", 950));

        barChart.getData().add(series);

        // 2. PieChart : Répartition par catégories
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Entrées", 15),
                        new PieChart.Data("Plats", 55),
                        new PieChart.Data("Desserts", 18),
                        new PieChart.Data("Boissons", 12));

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Répartition des Ventes");
        pieChart.setLabelsVisible(true);
        pieChart.setPrefWidth(350);

        chartsLayout.getChildren().addAll(barChart, pieChart);
        this.getChildren().addAll(title, chartsLayout);
    }
}
