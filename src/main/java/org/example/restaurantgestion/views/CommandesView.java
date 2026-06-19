package org.example.restaurantgestion.views;

import javafx.animation.RotateTransition;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.FactureDAO;
import org.example.restaurantgestion.dao.PaiementDAO;
import org.example.restaurantgestion.models.Commande;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommandesView extends VBox {

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final TableView<Commande> table = new TableView<>();
    private final ObservableList<Commande> data = FXCollections.observableArrayList();
    private String statutSelectionne = "Tout";
    private Integer selectedTableId = null;

    public CommandesView() {
        this.getStyleClass().add("erp-page");

        // --- HEADER ---
        HBox header = new HBox();
        header.getStyleClass().add("erp-page-header");

        Label lblTitle = new Label("Commandes & Facturation");
        lblTitle.getStyleClass().add("erp-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNew = new Button("+ Nouvelle commande");
        btnNew.getStyleClass().add("button-primary");
        btnNew.setOnAction(e -> {
            new CommandeDialog().showAndWait();
            chargerDonnees();
        });

        Label arrowIcon = new Label("↻");
        arrowIcon.getStyleClass().add("refresh-icon");

        Button btnRefresh = new Button();
        btnRefresh.setGraphic(arrowIcon);
        btnRefresh.getStyleClass().add("button-refresh");
        btnRefresh.setOnAction(e -> {
            RotateTransition spin = new RotateTransition(Duration.millis(400), arrowIcon);
            spin.setFromAngle(0);
            spin.setToAngle(360);
            spin.setCycleCount(1);
            spin.play();
            spin.setOnFinished(ev -> chargerDonnees());
        });

        header.getChildren().addAll(lblTitle, spacer, btnRefresh, btnNew);

        // --- TOOLBAR / FILTRES ---
        HBox toolbar = new HBox();
        toolbar.getStyleClass().add("erp-toolbar");

        ajouterFiltre(toolbar, "Toutes", "Tout");
        ajouterFiltre(toolbar, "En cours", "En cours");
        ajouterFiltre(toolbar, "Payées", "Payée");

        // --- CORPS : TABLEAU ---
        VBox body = new VBox();
        body.getStyleClass().add("erp-body");
        VBox.setVgrow(body, Priority.ALWAYS);

        setupTable();
        body.getChildren().add(table);

        this.getChildren().addAll(header, toolbar, body);
        chargerDonnees();
    }

    private void ajouterFiltre(HBox container, String label, String statusKey) {
        ToggleButton btn = new ToggleButton(label);
        btn.getStyleClass().add("erp-filter-btn");
        btn.setOnAction(e -> {
            container.getChildren().forEach(n -> {
                if (n instanceof ToggleButton tb && tb != btn) tb.setSelected(false);
            });
            btn.setSelected(true);
            statutSelectionne = statusKey;
            appliquerFiltre();
        });
        if (statusKey.equals("Tout")) btn.setSelected(true);
        container.getChildren().add(btn);
    }

    private void setupTable() {
        table.getStyleClass().add("erp-table");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Commande, Number> colId = new TableColumn<>("N°");
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getIdCommande()));
        colId.setPrefWidth(60);
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                setText("#" + id.intValue());
                setStyle("-fx-font-weight: 700; -fx-text-fill: #111827;");
            }
        });

        TableColumn<Commande, String> colTable = new TableColumn<>("Table");
        colTable.setCellValueFactory(cd -> {
            Commande c = cd.getValue();
            String t = c.getTable() != null ? "Table " + c.getTable().getNumeroTable() : "À emporter";
            return new SimpleStringProperty(t);
        });
        colTable.setPrefWidth(120);

        TableColumn<Commande, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getDateCommande()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
        );
        colDate.setPrefWidth(140);

        TableColumn<Commande, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatut()));
        colStatut.setPrefWidth(120);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statut.toUpperCase());
                String cls = switch (statut.toLowerCase()) {
                    case "en cours" -> "erp-badge-encours";
                    case "payé" -> "erp-badge-paye";
                    default -> "erp-badge-pret";
                };
                badge.getStyleClass().addAll("erp-badge", cls);
                setGraphic(badge);
            }
        });

        TableColumn<Commande, Number> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getTotal()));
        colTotal.setPrefWidth(120);
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setText(null); return; }
                setText(String.format("%.2f FCFA", total.doubleValue()));
                getStyleClass().add("erp-price");
            }
        });

        TableColumn<Commande, Void> colActions = new TableColumn<>("");
        colActions.setPrefWidth(180);
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir = new Button("Voir");
            private final Button btnPay = new Button("Encaisser");
            private final Label lblPaye = new Label("Payée");
            private final HBox box = new HBox(6);

            {
                box.setAlignment(Pos.CENTER);
                btnVoir.getStyleClass().addAll("erp-action-btn");
                btnPay.getStyleClass().addAll("erp-action-btn", "erp-action-btn-success");
                lblPaye.getStyleClass().addAll("erp-badge", "erp-badge-paye");
                lblPaye.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");

                btnVoir.setOnAction(e -> {
                    Commande c = getTableView().getItems().get(getIndex());
                    CommandeDAO.genererFactureTXT(c);
                    String content = CommandeDAO.lireFactureTXT(c.getIdCommande());

                    TextArea area = new TextArea(content);
                    area.setEditable(false);
                    area.setPrefSize(480, 360);
                    area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-padding: 16px;");

                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Facture — Commande #" + c.getIdCommande());
                    dialog.getDialogPane().setContent(area);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                    dialog.showAndWait();
                });

                btnPay.setOnAction(e -> {
                    Commande c = getTableView().getItems().get(getIndex());

                    EncaisseDialog dialog = new EncaisseDialog(c);
                    dialog.showAndWait();
                    if (!dialog.estValide()) return;

                    new PaiementDAO().enregistrerPaiement(c.getIdCommande(), dialog.getModePaiement(), dialog.getMontantRecu());
                    new FactureDAO().creerFacture(c.getIdCommande(), c.getTotal());
                    CommandeDAO.genererFactureTXT(c);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Commande #" + c.getIdCommande() + " payée — " + String.format("%,.0f", c.getTotal()) + " FCFA",
                        ButtonType.OK);
                    alert.showAndWait();
                    chargerDonnees();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Commande c = getTableView().getItems().get(getIndex());
                box.getChildren().clear();
                box.getChildren().add(btnVoir);
                if (c != null && !"Payée".equalsIgnoreCase(c.getStatut())) {
                    box.getChildren().add(btnPay);
                } else {
                    box.getChildren().add(lblPaye);
                }
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colTable, colDate, colStatut, colTotal, colActions);
        table.setItems(data);
    }

    private void appliquerFiltre() {
        data.clear();
        List<Commande> liste = commandeDAO.getHistoriqueVentes();
        if (liste == null) return;
        liste.stream()
            .filter(c -> selectedTableId == null || (c.getTable() != null && c.getTable().getId() == selectedTableId))
            .filter(c -> statutSelectionne.equals("Tout") || c.getStatut().equalsIgnoreCase(statutSelectionne))
            .forEach(data::add);
    }

    public void chargerDonnees() {
        appliquerFiltre();
    }

    public void selectionnerTable(int idTable) {
        this.selectedTableId = idTable;
        chargerDonnees();
    }
}
