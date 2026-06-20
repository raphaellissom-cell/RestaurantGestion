package org.example.restaurantgestion.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.restaurantgestion.util.AlertUtil;
import org.example.restaurantgestion.dao.TableDAO;
import org.example.restaurantgestion.models.TableRestaurant;

/**
 * Popup dialog used by {@link TablesView} to add or edit a {@link TableRestaurant}.
 */
public class TableDialog extends Stage {
    private final TableDAO tableDAO = new TableDAO();
    private final TextField txtNumero = new TextField();
    private final TextField txtCapacite = new TextField();
    private final ComboBox<String> cbStatut = new ComboBox<>();
    private final TextField txtLocalisation = new TextField();
    private TableRestaurant editingTable = null;

    public TableDialog() {
        this(null);
    }

    public TableDialog(TableRestaurant table) {
        this.editingTable = table;
        initModality(Modality.APPLICATION_MODAL);
        setTitle(table == null ? "Ajouter une Table" : "Modifier la Table");
        VBox root = new VBox(12);
        root.setPadding(new Insets(16, 28, 28, 28));
        root.setStyle("-fx-background-color: #F9FAFB;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Numéro *"), txtNumero);
        form.addRow(1, new Label("Capacité *"), txtCapacite);
        form.addRow(2, new Label("Statut *"), cbStatut);
        form.addRow(3, new Label("Localisation"), txtLocalisation);

        cbStatut.getItems().addAll("Libre", "Occupée", "Réservée");
        cbStatut.setValue("Libre");

        if (table != null) {
            txtNumero.setText(String.valueOf(table.getNumeroTable()));
            txtCapacite.setText(String.valueOf(table.getCapacite()));
            cbStatut.setValue(table.getStatut());
            txtLocalisation.setText(table.getLocalisation());
        }

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button btnOk = new Button(table == null ? "Ajouter" : "Modifier");
        btnOk.getStyleClass().add("button-primary");
        btnOk.setOnAction(e -> onConfirm());
        Button btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setOnAction(e -> close());
        actions.getChildren().addAll(btnOk, btnCancel);

        root.getChildren().addAll(form, actions);
        Scene scene = new Scene(root, 500, 400);
        setScene(scene);
        // Force explicit size and prevent resizing
        setWidth(500);
        setHeight(400);
        setMinWidth(500);
        setMinHeight(400);
        setResizable(false);
        sizeToScene();
    }

    private void onConfirm() {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacite = Integer.parseInt(txtCapacite.getText().trim());
            String statut = cbStatut.getValue();
            String localisation = txtLocalisation.getText().trim();
            if (editingTable == null) {
                TableRestaurant nouvelle = new TableRestaurant();
                nouvelle.setNumeroTable(numero);
                nouvelle.setCapacite(capacite);
                nouvelle.setStatut(statut);
                nouvelle.setLocalisation(localisation.isEmpty() ? null : localisation);
                tableDAO.ajouterTable(nouvelle);
                AlertUtil.showInfo("Table ajoutée avec succès.");
            } else {
                editingTable.setNumeroTable(numero);
                editingTable.setCapacite(capacite);
                editingTable.setStatut(statut);
                editingTable.setLocalisation(localisation.isEmpty() ? null : localisation);
                tableDAO.modifierTable(editingTable);
                AlertUtil.showInfo("Table modifiée avec succès.");
            }
            close();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Numéro et capacité doivent être des entiers.");
        } catch (IllegalStateException ex) {
            AlertUtil.showError(ex.getMessage());
        }
    }
}
