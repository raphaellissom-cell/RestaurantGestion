package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.PaiementDAO;
import org.example.restaurantgestion.models.Commande;
import java.util.List;
import java.util.stream.Collectors;
import org.example.restaurantgestion.util.AlertUtil;

public class PaiementsView extends VBox {

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final TableView<Commande> tableCommandes;
    private final ObservableList<Commande> commandesList = FXCollections.observableArrayList();

    private final Label lblCommandeSelectionnee;
    private final Label lblMontantTotal;
    private final ComboBox<String> cbModesPaiement;
    private final TextField txtMontantRecu;
    private final Label lblRenduMonnaie;

    private Commande activeCommande = null;

    public PaiementsView() {
        this.setSpacing(20);
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("💰 Caisse & Paiements");
        title.getStyleClass().add("section-title");

        HBox mainLayout = new HBox(20);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // Partie Gauche : Liste des commandes actives
        VBox leftBox = new VBox(10);
        leftBox.setPrefWidth(450);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        Label lblListTitle = new Label("Commandes Actives (à payer)");
        lblListTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #8E8E93;");

        tableCommandes = new TableView<>();
        tableCommandes.getStyleClass().add("table-view");
        tableCommandes.setPrefHeight(350);

        TableColumn<Commande, Integer> colId = new TableColumn<>("N° Cmd");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCommande"));
        colId.setPrefWidth(70);

        TableColumn<Commande, Integer> colTable = new TableColumn<>("Table");
        colTable.setCellValueFactory(new PropertyValueFactory<>("idTable"));
        colTable.setPrefWidth(70);

        TableColumn<Commande, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(90);

        TableColumn<Commande, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        tableCommandes.getColumns().addAll(colId, colTable, colTotal, colStatut);
        tableCommandes.setItems(commandesList);

        // Sélection d'une ligne dans le tableau
        tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectionnerCommande(newSelection);
            }
        });

        leftBox.getChildren().addAll(lblListTitle, tableCommandes);

        // Partie Droite : Formulaire de Paiement
        VBox rightBox = new VBox(15);
        rightBox.getStyleClass().add("order-cart-panel");
        rightBox.setPrefWidth(320);
        rightBox.setMinWidth(300);

        Label lblFormTitle = new Label("Enregistrement du Paiement");
        lblFormTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);

        Label lblCmdLabel = new Label("Commande :");
        lblCmdLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-font-size: 12px;");
        grid.add(lblCmdLabel, 0, 0);
        lblCommandeSelectionnee = new Label("Aucune sélectionnée");
        lblCommandeSelectionnee.setStyle("-fx-font-weight: 700; -fx-text-fill: #111827; -fx-font-size: 13px;");
        grid.add(lblCommandeSelectionnee, 1, 0);

        Label lblTotalLabel = new Label("Total :");
        lblTotalLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-font-size: 12px;");
        grid.add(lblTotalLabel, 0, 1);
        lblMontantTotal = new Label("0.00 FCFA");
        lblMontantTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #F07C33;");
        grid.add(lblMontantTotal, 1, 1);

        Label lblModeLabel = new Label("Mode :");
        lblModeLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-font-size: 12px;");
        grid.add(lblModeLabel, 0, 2);
        cbModesPaiement = new ComboBox<>();
        cbModesPaiement.getItems().addAll("Espèces", "Carte Bancaire", "Mobile Money");
        cbModesPaiement.setValue("Carte Bancaire");
        grid.add(cbModesPaiement, 1, 2);

        Label lblRecuLabel = new Label("Montant Reçu :");
        lblRecuLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-font-size: 12px;");
        grid.add(lblRecuLabel, 0, 3);
        txtMontantRecu = new TextField();
        txtMontantRecu.setPromptText("Saisir montant...");
        txtMontantRecu.textProperty().addListener((obs, oldText, newText) -> calculerRenduMonnaie());
        grid.add(txtMontantRecu, 1, 3);

        Label lblRenduLabel = new Label("Monnaie à Rendre :");
        lblRenduLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-font-size: 12px;");
        grid.add(lblRenduLabel, 0, 4);
        lblRenduMonnaie = new Label("0.00 FCFA");
        lblRenduMonnaie.setStyle("-fx-font-weight: 700; -fx-text-fill: #059669; -fx-font-size: 15px;");
        grid.add(lblRenduMonnaie, 1, 4);

        Button btnPayer = new Button("💵 Valider l'Encaissement");
        btnPayer.getStyleClass().add("button-primary");
        btnPayer.setMaxWidth(Double.MAX_VALUE);
        btnPayer.setOnAction(e -> validerPaiement());

        Button btnFacture = new Button("📄 Imprimer Facture (PDF)");
        btnFacture.getStyleClass().add("button-secondary");
        btnFacture.setMaxWidth(Double.MAX_VALUE);
        btnFacture.setOnAction(e -> imprimerFacture());

        rightBox.getChildren().addAll(lblFormTitle, grid, btnPayer, btnFacture);

        mainLayout.getChildren().addAll(leftBox, rightBox);
        this.getChildren().addAll(title, mainLayout);

        chargerCommandesActives();
    }

    private void chargerCommandesActives() {
        commandesList.clear();
        List<Commande> all = commandeDAO.getHistoriqueVentes();
        // On ne filtre que les commandes "En cours" ou "En attente" ou fictives actives
        List<Commande> actives = all.stream()
                .filter(cmd -> !"Payée".equalsIgnoreCase(cmd.getStatut()) && !"Annulée".equalsIgnoreCase(cmd.getStatut()))
                .collect(Collectors.toList());
        commandesList.addAll(actives);
    }

    private void selectionnerCommande(Commande cmd) {
        activeCommande = cmd;
        lblCommandeSelectionnee.setText("Commande n°" + cmd.getIdCommande() + " (Table " + cmd.getIdTable() + ")");
        lblMontantTotal.setText(String.format("%.2f FCFA", cmd.getTotal()));
        txtMontantRecu.clear();
        lblRenduMonnaie.setText("0.00 FCFA");
    }

    private void calculerRenduMonnaie() {
        if (activeCommande == null) return;
        try {
            double recu = Double.parseDouble(txtMontantRecu.getText().trim());
            double total = activeCommande.getTotal();
            double rendu = recu - total;
            if (rendu >= 0) {
                lblRenduMonnaie.setText(String.format("%.2f FCFA", rendu));
                lblRenduMonnaie.setStyle("-fx-text-fill: #059669; -fx-font-weight: 700; -fx-font-size: 15px;");
            } else {
                lblRenduMonnaie.setText("Montant insuffisant");
                lblRenduMonnaie.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-font-size: 13px;");
            }
        } catch (NumberFormatException e) {
            lblRenduMonnaie.setText("0.00 FCFA");
            lblRenduMonnaie.setStyle("-fx-text-fill: #059669; -fx-font-weight: 700; -fx-font-size: 15px;");
        }
    }

    private void validerPaiement() {
        if (activeCommande == null) {
            AlertUtil.showWarning("Veuillez sélectionner une commande à payer !");
            return;
        }

        try {
            double montantRecu = Double.parseDouble(txtMontantRecu.getText().trim());
            if (montantRecu < activeCommande.getTotal()) {
                AlertUtil.showWarning("Le montant reçu est insuffisant.");
                return;
            }

            paiementDAO.enregistrerPaiement(activeCommande.getIdCommande(), cbModesPaiement.getValue(), montantRecu);
            AlertUtil.showInfo("Paiement de " + String.format("%.2f FCFA", activeCommande.getTotal()) + " validé avec succès !");

            activeCommande = null;
            lblCommandeSelectionnee.setText("Aucune");
            lblMontantTotal.setText("0.00 FCFA");
            txtMontantRecu.clear();
            lblRenduMonnaie.setText("0.00 FCFA");

            chargerCommandesActives();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Le montant reçu est invalide.");
        } catch (IllegalStateException ex) {
            AlertUtil.showError("Impossible d'enregistrer le paiement : " + ex.getMessage());
        }
    }

    private void imprimerFacture() {
        if (activeCommande == null) {
            AlertUtil.showWarning("Veuillez sélectionner une commande pour imprimer la facture !");
            return;
        }
        CommandeDAO.genererFactureTXT(activeCommande);
        String content = CommandeDAO.lireFactureTXT(activeCommande.getIdCommande());

        TextArea area = new TextArea(content);
        area.setEditable(false);
        area.setPrefSize(480, 360);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-padding: 16px;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Facture — Commande #" + activeCommande.getIdCommande());
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
