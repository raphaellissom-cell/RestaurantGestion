package org.example.restaurantgestion.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.example.restaurantgestion.dao.CommandeDAO;
import org.example.restaurantgestion.dao.IngredientDAO;
import org.example.restaurantgestion.dao.TableDAO;
import org.example.restaurantgestion.models.Commande;
import org.example.restaurantgestion.models.TableRestaurant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardView extends VBox {

    private final MainView mainView;
    private final TableDAO tableDAO = new TableDAO();
    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();

    private final Label lblTablesLibres;
    private final Label lblChiffreAffaires;
    private final Label lblCommandesEnCours;
    private final Label lblAlertesStock;
    private final Label lblLastRefresh;
    private final VBox recentOrdersContainer;
    private final XYChart.Series<String, Number> serieCA;
    private final ObservableList<PieChart.Data> pieData;

    public DashboardView(MainView mainView) {
        this.mainView = mainView;
        this.getStyleClass().add("erp-page");

        // --- HEADER ---
        HBox header = new HBox();
        header.getStyleClass().add("erp-page-header");

        Label lblTitle = new Label("Tableau de Bord");
        lblTitle.getStyleClass().add("erp-page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblLastRefresh = new Label();
        lblLastRefresh.getStyleClass().add("dashboard-refresh-label");

        Button btnRefresh = new Button("↻");
        btnRefresh.getStyleClass().add("erp-action-btn");
        btnRefresh.setTooltip(new Tooltip("Rafraîchir les données"));
        btnRefresh.setOnAction(e -> rafraichirDonnees());

        header.getChildren().addAll(lblTitle, spacer, lblLastRefresh, btnRefresh);

        // --- BODY ---
        VBox body = new VBox(20);
        body.getStyleClass().add("erp-body");

        // --- 1. KPI ROW ---
        HBox kpiRow = new HBox(16);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        VBox cardTables = creerKPICard("TABLES DISPONIBLES", "0 / 0", "#10B981", "🏠");
        lblTablesLibres = (Label) cardTables.getChildren().get(1);

        VBox cardCA = creerKPICard("CHIFFRE D'AFFAIRES", "0 FCFA", "#F07C33", "💰");
        lblChiffreAffaires = (Label) cardCA.getChildren().get(1);

        VBox cardEnCours = creerKPICard("COMMANDES EN COURS", "0", "#3B82F6", "📋");
        lblCommandesEnCours = (Label) cardEnCours.getChildren().get(1);

        VBox cardAlertes = creerKPICard("ALERTES STOCK", "0", "#EF4444", "⚠️");
        lblAlertesStock = (Label) cardAlertes.getChildren().get(1);

        kpiRow.getChildren().addAll(cardTables, cardCA, cardEnCours, cardAlertes);

        // --- 2. CHARTS ROW ---
        HBox chartsRow = new HBox(16);
        chartsRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(chartsRow, Priority.ALWAYS);

        // BarChart CA 7 jours
        VBox chartCA = creerSectionChart("ÉVOLUTION DU CHIFFRE D'AFFAIRES (7 JOURS)");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("FCFA");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.getStyleClass().add("dashboard-chart");
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(220);
        XYChart.Series<String, Number> sCA = new XYChart.Series<>();
        barChart.getData().add(sCA);
        serieCA = sCA;
        chartCA.getChildren().add(barChart);

        // PieChart occupation tables
        VBox chartTables = creerSectionChart("OCCUPATION DES TABLES");
        PieChart pieChart = new PieChart();
        pieChart.getStyleClass().add("dashboard-chart");
        pieChart.setAnimated(false);
        pieChart.setLegendVisible(true);
        pieChart.setPrefHeight(220);
        pieChart.setLabelsVisible(false);
        ObservableList<PieChart.Data> pData = FXCollections.observableArrayList();
        pieChart.setData(pData);
        pieData = pData;
        chartTables.getChildren().add(pieChart);

        chartsRow.getChildren().addAll(chartCA, chartTables);

        // --- 3. RECENT ORDERS ---
        VBox recentSection = new VBox(10);
        recentSection.getStyleClass().add("dashboard-section");

        HBox recentHeader = new HBox();
        recentHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblRecentTitle = new Label("DERNIÈRES COMMANDES");
        lblRecentTitle.getStyleClass().add("dashboard-section-title");
        recentHeader.getChildren().add(lblRecentTitle);

        recentOrdersContainer = new VBox();
        recentOrdersContainer.setSpacing(0);

        recentSection.getChildren().addAll(recentHeader, recentOrdersContainer);

        body.getChildren().addAll(kpiRow, chartsRow, recentSection);

        this.getChildren().addAll(header, body);
        rafraichirDonnees();
    }

    private VBox creerKPICard(String titre, String valeur, String couleur, String emoji) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-kpi");
        card.setPrefWidth(220);
        card.setMinWidth(200);

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 18px;");

        Label lblTitre = new Label(titre);
        lblTitre.getStyleClass().add("dashboard-kpi-title");

        top.getChildren().addAll(icon, lblTitre);

        Label lblValeur = new Label(valeur);
        lblValeur.getStyleClass().add("dashboard-kpi-value");
        lblValeur.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 26px; -fx-font-weight: 900;");

        card.getChildren().addAll(top, lblValeur);
        return card;
    }

    private VBox creerSectionChart(String titre) {
        VBox section = new VBox(10);
        section.getStyleClass().add("dashboard-section");
        HBox.setHgrow(section, Priority.ALWAYS);

        Label lbl = new Label(titre);
        lbl.getStyleClass().add("dashboard-section-title");
        section.getChildren().add(lbl);
        return section;
    }

    public void rafraichirDonnees() {
        lblLastRefresh.setText("Dernière màj : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        List<TableRestaurant> tables = tableDAO.getAllTables() != null ? tableDAO.getAllTables() : List.of();
        List<Commande> commandes = commandeDAO.getHistoriqueVentes() != null ? commandeDAO.getHistoriqueVentes() : List.of();

        // --- KPIs ---
        long libres = tables.stream().filter(t -> "Libre".equalsIgnoreCase(t.getStatut())).count();
        lblTablesLibres.setText(libres + " / " + tables.size());

        double caJour = 0.0;
        int enCours = 0;
        LocalDate today = LocalDate.now();
        for (Commande cmd : commandes) {
            if (cmd.getDateCommande().toLocalDate().equals(today)
                && "Payée".equalsIgnoreCase(cmd.getStatut())) {
                caJour += cmd.getTotal();
            }
            if ("En cours".equalsIgnoreCase(cmd.getStatut())) {
                enCours++;
            }
        }
        lblChiffreAffaires.setText(String.format("%,.0f FCFA", caJour));
        lblCommandesEnCours.setText(String.valueOf(enCours));

        long stockAlert = ingredientDAO.getNbAlertes();
        lblAlertesStock.setText(String.valueOf(stockAlert));

        // --- BAR CHART CA 7 jours ---
        serieCA.getData().clear();
        Map<LocalDate, Double> caParJour = new TreeMap<>();
        for (int i = 6; i >= 0; i--) caParJour.put(today.minusDays(i), 0.0);
        for (Commande cmd : commandes) {
            LocalDate d = cmd.getDateCommande().toLocalDate();
            if (caParJour.containsKey(d) && "Payée".equalsIgnoreCase(cmd.getStatut())) {
                caParJour.put(d, caParJour.get(d) + cmd.getTotal());
            }
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (Map.Entry<LocalDate, Double> e : caParJour.entrySet()) {
            serieCA.getData().add(new XYChart.Data<>(e.getKey().format(fmt), e.getValue()));
        }

        // --- PIE CHART tables ---
        pieData.clear();
        Map<String, Long> occ = tables.stream().collect(Collectors.groupingBy(TableRestaurant::getStatut, Collectors.counting()));
        if (occ.isEmpty()) occ.put("Aucune", 1L);
        for (Map.Entry<String, Long> e : occ.entrySet()) {
            pieData.add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
        }

        // --- RECENT ORDERS ---
        recentOrdersContainer.getChildren().clear();
        List<Commande> recentes = commandes.stream()
            .sorted(Comparator.comparing(Commande::getDateCommande).reversed())
            .limit(8).toList();

        if (recentes.isEmpty()) {
            Label empty = new Label("Aucune commande récente.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF; -fx-padding: 20px; -fx-alignment: center;");
            empty.setMaxWidth(Double.MAX_VALUE);
            recentOrdersContainer.getChildren().add(empty);
            return;
        }

        // Header row
        HBox headerRow = creerLigneCommande("#", "Table", "Date", "Statut", "Total", true);
        recentOrdersContainer.getChildren().add(headerRow);

        for (Commande cmd : recentes) {
            String tableStr = cmd.getTable() != null ? "T" + cmd.getTable().getNumeroTable() : "À emp.";
            HBox row = creerLigneCommande(
                "#" + cmd.getIdCommande(),
                tableStr,
                cmd.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                cmd.getStatut(),
                String.format("%,.0f FCFA", cmd.getTotal()),
                false
            );
            row.setOnMouseClicked(e -> mainView.navigateTo("Commandes"));
            recentOrdersContainer.getChildren().add(row);
        }
    }

    private HBox creerLigneCommande(String id, String table, String date, String statut, String total, boolean isHeader) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(isHeader ? 8 : 10, 12, isHeader ? 8 : 10, 12));
        row.setStyle(isHeader
            ? "-fx-background-color: #F9FAFB; -fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1px 0;"
            : "-fx-background-color: #FFFFFF; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1px 0;");
        if (!isHeader) {
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #FFF7F0; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1px 0;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1px 0;"));
            row.setCursor(javafx.scene.Cursor.HAND);
        }

        int colWidth = 60;
        Label lblId = new Label(id);
        lblId.setPrefWidth(50);
        lblId.setStyle("-fx-font-weight: " + (isHeader ? 700 : 600) + "; -fx-font-size: " + (isHeader ? 11 : 12) + "px; -fx-text-fill: " + (isHeader ? "#6B7280" : "#111827") + ";");

        Label lblTable = new Label(table);
        lblTable.setPrefWidth(colWidth);
        lblTable.setStyle("-fx-font-weight: " + (isHeader ? 700 : 600) + "; -fx-font-size: " + (isHeader ? 11 : 12) + "px; -fx-text-fill: " + (isHeader ? "#6B7280" : "#111827") + ";");

        Label lblDate = new Label(date);
        lblDate.setPrefWidth(110);
        lblDate.setStyle("-fx-font-size: " + (isHeader ? 11 : 12) + "px; -fx-text-fill: " + (isHeader ? "#6B7280" : "#6B7280") + ";");

        Label lblStatut = new Label(statut);
        lblStatut.setPrefWidth(100);
        String badgeCls = switch (statut.toLowerCase()) {
            case "en cours" -> "erp-badge-encours";
            case "payé", "payée", "paye" -> "erp-badge-paye";
            default -> "erp-badge-encours";
        };
        if (isHeader) {
            lblStatut.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6B7280;");
        } else {
            lblStatut.getStyleClass().addAll("erp-badge", badgeCls);
        }

        Label lblTotal = new Label(total);
        lblTotal.setPrefWidth(100);
        lblTotal.setAlignment(Pos.CENTER_RIGHT);
        lblTotal.setStyle("-fx-font-weight: " + (isHeader ? 700 : 800) + "; -fx-font-size: " + (isHeader ? 11 : 12) + "px; -fx-text-fill: " + (isHeader ? "#6B7280" : "#111827") + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lblId, lblTable, lblDate, lblStatut, spacer, lblTotal);
        return row;
    }
}
