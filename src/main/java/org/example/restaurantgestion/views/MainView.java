package org.example.restaurantgestion.views;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

public class MainView extends BorderPane {

    private final SidebarView sidebar;
    private final HeaderView header;
    private final StackPane contentArea;
    private final Map<String, Node> viewCache = new HashMap<>();

    public MainView() {
        this.getStylesheets().add(
            getClass().getResource("/org/example/restaurantgestion/css/style.css").toExternalForm()
        );

        // S'assurer que le BorderPane occupe tout l'espace disponible
        this.setMinSize(0, 0);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.header = new HeaderView(this::rafraichirVueCourante);
        this.sidebar = new SidebarView(this::navigateTo);

        this.contentArea = new StackPane();
        this.contentArea.getStyleClass().add("content-view");
        this.contentArea.setMinSize(0, 0);
        this.contentArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ScrollPane pour le contenu central — fond transparent
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-border-color: transparent;"
        );
        // Le ScrollPane doit s'étirer pour remplir toute la zone centrale
        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.setTop(header);
        this.setLeft(sidebar);
        this.setCenter(scrollPane);

        navigateTo("Dashboard");
    }

    private void rafraichirVueCourante() {
        if (contentArea.getChildren().isEmpty()) return;
        Node view = contentArea.getChildren().get(0);
        if (view instanceof DashboardView dashboardView) {
            dashboardView.rafraichirDonnees();
        } else if (view instanceof TablesView tablesView) {
            tablesView.rafraichirTables();
        } else if (view instanceof CommandesView commandesView) {
            commandesView.chargerDonnees();
        }
    }

    public void navigateTo(String viewName) {
        Node view = viewCache.get(viewName);
        if (view == null) {
            view = createViewInstance(viewName);
            viewCache.put(viewName, view);
        }
        sidebar.setActiveButton(viewName);
        contentArea.getChildren().setAll(view);
    }


    public void ouvrirCommandePourTable(int idTable) {
        Node view = viewCache.get("Commandes");
        CommandesView commandesView;
        if (view instanceof CommandesView existingView) {
            commandesView = existingView;
        } else {
            commandesView = new CommandesView();
            view = commandesView;
            viewCache.put("Commandes", commandesView);
        }
        commandesView.selectionnerTable(idTable);
        sidebar.setActiveButton("Commandes");
        contentArea.getChildren().setAll(view);
    }

    private Node createViewInstance(String viewName) {
        switch (viewName) {
            case "Dashboard":  return new DashboardView(this);
            case "Menus":      return new MenuView();
            case "Commandes":  return new CommandesView();
            case "Factures":   return new FacturesView();
            case "Tables":     return new TablesView(this);
            case "Paiements":  return new PaiementsView();
            case "Stocks":     return new StocksView();
            case "Ventes":     return new VentesView();
            default:
                javafx.scene.control.Label lbl =
                    new javafx.scene.control.Label("Vue en cours de développement : " + viewName);
                lbl.getStyleClass().add("section-title");
                VBox placeholder = new VBox(lbl);
                placeholder.setStyle("-fx-padding: 40px; -fx-alignment: center;");
                return placeholder;
        }
    }
}
