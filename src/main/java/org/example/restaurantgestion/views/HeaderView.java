package org.example.restaurantgestion.views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class HeaderView extends HBox {

    private Runnable onRefreshAction;

    public HeaderView() {
        this(null);
    }

    public HeaderView(Runnable onRefresh) {
        this.onRefreshAction = onRefresh;

        this.getStyleClass().add("header-view");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(0);

        // --- LEFT : Brand (logo SVG + titre) ---
        HBox brandBox = new HBox(14);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Pane logoPane = SidebarView.chargerSvg(
            SidebarView.SVG_DIR + "logo.svg", "header-logo-icon", 28
        );

        Label lblTitle = new Label("L'ÉLIXIR GOURMAND");
        lblTitle.getStyleClass().add("header-title");

        Label lblBadge = new Label("CONSOLE");
        lblBadge.getStyleClass().add("header-badge");

        brandBox.getChildren().addAll(logoPane, lblTitle, lblBadge);

        // --- SPACER ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- RIGHT : Actions ---
        HBox rightBox = new HBox(12);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnRefresh = new Button("↻");
        btnRefresh.getStyleClass().addAll("header-btn", "header-btn-refresh");
        btnRefresh.setTooltip(new Tooltip("Rafraîchir les données"));
        btnRefresh.setOnAction(e -> {
            if (onRefreshAction != null) onRefreshAction.run();
        });

        StackPane notifContainer = new StackPane();
        notifContainer.setAlignment(Pos.CENTER);
        Button btnNotif = new Button("🔔");
        btnNotif.getStyleClass().addAll("header-btn", "header-btn-notif");
        btnNotif.setTooltip(new Tooltip("Notifications"));

        Region notifBadge = new Region();
        notifBadge.getStyleClass().add("header-notif-badge");
        StackPane.setAlignment(notifBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(notifBadge, new javafx.geometry.Insets(4, 4, 0, 0));
        notifContainer.getChildren().addAll(btnNotif, notifBadge);

        // --- Profil utilisateur (SVG avatar) ---
        HBox profileBox = new HBox(10);
        profileBox.getStyleClass().add("header-profile-box");
        profileBox.setAlignment(Pos.CENTER_LEFT);

        Pane avatarPane = SidebarView.chargerSvg(
            SidebarView.SVG_DIR + "profil.svg", "header-avatar-svg", 24
        );

        Label lblProfileName = new Label("Administrateur");
        lblProfileName.getStyleClass().add("header-profile-name");

        Label lblArrow = new Label("▾");
        lblArrow.getStyleClass().add("header-profile-arrow");

        profileBox.getChildren().addAll(avatarPane, lblProfileName, lblArrow);

        rightBox.getChildren().addAll(btnRefresh, notifContainer, profileBox);

        this.getChildren().addAll(brandBox, spacer, rightBox);
    }

    public void setOnRefresh(Runnable onRefresh) {
        this.onRefreshAction = onRefresh;
    }
}
