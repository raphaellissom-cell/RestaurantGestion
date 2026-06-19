package org.example.restaurantgestion.views;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.restaurantgestion.dao.ProduitDAO;
import org.example.restaurantgestion.models.Produit;

import java.util.List;

public class MenuView extends VBox {

    private final ProduitDAO produitDAO = new ProduitDAO();
    private final TableView<Produit> table = new TableView<>();
    private final ObservableList<Produit> data = FXCollections.observableArrayList();
    private String categorieSelectionnee = "Tout";

    public MenuView() {
        this.getStyleClass().add("erp-page");

        // --- HEADER ---
        HBox header = new HBox();
        header.getStyleClass().add("erp-page-header");

        Label lblTitle = new Label("Gestion de la Carte");
        lblTitle.getStyleClass().add("erp-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNew = new Button("+ Nouveau produit");
        btnNew.getStyleClass().add("button-primary");
        btnNew.setOnAction(e -> {
            new ProductDialog().showAndWait();
            chargerDonnees();
        });

        header.getChildren().addAll(lblTitle, spacer, btnNew);

        // --- TOOLBAR / FILTRES ---
        HBox toolbar = new HBox();
        toolbar.getStyleClass().add("erp-toolbar");

        ajouterFiltre(toolbar, "Tous", "Tout");
        ajouterFiltre(toolbar, "Entrées", "Entrée");
        ajouterFiltre(toolbar, "Plats", "Plat");
        ajouterFiltre(toolbar, "Desserts", "Dessert");
        ajouterFiltre(toolbar, "Boissons", "Boisson");

        // --- CORPS : TABLEAU ---
        VBox body = new VBox();
        body.getStyleClass().add("erp-body");
        VBox.setVgrow(body, Priority.ALWAYS);

        setupTable();
        body.getChildren().add(table);

        this.getChildren().addAll(header, toolbar, body);
        chargerDonnees();
    }

    private void ajouterFiltre(HBox container, String label, String catKey) {
        ToggleButton btn = new ToggleButton(label);
        btn.getStyleClass().add("erp-filter-btn");
        btn.setOnAction(e -> {
            container.getChildren().forEach(n -> {
                if (n instanceof ToggleButton tb && tb != btn) tb.setSelected(false);
            });
            btn.setSelected(true);
            categorieSelectionnee = catKey;
            appliquerFiltre();
        });
        if (catKey.equals("Tout")) btn.setSelected(true);
        container.getChildren().add(btn);
    }

    private void setupTable() {
        table.getStyleClass().add("erp-table");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Produit, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNom()));
        colNom.setPrefWidth(180);

