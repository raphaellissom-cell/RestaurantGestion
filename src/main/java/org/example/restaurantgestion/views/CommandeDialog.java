package org.example.restaurantgestion.views;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.LigneCommandeDAO;
import org.example.restaurantgestion.dao.ProduitDAO;
import org.example.restaurantgestion.dao.TableDAO;
import org.example.restaurantgestion.models.Produit;
import org.example.restaurantgestion.models.TableRestaurant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.example.restaurantgestion.util.AlertUtil;

public class CommandeDialog extends Stage {

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final LigneCommandeDAO ligneCommandeDAO = new LigneCommandeDAO();
    private final ProduitDAO produitDAO = new ProduitDAO();
    private final TableDAO tableDAO = new TableDAO();

    private final ToggleGroup orderTypeGroup = new ToggleGroup();
    private final RadioButton rbDineIn = new RadioButton("Sur place");
    private final RadioButton rbTakeAway = new RadioButton("A emporter");
    private final ComboBox<TableRestaurant> cbTable = new ComboBox<>();
    private final TextField txtTime = new TextField();

    private final ComboBox<String> cbCategory = new ComboBox<>();
    private final ComboBox<Produit> cbItemName = new ComboBox<>();
    private final TextField txtQuantity = new TextField("1");

    private final TableView<PanierItem> panierTable = new TableView<>();
    private final ObservableList<PanierItem> panier = FXCollections.observableArrayList();
    private final Label lblTotalPanier = new Label("0.00 FCFA");

    private final ComboBox<String> cbAttendant = new ComboBox<>();
    private final TextField txtRemise = new TextField();
    private final TextArea txtNotes = new TextArea();

    public CommandeDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Nouvelle Commande");
        VBox root = new VBox(8);
        root.setPadding(new Insets(16, 28, 28, 28));
        root.setStyle("-fx-background-color: #F9FAFB;");

        Label lblHeaderSub = new Label("Restaurants");
        lblHeaderSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 500;");
        Label lblHeaderTitle = new Label("Nouvelle Commande");
        lblHeaderTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827; -fx-padding: 2px 0 14px 0;");

        HBox rowOrderType = new HBox(50);
        rowOrderType.setAlignment(Pos.CENTER_LEFT);
        Label lblOrderType = createRowLabel("Type de commande *");
        rbDineIn.setToggleGroup(orderTypeGroup);
        rbDineIn.setSelected(true);
        rbTakeAway.setToggleGroup(orderTypeGroup);
        rbTakeAway.setOnAction(e -> cbTable.setDisable(true));
        rbDineIn.setOnAction(e -> cbTable.setDisable(false));
        rowOrderType.getChildren().addAll(lblOrderType, rbDineIn, rbTakeAway);

        HBox rowTable = new HBox(50);
        rowTable.setAlignment(Pos.CENTER_LEFT);
        Label lblTable = createRowLabel("Table *");
        cbTable.setPrefWidth(260);
        cbTable.setConverter(new StringConverter<>() {
            @Override
            public String toString(TableRestaurant table) {
                if (table == null) return "";
                return "Table " + table.getNumeroTable() + " - " + table.getStatut() + " (" + table.getCapacite() + " pers.)";
            }
            @Override
            public TableRestaurant fromString(String value) { return null; }
        });
        rowTable.getChildren().addAll(lblTable, cbTable);

        HBox rowTime = new HBox(50);
        rowTime.setAlignment(Pos.CENTER_LEFT);
        Label lblTime = createRowLabel("Heure");
        txtTime.setPrefWidth(260);
        txtTime.setEditable(false);
        txtTime.setText(nowText());
        rowTime.getChildren().addAll(lblTime, txtTime);

        VBox sectionItems = new VBox(8);
        Label lblItemsTitle = new Label("Articles de commande *");
        lblItemsTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #374151;");

