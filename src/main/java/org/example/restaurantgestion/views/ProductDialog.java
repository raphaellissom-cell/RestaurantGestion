package org.example.restaurantgestion.views;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.restaurantgestion.dao.IngredientDAO;
import org.example.restaurantgestion.dao.ProduitDAO;
import org.example.restaurantgestion.models.Produit;
import org.example.restaurantgestion.models.ProduitIngredient;
import org.example.restaurantgestion.models.Stock;

import java.util.List;

public class ProductDialog extends Stage {
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();

    private final TextField txtNom = new TextField();
    private final ComboBox<String> cbCategorie = new ComboBox<>();
    private final TextField txtPrix = new TextField();
    private final TextArea txtDescription = new TextArea();
    private final TextField txtImagePath = new TextField();
    private final CheckBox chkDisponible = new CheckBox("Disponible");

    private final ComboBox<Stock> cbIngredient = new ComboBox<>();
    private final TextField txtQuantiteIng = new TextField();
    private final TableView<IngredientPanier> tableIngredients = new TableView<>();
    private final ObservableList<IngredientPanier> ingredientList = FXCollections.observableArrayList();

    private final Produit editingProduit;

    public ProductDialog() {
        this(null);
    }

    public ProductDialog(Produit produit) {
        this.editingProduit = produit;
        initModality(Modality.APPLICATION_MODAL);
        setTitle(produit == null ? "Ajouter un produit" : "Modifier le produit");
        setAlwaysOnTop(true);
        setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #F9FAFB;");

        GridPane grid = new GridPane();
        grid.setVgap(14);
        grid.setHgap(14);
        grid.setAlignment(Pos.CENTER);

        Label lblNom = new Label("Nom du produit *");
        lblNom.setStyle("-fx-font-weight: bold;");
        grid.add(lblNom, 0, 0);
        txtNom.setPromptText("ex : Pizza Margherita");
        txtNom.setPrefWidth(300);
        grid.add(txtNom, 1, 0);

        Label lblCat = new Label("Catégorie *");
        lblCat.setStyle("-fx-font-weight: bold;");
        grid.add(lblCat, 0, 1);
        cbCategorie.getItems().addAll("Entrée", "Plat", "Dessert", "Boisson");
        cbCategorie.setValue("Plat");
        cbCategorie.setPrefWidth(300);
        grid.add(cbCategorie, 1, 1);

        Label lblPrix = new Label("Prix (FCFA) *");
        lblPrix.setStyle("-fx-font-weight: bold;");
        grid.add(lblPrix, 0, 2);
        txtPrix.setPromptText("ex : 2500");
        txtPrix.setPrefWidth(300);
        grid.add(txtPrix, 1, 2);

        Label lblDesc = new Label("Description");
        lblDesc.setStyle("-fx-font-weight: bold;");
        grid.add(lblDesc, 0, 3);
        txtDescription.setPromptText("Description du plat (préparation, goût, présentation...) ex : Saumon sauvage, avocat, citronnelle");
        txtDescription.setPrefRowCount(4);
        txtDescription.setPrefWidth(300);
        txtDescription.setPrefHeight(80);
        txtDescription.setWrapText(true);
        grid.add(txtDescription, 1, 3);

        Label lblImage = new Label("Image (URL / Emoji)");
        lblImage.setStyle("-fx-font-weight: bold;");
        grid.add(lblImage, 0, 4);
        txtImagePath.setPromptText("ex : https://image.com/pizza.jpg ou 🍕");
        txtImagePath.setPrefWidth(300);
        grid.add(txtImagePath, 1, 4);

        HBox dispoBox = new HBox(10);
        dispoBox.setAlignment(Pos.CENTER_LEFT);
        Label lblDispo = new Label("Statut");
        lblDispo.setStyle("-fx-font-weight: bold;");
        chkDisponible.setSelected(true);
        dispoBox.getChildren().addAll(lblDispo, chkDisponible);
        grid.add(dispoBox, 0, 5, 2, 1);

        Label lblIngSection = new Label("Ingrédients");
        lblIngSection.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #111827; -fx-padding: 8px 0 0 0;");

        HBox ingAddRow = new HBox(10);
        ingAddRow.setAlignment(Pos.CENTER_LEFT);
        cbIngredient.setPrefWidth(200);
        cbIngredient.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Stock s) {
                return s == null ? "" : s.getNomIngredient() + (s.getUnite() != null ? " (" + s.getUnite() + ")" : "");
            }
            @Override
            public Stock fromString(String value) { return null; }
        });
        txtQuantiteIng.setPromptText("Quantité");
        txtQuantiteIng.setPrefWidth(100);
        Button btnAjouterIng = new Button("+ Ajouter");
        btnAjouterIng.getStyleClass().add("button-primary");
        btnAjouterIng.setOnAction(e -> ajouterIngredientAuPanier());
        ingAddRow.getChildren().addAll(cbIngredient, txtQuantiteIng, btnAjouterIng);

        setupTableIngredients();

        VBox ingSection = new VBox(8);
        ingSection.getChildren().addAll(lblIngSection, ingAddRow, tableIngredients);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnEnregistrer = new Button(produit == null ? "Enregistrer" : "Modifier");
        btnEnregistrer.getStyleClass().add("button-primary");
        btnEnregistrer.setOnAction(e -> enregistrerProduit());

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.getStyleClass().add("button-secondary");
        btnAnnuler.setOnAction(e -> close());
        btnBox.getChildren().addAll(btnAnnuler, btnEnregistrer);

        root.getChildren().addAll(grid, ingSection, btnBox);

        Scene scene = new Scene(root, 580, 650);
        scene.getStylesheets().add(
            getClass().getResource("/org/example/restaurantgestion/css/style.css").toExternalForm()
        );
        setScene(scene);
        setWidth(580);
        setHeight(650);
        setMinWidth(580);
        setMinHeight(650);

        chargerIngredients();
        if (produit != null) {
            remplirChamps(produit);
        }
    }

    private void remplirChamps(Produit produit) {
        txtNom.setText(produit.getNom());
        cbCategorie.setValue(produit.getCategorie());
        txtPrix.setText(String.valueOf(produit.getPrix()));
        txtDescription.setText(produit.getDescription());
        txtImagePath.setText(produit.getImagePath());
        chkDisponible.setSelected(produit.getDisponible() != null && produit.getDisponible());

        Produit complet = produitDAO.getProduitAvecIngredients(produit.getId());
        if (complet != null && complet.getIngredients() != null) {
            for (ProduitIngredient pi : complet.getIngredients()) {
                if (pi.getIngredient() != null) {
                    ingredientList.add(new IngredientPanier(pi.getIngredient(), pi.getQuantite()));
                }
            }
        }
    }

    private void setupTableIngredients() {
        tableIngredients.getStyleClass().add("erp-table");
        tableIngredients.setPrefHeight(120);
        tableIngredients.setItems(ingredientList);

        TableColumn<IngredientPanier, String> colIng = new TableColumn<>("Ingrédient");
        colIng.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().stock.getNomIngredient()));
        colIng.setPrefWidth(200);

        TableColumn<IngredientPanier, Number> colQte = new TableColumn<>("Quantité");
        colQte.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().quantite));
        colQte.setPrefWidth(100);
        colQte.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.1f", item.doubleValue()));
            }
        });

        TableColumn<IngredientPanier, String> colUnite = new TableColumn<>("Unité");
        colUnite.setCellValueFactory(cd -> {
            String u = cd.getValue().stock.getUnite();
            return new SimpleStringProperty(u != null ? u : "");
        });
        colUnite.setPrefWidth(80);

        TableColumn<IngredientPanier, Void> colAction = new TableColumn<>("");
        colAction.setPrefWidth(60);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    IngredientPanier item = getTableView().getItems().get(getIndex());
                    ingredientList.remove(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableIngredients.getColumns().addAll(colIng, colQte, colUnite, colAction);
    }

    private void chargerIngredients() {
        List<Stock> stocks = ingredientDAO.getAllStocks();
        cbIngredient.getItems().setAll(stocks);
        if (!stocks.isEmpty()) cbIngredient.setValue(stocks.getFirst());
    }

    private void ajouterIngredientAuPanier() {
        Stock stock = cbIngredient.getValue();
        if (stock == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un ingrédient.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            double qte = Double.parseDouble(txtQuantiteIng.getText().trim());
            if (qte <= 0) throw new NumberFormatException();
            ingredientList.add(new IngredientPanier(stock, qte));
            txtQuantiteIng.clear();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "La quantité doit être un nombre valide et positif.", ButtonType.OK).showAndWait();
        }
    }

    private void enregistrerProduit() {
        String nom = txtNom.getText().trim();
        String cat = cbCategorie.getValue();
        String prixStr = txtPrix.getText().trim();
        String desc = txtDescription.getText().trim();
        String img = txtImagePath.getText().trim();
        boolean disponible = chkDisponible.isSelected();

        if (nom.isEmpty() || prixStr.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires (*).", ButtonType.OK).showAndWait();
            return;
        }
        try {
            double prix = Double.parseDouble(prixStr);

            if (editingProduit != null) {
                editingProduit.setNom(nom);
                editingProduit.setPrix(prix);
                editingProduit.setCategorie(cat);
                editingProduit.setDescription(desc);
                editingProduit.setImagePath(img);
                editingProduit.setDisponible(disponible);
                editingProduit.getIngredients().clear();
                for (IngredientPanier ip : ingredientList) {
                    editingProduit.getIngredients().add(new ProduitIngredient(editingProduit, ip.stock, ip.quantite));
                }
                produitDAO.modifierProduit(editingProduit);
                new Alert(Alert.AlertType.INFORMATION, "Produit modifié avec succès !", ButtonType.OK).showAndWait();
            } else {
                Produit p = new Produit(0, nom, prix, cat, desc, img);
                p.setDisponible(disponible);
                for (IngredientPanier ip : ingredientList) {
                    p.getIngredients().add(new ProduitIngredient(p, ip.stock, ip.quantite));
                }
                produitDAO.ajouterProduit(p);
                new Alert(Alert.AlertType.INFORMATION, "Produit ajouté avec succès !", ButtonType.OK).showAndWait();
            }
            close();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Le prix doit être un nombre valide.", ButtonType.OK).showAndWait();
        }
    }

    private static class IngredientPanier {
        private final Stock stock;
        private final double quantite;

        IngredientPanier(Stock stock, double quantite) {
            this.stock = stock;
            this.quantite = quantite;
        }
    }
}