        TableColumn<Produit, String> colCategorie = new TableColumn<>("Catégorie");
        colCategorie.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCategorie()));
        colCategorie.setPrefWidth(120);
        colCategorie.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(cat.toUpperCase());
                badge.getStyleClass().addAll("erp-badge", badgeClassFor(cat));
                setGraphic(badge);
            }
        });

        TableColumn<Produit, Number> colPrix = new TableColumn<>("Prix");
        colPrix.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getPrix()));
        colPrix.setPrefWidth(120);
        colPrix.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    return;
                }
                setText(String.format("%.2f FCFA", prix.doubleValue()));
                getStyleClass().add("erp-price");
            }
        });

        TableColumn<Produit, String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        colDescription.setPrefWidth(180);

        TableColumn<Produit, Void> colActions = new TableColumn<>("");
        colActions.setPrefWidth(220);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetail = new Button("Détail");
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox box = new HBox(4);
            {
                box.setAlignment(Pos.CENTER);
                btnDetail.getStyleClass().add("erp-action-btn");
                btnEdit.getStyleClass().add("erp-action-btn");
                btnDelete.getStyleClass().addAll("erp-action-btn", "erp-action-btn-danger");

                btnDetail.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    afficherDetailsProduit(p);
                });
                btnEdit.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    ProductDialog dialog = new ProductDialog(p);
                    dialog.showAndWait();
                    chargerDonnees();
                });
                btnDelete.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    supprimerProduit(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                box.getChildren().setAll(btnDetail, btnEdit, btnDelete);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colNom, colCategorie, colPrix, colDescription, colActions);
        table.setItems(data);

        table.setRowFactory(tv -> {
            TableRow<Produit> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    afficherDetailsProduit(row.getItem());
                }
            });
            return row;
        });
    }

    private String badgeClassFor(String cat) {
        return switch (cat) {
            case "Entrée" -> "erp-badge-entree";
            case "Plat" -> "erp-badge-plat";
            case "Dessert" -> "erp-badge-dessert";
            case "Boisson" -> "erp-badge-boisson";
            default -> "erp-badge-entree";
        };
    }

    private void appliquerFiltre() {
        data.clear();
        List<Produit> tous = produitDAO.getAllProduits();
        if (tous == null) return;
        tous.stream()
            .filter(p -> categorieSelectionnee.equals("Tout") || p.getCategorie().equals(categorieSelectionnee))
            .forEach(data::add);
    }

    public void chargerDonnees() {
        appliquerFiltre();
    }

    private void afficherDetailsProduit(Produit produit) {
        Produit produitComplet = produitDAO.getProduitAvecIngredients(produit.getId());
        if (produitComplet == null) {
            new Alert(Alert.AlertType.ERROR, "Impossible de charger les détails du produit.", ButtonType.OK).showAndWait();
            return;
        }

        Stage detailsStage = new Stage();
        detailsStage.initModality(Modality.APPLICATION_MODAL);
        detailsStage.setTitle("Détails - " + produitComplet.getNom());
        detailsStage.setResizable(false);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(24));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #FFFFFF;");

        StackPane mediaContainer = new StackPane();
        mediaContainer.setPrefSize(200, 150);
        mediaContainer.setMinSize(200, 150);
        mediaContainer.setMaxSize(200, 150);

        String catColor = "#6B7280";
        String catEmoji = "🍽️";
        switch (produitComplet.getCategorie()) {
            case "Entrée" -> { catColor = "#10B981"; catEmoji = "🥗"; }
            case "Plat" -> { catColor = "#3B82F6"; catEmoji = "🍝"; }
            case "Dessert" -> { catColor = "#EC4899"; catEmoji = "🍰"; }
            case "Boisson" -> { catColor = "#F59E0B"; catEmoji = "🍹"; }
        }

        mediaContainer.setStyle(
            "-fx-background-color: " + catColor + "1A; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-color: " + catColor + "33; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 12px;"
        );

        boolean hasImage = false;
        if (produitComplet.getImagePath() != null && !produitComplet.getImagePath().trim().isEmpty()) {
            String path = produitComplet.getImagePath().trim();
            if (path.length() <= 2) {
                Label lblBigEmoji = new Label(path);
                lblBigEmoji.setStyle("-fx-font-size: 72px;");
                mediaContainer.getChildren().add(lblBigEmoji);
                hasImage = true;
            } else {
                try {
                    ImageView imgView = new ImageView(new Image(path));
                    imgView.setFitWidth(180);
                    imgView.setFitHeight(130);
                    imgView.setPreserveRatio(true);
                    mediaContainer.getChildren().add(imgView);
                    hasImage = true;
                } catch (Exception ex) {
                    System.out.println("Impossible de charger l'image : " + ex.getMessage());
                }
            }
        }
        if (!hasImage) {
            Label lblBigEmoji = new Label(catEmoji);
            lblBigEmoji.setStyle("-fx-font-size: 72px;");
            mediaContainer.getChildren().add(lblBigEmoji);
        }

        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblNom = new Label(produitComplet.getNom());
        lblNom.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #1F2937;");
        lblNom.setWrapText(true);

        Label lblCat = new Label(produitComplet.getCategorie().toUpperCase());
        lblCat.setStyle(
            "-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: " + catColor + "; " +
            "-fx-background-color: " + catColor + "15; -fx-padding: 4px 10px; -fx-background-radius: 12px;"
        );

        Label lblPrix = new Label(String.format("Prix : %.2f FCFA", produitComplet.getPrix()));
        lblPrix.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #F07C33; -fx-padding: 4px 0;");

        Boolean dispo = produitComplet.getDisponible();
        Label lblDispo = new Label(dispo != null && dispo ? "✓ Disponible" : "✗ Indisponible");
        lblDispo.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: 700; -fx-padding: 4px 10px; -fx-background-radius: 12px;" +
            (dispo != null && dispo
                ? "-fx-text-fill: #059669; -fx-background-color: #05966915;"
                : "-fx-text-fill: #DC2626; -fx-background-color: #DC262615;")
        );

        Separator separator = new Separator();

        Label lblDescTitle = new Label("Description :");
        lblDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #4B5563;");

        String descriptionText = (produitComplet.getDescription() == null || produitComplet.getDescription().trim().isEmpty())
                ? "Aucune description fournie pour ce produit."
                : produitComplet.getDescription();
        Label lblDesc = new Label(descriptionText);
        lblDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563; -fx-line-spacing: 1.4;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(350);

        infoBox.getChildren().addAll(lblNom, lblCat, lblPrix, lblDispo, separator, lblDescTitle, lblDesc);

        if (produitComplet.getIngredients() != null && !produitComplet.getIngredients().isEmpty()) {
            Separator sep2 = new Separator();
            Label lblIngTitle = new Label("Ingrédients :");
            lblIngTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #4B5563;");

            VBox ingBox = new VBox(4);
            for (var pi : produitComplet.getIngredients()) {
                String nomIng = pi.getIngredient() != null ? pi.getIngredient().getNomIngredient() : "?";
                String unite = pi.getIngredient() != null && pi.getIngredient().getUnite() != null ? pi.getIngredient().getUnite() : "";
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                Label bullet = new Label("•");
                bullet.setStyle("-fx-font-size: 14px; -fx-text-fill: #F07C33; -fx-font-weight: 700;");
                Label ingLabel = new Label(nomIng + " : " + pi.getQuantite() + " " + unite);
                ingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");
                row.getChildren().addAll(bullet, ingLabel);
                ingBox.getChildren().add(row);
            }

            infoBox.getChildren().addAll(sep2, lblIngTitle, ingBox);
        }

        Button btnClose = new Button("Fermer");
        btnClose.getStyleClass().add("button-secondary");
        btnClose.setPrefWidth(120);
        btnClose.setOnAction(e -> detailsStage.close());

        layout.getChildren().addAll(mediaContainer, infoBox, btnClose);

        Scene scene = new Scene(layout, 420, 550);
        scene.getStylesheets().add(
            getClass().getResource("/org/example/restaurantgestion/css/style.css").toExternalForm()
        );
        detailsStage.setScene(scene);
        detailsStage.showAndWait();
    }

    private void supprimerProduit(Produit produit) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer le produit \"" + produit.getNom() + "\" ?\nCette action est irréversible.",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    produitDAO.supprimerProduit(produit.getId());
                    chargerDonnees();
                    new Alert(Alert.AlertType.INFORMATION,
                        "Produit \"" + produit.getNom() + "\" supprimé avec succès.",
                        ButtonType.OK).showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR,
                        "Impossible de supprimer ce produit :\n" + ex.getMessage(),
                        ButtonType.OK).showAndWait();
                }
            }
        });
    }
}