        HBox addRow = new HBox(10);
        addRow.setAlignment(Pos.CENTER_LEFT);
        cbCategory.setPrefWidth(150);
        cbCategory.valueProperty().addListener((obs, oldV, newV) -> filtrerProduitsParCategorie(newV));
        cbItemName.setPrefWidth(200);
        cbItemName.setConverter(new StringConverter<>() {
            @Override
            public String toString(Produit produit) { return produit == null ? "" : produit.getNom() + " - " + String.format("%.0f", produit.getPrix()) + " FCFA"; }
            @Override
            public Produit fromString(String value) { return null; }
        });
        txtQuantity.setPrefWidth(80);
        txtQuantity.setPromptText("Qté");
        Button btnAjouter = new Button("+ Ajouter");
        btnAjouter.getStyleClass().add("button-primary");
        btnAjouter.setOnAction(e -> ajouterAuPanier());
        addRow.getChildren().addAll(cbCategory, cbItemName, txtQuantity, btnAjouter);

        setupPanierTable();

        HBox panierTotal = new HBox(10);
        panierTotal.setAlignment(Pos.CENTER_RIGHT);
        panierTotal.setPadding(new Insets(8, 0, 0, 0));
        Label lblTotalLabel = new Label("Total :");
        lblTotalLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 16px; -fx-text-fill: #374151;");
        lblTotalPanier.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #F07C33;");
        panierTotal.getChildren().addAll(lblTotalLabel, lblTotalPanier);

        sectionItems.getChildren().addAll(lblItemsTitle, addRow, panierTable, panierTotal);

        HBox rowRemise = new HBox(50);
        rowRemise.setAlignment(Pos.CENTER_LEFT);
        Label lblRemise = createRowLabel("Remise (FCFA)");
        txtRemise.setPromptText("0");
        txtRemise.setPrefWidth(260);
        rowRemise.getChildren().addAll(lblRemise, txtRemise);

        HBox rowAttendant = new HBox(50);
        rowAttendant.setAlignment(Pos.CENTER_LEFT);
        Label lblAttendant = createRowLabel("Attendant *");
        cbAttendant.getItems().addAll("Marie - 101", "Thomas - 102", "Lucas - 103");
        cbAttendant.setValue("Marie - 101");
        cbAttendant.setPrefWidth(260);
        rowAttendant.getChildren().addAll(lblAttendant, cbAttendant);

        HBox rowNotes = new HBox(50);
        rowNotes.setAlignment(Pos.CENTER_LEFT);
        Label lblNotes = createRowLabel("Notes");
        txtNotes.setPromptText("Notes sur la commande...");
        txtNotes.setPrefWidth(260);
        txtNotes.setPrefRowCount(2);
        rowNotes.getChildren().addAll(lblNotes, txtNotes);

