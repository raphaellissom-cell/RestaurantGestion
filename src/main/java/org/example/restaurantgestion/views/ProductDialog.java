package org.example.restaurantgestion.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.restaurantgestion.dao.ProduitDAO;
import org.example.restaurantgestion.models.Produit;

public class ProductDialog extends Stage {
    private final ProduitDAO produitDAO = new ProduitDAO();

    private final TextField txtNom = new TextField();
    private final ComboBox<String> cbCategorie = new ComboBox<>();
    private final TextField txtPrix = new TextField();
    private final TextArea txtDescription = new TextArea();
    private final TextField txtImagePath = new TextField();

    public ProductDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Ajouter un produit");
        setAlwaysOnTop(true);
        setResizable(false);

        // Layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(28));
        grid.setVgap(14);
        grid.setHgap(14);
        grid.setAlignment(Pos.CENTER);

        // Nom
        Label lblNom = new Label("Nom du produit *");
        lblNom.setStyle("-fx-font-weight: bold;");
        grid.add(lblNom, 0, 0);
        txtNom.setPromptText("ex : Pizza Margherita");
        txtNom.setPrefWidth(300);
        grid.add(txtNom, 1, 0);

        // Catégorie
        Label lblCat = new Label("Catégorie *");
        lblCat.setStyle("-fx-font-weight: bold;");
        grid.add(lblCat, 0, 1);
        cbCategorie.getItems().addAll("Entrée", "Plat", "Dessert", "Boisson");
        cbCategorie.setValue("Plat");
        cbCategorie.setPrefWidth(300);
        grid.add(cbCategorie, 1, 1);

        // Prix
        Label lblPrix = new Label("Prix (FCFA) *");
        lblPrix.setStyle("-fx-font-weight: bold;");
        grid.add(lblPrix, 0, 2);
        txtPrix.setPromptText("ex : 2500");
        txtPrix.setPrefWidth(300);
        grid.add(txtPrix, 1, 2);

        // Description
        Label lblDesc = new Label("Description");
        lblDesc.setStyle("-fx-font-weight: bold;");
        grid.add(lblDesc, 0, 3);
        txtDescription.setPromptText("Ingrédients, détails du plat...");
        txtDescription.setPrefRowCount(5);
        txtDescription.setPrefWidth(300);
        txtDescription.setPrefHeight(100);
        txtDescription.setWrapText(true);
        grid.add(txtDescription, 1, 3);

        // Image Path / Emoji
        Label lblImage = new Label("Image (URL / Emoji)");
        lblImage.setStyle("-fx-font-weight: bold;");
        grid.add(lblImage, 0, 4);
        txtImagePath.setPromptText("ex : https://image.com/pizza.jpg ou 🍕");
        txtImagePath.setPrefWidth(300);
        grid.add(txtImagePath, 1, 4);

        // Buttons
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnEnregistrer = new Button("Enregistrer");
        btnEnregistrer.getStyleClass().add("button-primary");
        btnEnregistrer.setOnAction(e -> enregistrerProduit());

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.getStyleClass().add("button-secondary");
        btnAnnuler.setOnAction(e -> close());
        btnBox.getChildren().addAll(btnAnnuler, btnEnregistrer);

        grid.add(btnBox, 0, 5, 2, 1);

        Scene scene = new Scene(grid, 520, 520);
        setScene(scene);
        setWidth(520);
        setHeight(520);
        setMinWidth(520);
        setMinHeight(520);
    }

    private void enregistrerProduit() {
        String nom = txtNom.getText().trim();
        String cat = cbCategorie.getValue();
        String prixStr = txtPrix.getText().trim();
        String desc = txtDescription.getText().trim();
        String img = txtImagePath.getText().trim();

        if (nom.isEmpty() || prixStr.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires (*).", ButtonType.OK).showAndWait();
            return;
        }
        try {
            double prix = Double.parseDouble(prixStr);
            Produit p = new Produit(0, nom, prix, cat, desc, img);
            produitDAO.ajouterProduit(p);
            new Alert(Alert.AlertType.INFORMATION, "Produit ajouté avec succès !", ButtonType.OK).showAndWait();
            close();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Le prix doit être un nombre valide.", ButtonType.OK).showAndWait();
        }
    }
}
