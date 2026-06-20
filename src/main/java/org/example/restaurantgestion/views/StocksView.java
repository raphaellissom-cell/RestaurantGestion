package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.restaurantgestion.dao.IngredientDAO;
import org.example.restaurantgestion.models.Stock;

import java.util.List;
import org.example.restaurantgestion.util.AlertUtil;

public class StocksView extends VBox {

    private final TableView<Stock> tableStocks;
    private final ObservableList<Stock> stockList = FXCollections.observableArrayList();
    private final IngredientDAO ingredientDAO = new IngredientDAO();

    public StocksView() {
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("📦 Gestion des Stocks");
        title.getStyleClass().add("section-title");

        tableStocks = new TableView<>();
        tableStocks.getStyleClass().add("table-view");
        tableStocks.setPrefHeight(350);

        TableColumn<Stock, String> colNom = new TableColumn<>("Ingrédient");
        colNom.setCellValueFactory(cd -> {
            Stock s = cd.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    s.getNomIngredient() + (s.getUnite() != null ? " (" + s.getUnite() + ")" : ""));
        });
        colNom.setPrefWidth(200);

        TableColumn<Stock, Number> colQte = new TableColumn<>("Quantité");
        colQte.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getQuantite()));
        colQte.setPrefWidth(100);
        colQte.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.1f", item.doubleValue()));
            }
        });

        TableColumn<Stock, Number> colSeuil = new TableColumn<>("Seuil d'Alerte");
        colSeuil.setCellValueFactory(cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getSeuilAlerte()));
        colSeuil.setPrefWidth(120);
        colSeuil.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.1f", item.doubleValue()));
            }
        });

        TableColumn<Stock, String> colEtat = new TableColumn<>("État");
        colEtat.setCellValueFactory(cd -> {
            Stock s = cd.getValue();
            double qte = s.getQuantite();
            double seuil = s.getSeuilAlerte();
            if (qte <= seuil / 2) return new javafx.beans.property.SimpleStringProperty("Critique");
            else if (qte <= seuil) return new javafx.beans.property.SimpleStringProperty("Faible");
            else return new javafx.beans.property.SimpleStringProperty("Normal");
        });
        colEtat.setPrefWidth(120);
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    switch (item) {
                        case "Critique" -> setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700;");
                        case "Faible" -> setStyle("-fx-text-fill: #D97706; -fx-font-weight: 700;");
                        default -> setStyle("-fx-text-fill: #059669; -fx-font-weight: 700;");
                    }
                }
            }
        });

        tableStocks.getColumns().addAll(colNom, colQte, colSeuil, colEtat);
        tableStocks.setItems(stockList);

        HBox form = new HBox(15);
        form.setPadding(new Insets(10, 0, 10, 0));

        Label lblForm = new Label("Réapprovisionner :");
        lblForm.setStyle("-fx-text-fill: #8E8E93; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtQteAjout = new TextField();
        txtQteAjout.setPromptText("Qté à ajouter...");
        txtQteAjout.setPrefWidth(130);

        Button btnReappro = new Button("⚡ Ajouter au Stock");
        btnReappro.getStyleClass().add("button-primary");
        btnReappro.setOnAction(e -> {
            Stock selected = tableStocks.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("Veuillez sélectionner un ingrédient dans la liste !");
                return;
            }

            String qteStr = txtQteAjout.getText().trim();
            if (qteStr.isEmpty()) return;

            try {
                double qte = Double.parseDouble(qteStr);
                ingredientDAO.approvisionner(selected.getId(), qte);
                chargerStocks();
                txtQteAjout.clear();
                AlertUtil.showInfo("Le stock de " + selected.getNomIngredient() + " a été mis à jour !");
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Quantité invalide !");
            }
        });

        form.getChildren().addAll(lblForm, txtQteAjout, btnReappro);

        // --- Nouvel ingrédient form ---
        Separator sep = new Separator();
        Label lblNewIng = new Label("Nouvel ingrédient :");
        lblNewIng.setStyle("-fx-text-fill: #8E8E93; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox newForm = new HBox(15);
        newForm.setPadding(new Insets(0, 0, 10, 0));

        TextField txtNomIng = new TextField();
        txtNomIng.setPromptText("Nom de l'ingrédient *");
        txtNomIng.setPrefWidth(180);

        TextField txtSeuil = new TextField();
        txtSeuil.setPromptText("Seuil d'alerte *");
        txtSeuil.setPrefWidth(120);

        TextField txtUnite = new TextField();
        txtUnite.setPromptText("Unité (g, ml, ...)");
        txtUnite.setPrefWidth(120);

        TextField txtQteInitiale = new TextField();
        txtQteInitiale.setPromptText("Qté initiale *");
        txtQteInitiale.setPrefWidth(100);

        Button btnAjouterIng = new Button("+ Ajouter");
        btnAjouterIng.getStyleClass().add("button-primary");
        btnAjouterIng.setOnAction(e -> {
            String nom = txtNomIng.getText().trim();
            String seuilStr = txtSeuil.getText().trim();
            String qteStr = txtQteInitiale.getText().trim();
            String unite = txtUnite.getText().trim();

            if (nom.isEmpty() || seuilStr.isEmpty() || qteStr.isEmpty()) {
                AlertUtil.showWarning("Veuillez remplir tous les champs obligatoires (*).");
                return;
            }
            try {
                double seuil = Double.parseDouble(seuilStr);
                double qteInit = Double.parseDouble(qteStr);
                Stock stock = new Stock();
                stock.setNomIngredient(nom);
                stock.setSeuilAlerte(seuil);
                stock.setQuantite(qteInit);
                stock.setUnite(unite.isEmpty() ? null : unite);
                ingredientDAO.ajouterIngredient(stock);
                chargerStocks();
                txtNomIng.clear();
                txtSeuil.clear();
                txtUnite.clear();
                txtQteInitiale.clear();
                AlertUtil.showInfo("Ingrédient '" + nom + "' ajouté avec succès !");
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Seuil d'alerte et quantité doivent être des nombres valides.");
            }
        });

        newForm.getChildren().addAll(txtNomIng, txtSeuil, txtUnite, txtQteInitiale, btnAjouterIng);

        this.getChildren().addAll(title, tableStocks, form, sep, lblNewIng, newForm);
        chargerStocks();
    }

    private void chargerStocks() {
        stockList.clear();
        List<Stock> stocks = ingredientDAO.getAllStocks();
        if (stocks != null) stockList.addAll(stocks);
    }
}
