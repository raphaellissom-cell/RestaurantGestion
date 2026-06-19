package org.example.restaurantgestion.views;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.FactureDAO;
import org.example.restaurantgestion.models.Facture;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FacturesView extends VBox {

    private final FactureDAO factureDAO = new FactureDAO();
    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final TableView<Facture> table = new TableView<>();
    private final ObservableList<Facture> data = FXCollections.observableArrayList();

    public FacturesView() {
        this.getStyleClass().add("erp-page");

        HBox header = new HBox();
        header.getStyleClass().add("erp-page-header");

        Label lblTitle = new Label("Factures");
        lblTitle.getStyleClass().add("erp-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("⟳ Actualiser");
        btnRefresh.getStyleClass().add("button-primary");
        btnRefresh.setOnAction(e -> chargerDonnees());

        header.getChildren().addAll(lblTitle, spacer, btnRefresh);

        VBox body = new VBox();
        body.getStyleClass().add("erp-body");
        VBox.setVgrow(body, Priority.ALWAYS);

        setupTable();
        body.getChildren().add(table);

        this.getChildren().addAll(header, body);
        chargerDonnees();
    }

    private void setupTable() {
        table.getStyleClass().add("erp-table");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Facture, Number> colId = new TableColumn<>("N° Facture");
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getId()));
        colId.setPrefWidth(100);
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                setText("F" + id.intValue());
                setStyle("-fx-font-weight: 700; -fx-text-fill: #111827;");
            }
        });

        TableColumn<Facture, Number> colCmd = new TableColumn<>("N° Commande");
        colCmd.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getCommande().getIdCommande()));
        colCmd.setPrefWidth(120);
        colCmd.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                setText("#" + id.intValue());
                setStyle("-fx-font-weight: 600; -fx-text-fill: #374151;");
            }
        });

        TableColumn<Facture, String> colTable = new TableColumn<>("Table");
        colTable.setCellValueFactory(cd -> {
            Facture f = cd.getValue();
            String t = f.getCommande().getTable() != null
                ? "Table " + f.getCommande().getTable().getNumeroTable()
                : "À emporter";
            return new SimpleStringProperty(t);
        });
        colTable.setPrefWidth(120);

        TableColumn<Facture, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getDateGeneration()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
        );
        colDate.setPrefWidth(140);

        TableColumn<Facture, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getTotal()));
        colTotal.setPrefWidth(120);
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setText(null); return; }
                setText(String.format("%,.0f FCFA", total.doubleValue()));
                getStyleClass().add("erp-price");
            }
        });

        TableColumn<Facture, Void> colActions = new TableColumn<>("");
        colActions.setPrefWidth(160);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnTxt = new Button("TXT");
            {
                btnVoir.getStyleClass().addAll("erp-action-btn");
                btnTxt.getStyleClass().addAll("erp-action-btn");
                btnVoir.setOnAction(e -> {
                    Facture f = getTableView().getItems().get(getIndex());
                    CommandeDAO.genererFactureTXT(f.getCommande());
                    String content = CommandeDAO.lireFactureTXT(f.getCommande().getIdCommande());

                    TextArea area = new TextArea(content);
                    area.setEditable(false);
                    area.setPrefSize(480, 360);
                    area.getStyleClass().add("erp-table");
                    area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-padding: 16px;");

                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Facture F" + f.getId() + " — Commande #" + f.getCommande().getIdCommande());
                    dialog.getDialogPane().setContent(area);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                    dialog.showAndWait();
                });
                btnTxt.setOnAction(e -> {
                    Facture f = getTableView().getItems().get(getIndex());
                    String nom = CommandeDAO.genererFactureTXT(f.getCommande());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Facture générée : " + nom, ButtonType.OK);
                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, btnVoir, btnTxt);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colCmd, colTable, colDate, colTotal, colActions);
        table.setItems(data);
    }

    private void chargerDonnees() {
        data.clear();
        List<Facture> factures = factureDAO.getAllFactures();
        if (factures != null) {
            data.addAll(factures);
        }
    }
}
