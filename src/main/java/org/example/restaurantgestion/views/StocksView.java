package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class StocksView extends VBox {

    private final TableView<StockItem> tableStocks;
    private final ObservableList<StockItem> stockList = FXCollections.observableArrayList();

    public StocksView() {
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("📦 Gestion des Stocks");
        title.getStyleClass().add("section-title");

        // Tableau des stocks
        tableStocks = new TableView<>();
        tableStocks.getStyleClass().add("table-view");
        tableStocks.setPrefHeight(350);

        TableColumn<StockItem, String> colNom = new TableColumn<>("Ingrédient");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(180);

        TableColumn<StockItem, Integer> colQte = new TableColumn<>("Quantité");
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQte.setPrefWidth(100);

        TableColumn<StockItem, Integer> colSeuil = new TableColumn<>("Seuil d'Alerte");
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        colSeuil.setPrefWidth(120);

        TableColumn<StockItem, String> colEtat = new TableColumn<>("État");
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colEtat.setPrefWidth(120);

        // Personnaliser le rendu de la colonne État
        colEtat.setCellFactory(column -> new TableCell<StockItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Critique".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700;"); // Rouge foncé lisible
                    } else if ("Faible".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #D97706; -fx-font-weight: 700;"); // Ambre/Orange foncé lisible
                    } else {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: 700;"); // Vert foncé lisible
                    }
                }
            }
        });

        tableStocks.getColumns().addAll(colNom, colQte, colSeuil, colEtat);
        tableStocks.setItems(stockList);

        // Formulaire de réapprovisionnement
        HBox form = new HBox(15);
        form.setPadding(new Insets(10, 0, 10, 0));

        Label lblForm = new Label("Réapprovisionner :");
        lblForm.setStyle("-fx-text-fill: #8E8E93; -fx-font-weight: bold; -fx-alignment: center-left; -fx-font-size: 14px;");

        TextField txtQteAjout = new TextField();
        txtQteAjout.setPromptText("Qté à ajouter...");
        txtQteAjout.setPrefWidth(130);

        Button btnReappro = new Button("⚡ Ajouter au Stock");
        btnReappro.getStyleClass().add("button-primary");
        btnReappro.setOnAction(e -> {
            StockItem selected = tableStocks.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un ingrédient dans la liste !", ButtonType.OK).showAndWait();
                return;
            }

            String qteStr = txtQteAjout.getText().trim();
            if (qteStr.isEmpty()) return;

            try {
                int qte = Integer.parseInt(qteStr);
                selected.setQuantite(selected.getQuantite() + qte);
                tableStocks.refresh();
                txtQteAjout.clear();
                new Alert(Alert.AlertType.INFORMATION, "Le stock de " + selected.getNom() + " a été mis à jour !", ButtonType.OK).showAndWait();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Quantité invalide !", ButtonType.OK).showAndWait();
            }
        });

        form.getChildren().addAll(lblForm, txtQteAjout, btnReappro);

        this.getChildren().addAll(title, tableStocks, form);

        chargerStocks();
    }

    private void chargerStocks() {
        stockList.clear();
        stockList.add(new StockItem("Farine", 50, 10));
        stockList.add(new StockItem("Tomates", 8, 15));
        stockList.add(new StockItem("Fromage râpé", 3, 5));
        stockList.add(new StockItem("Viande hachée (kg)", 25, 8));
        stockList.add(new StockItem("Saumon frais (kg)", 12, 5));
        stockList.add(new StockItem("Café en grain (kg)", 15, 3));
    }

    public static class StockItem {
        private final String nom;
        private int quantite;
        private int seuilAlerte;

        public StockItem(String nom, int quantite, int seuilAlerte) {
            this.nom = nom;
            this.quantite = quantite;
            this.seuilAlerte = seuilAlerte;
        }

        public String getNom() { return nom; }
        public int getQuantite() { return quantite; }
        public void setQuantite(int quantite) { this.quantite = quantite; }
        public int getSeuilAlerte() { return seuilAlerte; }
        public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }

        public String getEtat() {
            if (quantite <= seuilAlerte / 2) {
                return "Critique";
            } else if (quantite <= seuilAlerte) {
                return "Faible";
            } else {
                return "Normal";
            }
        }
    }
}
