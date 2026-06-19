package org.example.restaurantgestion.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    private final TextArea txtNotes = new TextArea();
    private final TextField txtQuantity = new TextField("1");
    private final TextField txtUnitPrice = new TextField();
    private final ComboBox<String> cbAttendant = new ComboBox<>();

    public CommandeDialog() {
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Nouvelle Commande");
        VBox root = new VBox(8);
        root.setPadding(new Insets(16, 28, 28, 28));
        root.setStyle("-fx-background-color: #F9FAFB;");

        // Header
        Label lblHeaderSub = new Label("Restaurants");
        lblHeaderSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 500;");
        Label lblHeaderTitle = new Label("Nouvelle Commande");
        lblHeaderTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827; -fx-padding: 2px 0 14px 0;");

        // Order type row
        HBox rowOrderType = new HBox(50);
        rowOrderType.setAlignment(Pos.CENTER_LEFT);
        Label lblOrderType = createRowLabel("Type de commande *");
        rbDineIn.setToggleGroup(orderTypeGroup);
        rbDineIn.setSelected(true);
        rbTakeAway.setToggleGroup(orderTypeGroup);
        rowOrderType.getChildren().addAll(lblOrderType, rbDineIn, rbTakeAway);

        // Table row
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

        // Time row (read‑only)
        HBox rowTime = new HBox(50);
        rowTime.setAlignment(Pos.CENTER_LEFT);
        Label lblTime = createRowLabel("Heure");
        txtTime.setPrefWidth(260);
        txtTime.setEditable(false);
        txtTime.setText(nowText());
        rowTime.getChildren().addAll(lblTime, txtTime);

        // Items section
        VBox sectionItems = new VBox(8);
        Label lblItemsTitle = new Label("Articles de commande *");
        lblItemsTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #374151;");
        GridPane headerGrid = createItemGrid();
        headerGrid.setStyle("-fx-background-color: #F3F4F6; -fx-border-color: #D1D5DB; -fx-border-width: 1px 1px 0 1px; -fx-padding: 10px;");
        headerGrid.add(createColHeader("Catégorie"), 0, 0);
        headerGrid.add(createColHeader("* Nom de l'article"), 1, 0);
        headerGrid.add(createColHeader("Notes"), 2, 0);
        headerGrid.add(createColHeader("* Quantité"), 3, 0);
        headerGrid.add(createColHeader("Prix unitaire"), 4, 0);
        GridPane fieldsGrid = createItemGrid();
        fieldsGrid.setHgap(10);
        fieldsGrid.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #D1D5DB; -fx-border-width: 1px; -fx-padding: 10px;");
        cbCategory.setPrefWidth(170);
        cbCategory.valueProperty().addListener((obs, oldV, newV) -> filtrerProduitsParCategorie(newV));
        txtUnitPrice.setPrefWidth(110);
        txtUnitPrice.setEditable(false);
        cbItemName.setPrefWidth(210);
        cbItemName.setConverter(new StringConverter<>() {
            @Override
            public String toString(Produit produit) { return produit == null ? "" : produit.getNom(); }
            @Override
            public Produit fromString(String value) { return null; }
        });
        cbItemName.valueProperty().addListener((obs, oldV, prod) -> txtUnitPrice.setText(prod == null ? "" : String.format("%.2f", prod.getPrix())));
        txtNotes.setPrefWidth(190);
        txtNotes.setPrefHeight(58);
        txtNotes.setPromptText("Optionnel");
        txtQuantity.setPrefWidth(100);
        fieldsGrid.add(cbCategory, 0, 0);
        fieldsGrid.add(cbItemName, 1, 0);
        fieldsGrid.add(txtNotes, 2, 0);
        fieldsGrid.add(txtQuantity, 3, 0);
        fieldsGrid.add(txtUnitPrice, 4, 0);
        Hyperlink lnkAddNew = new Hyperlink("+ Ajouter");
        lnkAddNew.setStyle("-fx-text-fill: #F07C33; -fx-font-weight: bold; -fx-font-size: 13px; -fx-underline: false;");
        lnkAddNew.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "L'ajout de plusieurs lignes sera branche à l'étape panier.", ButtonType.OK).showAndWait());
        sectionItems.getChildren().addAll(lblItemsTitle, headerGrid, fieldsGrid, lnkAddNew);

        // Attendant row
        HBox rowAttendant = new HBox(50);
        rowAttendant.setAlignment(Pos.CENTER_LEFT);
        Label lblAttendant = createRowLabel("Attendant *");
        cbAttendant.getItems().addAll("Marie - 101", "Thomas - 102", "Lucas - 103");
        cbAttendant.setValue("Marie - 101");
        cbAttendant.setPrefWidth(260);
        rowAttendant.getChildren().addAll(lblAttendant, cbAttendant);

        // Action buttons
        HBox rowActions = new HBox(12);
        rowActions.setAlignment(Pos.CENTER_LEFT);
        rowActions.setPadding(new Insets(8, 0, 0, 0));
        Button btnSubmit = new Button("Enregistrer");
        btnSubmit.getStyleClass().add("button-primary");
        btnSubmit.setOnAction(e -> { enregistrerCommande(); });
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> close());
        rowActions.getChildren().addAll(btnSubmit, btnCancel);

        // Assemble root
        root.getChildren().addAll(lblHeaderSub, lblHeaderTitle, rowOrderType, rowTable, rowTime, sectionItems, rowAttendant, rowActions);
        initialiseDonnees();
        Scene scene = new Scene(root, 900, 560);
setScene(scene);
setWidth(900);
setHeight(560);
setMinWidth(900);
setMinHeight(560);
setResizable(false);
sizeToScene();
setAlwaysOnTop(true);
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
        TableRestaurant table = cbTable.getValue();
        Produit produit = cbItemName.getValue();
        if (table == null || produit == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez choisir une table et un produit.", ButtonType.OK).showAndWait();
            return;
        }
        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantity.getText().trim());
            if (quantite <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "La quantité doit être un entier positif.", ButtonType.OK).showAndWait();
            return;
        }
        try {
            int idCommande = commandeDAO.creerCommande(table.getId());
            ligneCommandeDAO.ajouterLigneCommande(idCommande, produit.getId(), quantite);
            new Alert(Alert.AlertType.INFORMATION, "Commande n°" + idCommande + " enregistrée avec succès.", ButtonType.OK).showAndWait();
            close();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'enregistrer la commande : " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private Label createRowLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 100px;");
        return label;
    }

    private GridPane createItemGrid() {
        GridPane grid = new GridPane();
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(180),
                new ColumnConstraints(180),
                new ColumnConstraints(180),
                new ColumnConstraints(120),
                new ColumnConstraints(120)
        );
        return grid;
    }

    private Label createColHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: 800; -fx-text-fill: #374151; -fx-font-size: 12px;");
        return lbl;
    }

    private String nowText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