        HBox rowActions = new HBox(12);
        rowActions.setAlignment(Pos.CENTER_LEFT);
        rowActions.setPadding(new Insets(8, 0, 0, 0));
        Button btnSubmit = new Button("Enregistrer la commande");
        btnSubmit.getStyleClass().add("button-primary");
        btnSubmit.setOnAction(e -> enregistrerCommande());
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> close());
        rowActions.getChildren().addAll(btnSubmit, btnCancel);

        root.getChildren().addAll(lblHeaderSub, lblHeaderTitle, rowOrderType, rowTable, rowTime, sectionItems, rowRemise, rowAttendant, rowNotes, rowActions);
        initialiseDonnees();

        Scene scene = new Scene(root, 850, 700);
        scene.getStylesheets().add(
            getClass().getResource("/org/example/restaurantgestion/css/style.css").toExternalForm()
        );
        setScene(scene);
        setWidth(850);
        setHeight(620);
        setMinWidth(850);
        setMinHeight(620);
        setResizable(true);
        sizeToScene();
        setAlwaysOnTop(true);
    }

    private void setupPanierTable() {
        panierTable.getStyleClass().add("erp-table");
        panierTable.setPrefHeight(180);
        panierTable.setItems(panier);

        TableColumn<PanierItem, String> colProd = new TableColumn<>("Produit");
        colProd.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().produit.getNom()));
        colProd.setPrefWidth(200);

        TableColumn<PanierItem, Number> colQte = new TableColumn<>("Qté");
        colQte.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().quantite));
        colQte.setPrefWidth(80);

        TableColumn<PanierItem, Number> colPU = new TableColumn<>("Prix unitaire");
        colPU.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().produit.getPrix()));
        colPU.setPrefWidth(120);
        colPU.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f FCFA", item.doubleValue()));
            }
        });

        TableColumn<PanierItem, Number> colST = new TableColumn<>("Sous-total");
        colST.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getSousTotal()));
        colST.setPrefWidth(120);
        colST.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f FCFA", item.doubleValue()));
            }
        });

        TableColumn<PanierItem, Void> colAction = new TableColumn<>("");
        colAction.setPrefWidth(60);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    PanierItem item = getTableView().getItems().get(getIndex());
                    panier.remove(item);
                    mettreAJourTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        panierTable.getColumns().addAll(colProd, colQte, colPU, colST, colAction);
    }

    private void ajouterAuPanier() {
        Produit produit = cbItemName.getValue();
        if (produit == null) {
            AlertUtil.showWarning("Veuillez sélectionner un produit.");
            return;
        }
        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantity.getText().trim());
            if (quantite <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("La quantité doit être un entier positif.");
            return;
        }
        panier.add(new PanierItem(produit, quantite));
        mettreAJourTotal();
        txtQuantity.setText("1");
    }

    private void mettreAJourTotal() {
        double total = panier.stream().mapToDouble(PanierItem::getSousTotal).sum();
        lblTotalPanier.setText(String.format("%,.0f FCFA", total));
    }

    private void initialiseDonnees() {
        cbTable.getItems().setAll(tableDAO.getAllTables());
        List<Produit> produitsDb = produitDAO.getAllProduits();
        cbCategory.getItems().setAll(
                produitsDb.stream()
                        .map(Produit::getCategorie)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList()
        );
    }

    private void filtrerProduitsParCategorie(String categorie) {
        List<Produit> all = produitDAO.getAllProduits();
        List<Produit> filtered = all.stream()
                .filter(p -> categorie == null || categorie.equalsIgnoreCase(p.getCategorie()))
                .toList();
        cbItemName.getItems().setAll(filtered);
        cbItemName.setValue(filtered.isEmpty() ? null : filtered.getFirst());
    }

    private void enregistrerCommande() {
        if (panier.isEmpty()) {
            AlertUtil.showWarning("Ajoutez au moins un article à la commande.");
            return;
        }

        boolean isDineIn = rbDineIn.isSelected();
        TableRestaurant table = cbTable.getValue();
        if (isDineIn && table == null) {
            AlertUtil.showWarning("Veuillez choisir une table.");
            return;
        }

        double remise = 0;
        try {
            String remiseStr = txtRemise.getText().trim();
            if (!remiseStr.isEmpty()) {
                remise = Double.parseDouble(remiseStr);
                if (remise < 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            AlertUtil.showError("La remise doit être un nombre valide et positif.");
            return;
        }

        String serveur = cbAttendant.getValue();
        String notes = txtNotes.getText().trim();

        try {
            int idCommande;
            if (isDineIn) {
                idCommande = commandeDAO.creerCommande(table.getId(), serveur, notes, remise);
            } else {
                idCommande = commandeDAO.creerCommande(0, serveur, notes, remise);
            }

            for (PanierItem item : panier) {
                ligneCommandeDAO.ajouterLigneCommande(idCommande, item.produit.getId(), item.quantite);
            }

            AlertUtil.showInfo("Commande n°" + idCommande + " enregistrée avec succès (" + panier.size() + " article(s)).");
            close();
        } catch (Exception ex) {
            AlertUtil.showError("Impossible d'enregistrer la commande : " + ex.getMessage());
        }
    }

    private Label createRowLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 100px;");
        return label;
    }

    private String nowText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private static class PanierItem {
        private final Produit produit;
        private final int quantite;

        PanierItem(Produit produit, int quantite) {
            this.produit = produit;
            this.quantite = quantite;
        }

        double getSousTotal() {
            return produit.getPrix() * quantite;
        }
    }
}
