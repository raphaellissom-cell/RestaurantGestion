package org.example.restaurantgestion.views;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.restaurantgestion.util.AlertUtil;
import org.example.restaurantgestion.models.Commande;

public class EncaisseDialog extends Stage {

    private final ComboBox<String> cbMode = new ComboBox<>();
    private final TextField txtMontant = new TextField();
    private final Label lblRendu = new Label("0 FCFA");
    private final Commande commande;
    private boolean valide = false;

    public EncaisseDialog(Commande commande) {
        this.commande = commande;
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Encaisser — Commande #" + commande.getIdCommande());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-font-size: 13px;");

        Label lblTotal = new Label("Total :");
        lblTotal.setStyle("-fx-font-weight: 700; -fx-text-fill: #F07C33; -fx-font-size: 18px;");
        Label lblTotalVal = new Label(String.format("%,.0f FCFA", commande.getTotal()));
        lblTotalVal.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #F07C33;");
        grid.add(lblTotal, 0, 0);
        grid.add(lblTotalVal, 1, 0);

        grid.add(new Label("Mode de paiement :"), 0, 1);
        cbMode.getItems().addAll("Espèces", "Carte Bancaire", "Mobile Money");
        cbMode.setValue("Espèces");
        cbMode.setPrefWidth(200);
        grid.add(cbMode, 1, 1);

        grid.add(new Label("Montant reçu :"), 0, 2);
        txtMontant.setPromptText("Saisir montant...");
        txtMontant.setPrefWidth(200);
        txtMontant.textProperty().addListener((obs, o, n) -> calculerRendu());
        grid.add(txtMontant, 1, 2);

        grid.add(new Label("Monnaie à rendre :"), 0, 3);
        lblRendu.setStyle("-fx-font-weight: 700; -fx-text-fill: #059669; -fx-font-size: 15px;");
        grid.add(lblRendu, 1, 3);

        Button btnValider = new Button("Valider l'encaissement");
        btnValider.getStyleClass().add("button-primary");
        btnValider.setMaxWidth(Double.MAX_VALUE);
        btnValider.setOnAction(e -> valider());
        grid.add(btnValider, 0, 4, 2, 1);

        Scene scene = new Scene(grid);
        scene.getStylesheets().add(
            getClass().getResource("/org/example/restaurantgestion/css/style.css").toExternalForm()
        );
        setScene(scene);
        setWidth(360);
        setHeight(280);
        setResizable(false);
    }

    private void calculerRendu() {
        try {
            double recu = Double.parseDouble(txtMontant.getText().trim());
            double diff = recu - commande.getTotal();
            if (diff >= 0) {
                lblRendu.setText(String.format("%,.0f FCFA", diff));
                lblRendu.setStyle("-fx-font-weight: 700; -fx-text-fill: #059669; -fx-font-size: 15px;");
            } else {
                lblRendu.setText("Montant insuffisant");
                lblRendu.setStyle("-fx-font-weight: 700; -fx-text-fill: #DC2626; -fx-font-size: 13px;");
            }
        } catch (NumberFormatException e) {
            lblRendu.setText("0 FCFA");
            lblRendu.setStyle("-fx-font-weight: 700; -fx-text-fill: #059669; -fx-font-size: 15px;");
        }
    }

    private void valider() {
        try {
            double montantRecu = Double.parseDouble(txtMontant.getText().trim());
            if (montantRecu < commande.getTotal()) {
                AlertUtil.showWarning("Montant insuffisant.");
                return;
            }
            valide = true;
            close();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Montant invalide.");
        }
    }

    public boolean estValide() { return valide; }
    public String getModePaiement() { return cbMode.getValue(); }
    public double getMontantRecu() { return Double.parseDouble(txtMontant.getText().trim()); }
    public Commande getCommande() { return commande; }
}
