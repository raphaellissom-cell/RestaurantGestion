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

        cbStatut.getItems().addAll("Libre", "Occupée", "Réservée");
        cbStatut.setValue("Libre");

        if (table != null) {
            txtNumero.setText(String.valueOf(table.getNumeroTable()));
            txtCapacite.setText(String.valueOf(table.getCapacite()));
            cbStatut.setValue(table.getStatut());
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
        Scene scene = new Scene(root, 500, 350);
        setScene(scene);
        // Force explicit size and prevent resizing
        setWidth(500);
        setHeight(350);
        setMinWidth(500);
        setMinHeight(350);
        setResizable(false);
        sizeToScene();
    }

    private void onConfirm() {
        try {
            int numero = Integer.parseInt(txtNumero.getText().trim());
            int capacite = Integer.parseInt(txtCapacite.getText().trim());
            String statut = cbStatut.getValue();
            if (editingTable == null) {
                tableDAO.ajouterTable(new TableRestaurant(0, numero, capacite, statut));
                new Alert(Alert.AlertType.INFORMATION, "Table ajoutée avec succès.", ButtonType.OK).showAndWait();
            } else {
                editingTable.setNumeroTable(numero);
                editingTable.setCapacite(capacite);
                editingTable.setStatut(statut);
                tableDAO.modifierTable(editingTable);
                new Alert(Alert.AlertType.INFORMATION, "Table modifiée avec succès.", ButtonType.OK).showAndWait();
            }
            close();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Numéro et capacité doivent être des entiers.", ButtonType.OK).showAndWait();
        } catch (IllegalStateException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
