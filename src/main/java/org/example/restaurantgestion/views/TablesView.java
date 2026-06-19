package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.restaurantgestion.dao.TableDAO;
import org.example.restaurantgestion.models.TableRestaurant;

import java.util.List;

public class TablesView extends VBox {

    private final MainView mainView;
    private final TableDAO tableDAO = new TableDAO();
    private final FlowPane flowPane;

    private final ObservableList<String> statuts = FXCollections.observableArrayList("Libre", "Occupée", "Réservée");
    private final TextField txtNumero;
    private final TextField txtCapacite;
    private final ComboBox<String> cbStatut;
    private final Label lblSelection;

    private Integer tableSelectionneeId;

    public TablesView(MainView mainView) {
        this.mainView = mainView;
        this.getStyleClass().add("erp-page");

        // --- HEADER ---
        HBox header = new HBox();
        header.getStyleClass().add("erp-page-header");

        Label lblTitle = new Label("Plan de Salle");
        lblTitle.getStyleClass().add("erp-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻");
        btnRefresh.getStyleClass().add("erp-action-btn");
        btnRefresh.setTooltip(new Tooltip("Rafraîchir"));
        btnRefresh.setOnAction(e -> rafraichirTables());

        Button btnAdd = new Button("+ Nouvelle table");
        btnAdd.getStyleClass().add("button-primary");
        btnAdd.setOnAction(e -> {
            new TableDialog().showAndWait();
            rafraichirTables();
        });

        header.getChildren().addAll(lblTitle, spacer, btnRefresh, btnAdd);

        // --- BODY (FlowPane avec cartes) ---
        VBox body = new VBox();
        body.getStyleClass().add("erp-body");
        VBox.setVgrow(body, Priority.ALWAYS);

        flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);

        VBox.setVgrow(flowPane, Priority.ALWAYS);

        body.getChildren().add(flowPane);

        this.getChildren().addAll(header, body);

        txtNumero = new TextField();
        txtCapacite = new TextField();
        cbStatut = new ComboBox<>();
        lblSelection = new Label();

        rafraichirTables();
    }

    public void rafraichirTables() {
        flowPane.getChildren().clear();
        List<TableRestaurant> list = tableDAO.getAllTables();

        if (list == null || list.isEmpty()) {
            Label noData = new Label("Aucune table trouvée.");
            noData.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF; -fx-font-weight: 600; -fx-padding: 40px;");
            flowPane.getChildren().add(noData);
            return;
        }

        for (TableRestaurant table : list) {
            flowPane.getChildren().add(creerCarteTable(table));
        }
    }

    private VBox creerCarteTable(TableRestaurant table) {
        VBox card = new VBox(10);
        card.getStyleClass().add("table-card");
        card.setPrefSize(220, 200);
        card.setAlignment(Pos.CENTER);
        if (tableSelectionneeId != null && tableSelectionneeId == table.getId()) {
            card.getStyleClass().add("table-card-selected");
        }

        String statut = table.getStatut();
        appliquerStyleStatut(card, statut);

        Label lblNum = new Label("Table " + table.getNumeroTable());
        lblNum.getStyleClass().add("table-num");

        Label lblCapacite = new Label("Capacité : " + table.getCapacite() + " pers.");
        lblCapacite.getStyleClass().add("table-info");

        Label lblStatut = new Label(statut.toUpperCase());
        lblStatut.getStyleClass().add("table-status");
        appliquerStyleBadge(lblStatut, statut);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button btnCommande = new Button("Commander");
        btnCommande.getStyleClass().add("erp-action-btn");
        btnCommande.setDisable("Réservée".equalsIgnoreCase(statut));
        btnCommande.setOnAction(e -> mainView.ouvrirCommandePourTable(table.getId()));

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("erp-action-btn");
        btnEdit.setOnAction(e -> selectionnerTable(table));

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("erp-action-btn", "erp-action-btn-danger");
        btnDelete.setOnAction(e -> supprimerDepuisCarte(table));

        actions.getChildren().addAll(btnCommande, btnEdit, btnDelete);
        card.getChildren().addAll(lblNum, lblCapacite, lblStatut, actions);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            selectionnerTable(table);
        });

        return card;
    }

    private void selectionnerTable(TableRestaurant table) {
        tableSelectionneeId = table.getId();
        txtNumero.setText(String.valueOf(table.getNumeroTable()));
        txtCapacite.setText(String.valueOf(table.getCapacite()));
        cbStatut.setValue(table.getStatut());
        lblSelection.setText("Table sélectionnée : " + table.getNumeroTable());
        rafraichirTables();
    }

    private void clearForm() {
        tableSelectionneeId = null;
        txtNumero.clear();
        txtCapacite.clear();
        cbStatut.setValue("Libre");
        lblSelection.setText("Aucune table sélectionnée");
        rafraichirTables();
    }

    private void supprimerDepuisCarte(TableRestaurant table) {
        tableSelectionneeId = table.getId();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer la " + (table.getStatut().equalsIgnoreCase("Occupée") ? "table occupée" : "table") + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    tableDAO.supprimerTable(tableSelectionneeId);
                    clearForm();
                } catch (IllegalStateException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });
    }

    private void appliquerStyleStatut(VBox card, String statut) {
        if ("Libre".equalsIgnoreCase(statut)) {
            card.getStyleClass().add("table-card-free");
        } else if ("Occupée".equalsIgnoreCase(statut)) {
            card.getStyleClass().add("table-card-busy");
        } else {
            card.getStyleClass().add("table-card-reserved");
        }
    }

    private void appliquerStyleBadge(Label label, String statut) {
        if ("Libre".equalsIgnoreCase(statut)) {
            label.getStyleClass().add("table-status-free");
        } else if ("Occupée".equalsIgnoreCase(statut)) {
            label.getStyleClass().add("table-status-busy");
        } else {
            label.getStyleClass().add("table-status-reserved");
        }
    }
}
