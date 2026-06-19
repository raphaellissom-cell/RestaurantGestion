package org.example.restaurantgestion.views;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarView extends VBox {

    /** Dossier des SVG, résolu par rapport au répertoire de travail. */
    static final String SVG_DIR = resoudreCheminSvg();

    private static String resoudreCheminSvg() {
        // 1. Chemin relatif au répertoire de travail courant (quand lancé via Maven)
        File rel = new File("disp");
        if (rel.exists()) return rel.getAbsolutePath() + File.separator;
        // 2. Chemin absolu connu du projet
        File abs = new File("/home/jordan43/Bureau/projet/java/fenetre/test FX/RestaurantGestion/disp");
        if (abs.exists()) return abs.getAbsolutePath() + File.separator;
        return "disp" + File.separator;
    }

    private final List<NavEntry> navEntries = new ArrayList<>();
    private Button activeButton = null;
    private final Consumer<String> onNavigate;

    private static class NavEntry {
        final Button button;
        final String viewKey;
        NavEntry(Button button, String viewKey) {
            this.button = button;
            this.viewKey = viewKey;
        }
    }

    public SidebarView(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        this.getStyleClass().add("sidebar-view");
        this.setSpacing(0);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPrefWidth(80);
        this.setMinWidth(80);
        this.setMaxWidth(80);

        // ---- Bandeau de Marque Orange ----
        HBox brandContainer = new HBox();
        brandContainer.getStyleClass().add("sidebar-brand-container");
        brandContainer.setAlignment(Pos.CENTER);
        brandContainer.setPrefSize(80, 72);
        brandContainer.setMinSize(80, 72);
        brandContainer.setMaxSize(80, 72);

        Pane logoPane = chargerSvg(SVG_DIR + "logo.svg", "svg-icon", 28);
        brandContainer.getChildren().add(logoPane);
        brandContainer.setOnMouseClicked(e -> activateAndNavigate("Dashboard"));
        this.getChildren().add(brandContainer);

        // ---- Boutons de Navigation ----
        VBox navBox = new VBox(0);
        navBox.setAlignment(Pos.TOP_CENTER);
        navBox.setPrefWidth(80);
        navBox.setMinWidth(80);
        navBox.setMaxWidth(80);

        addNavButton(navBox, "logo.svg",     "Dashboard",  "Dashboard");
        addNavButton(navBox, "menu.svg",     "Menus",      "Menus");
        addNavButton(navBox, "table.svg",    "Tables",     "Tables");
        addNavButton(navBox, "commande.svg", "Commandes",  "Commandes");

        ScrollPane scrollPane = new ScrollPane(navBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-padding: 0;"
        );
        scrollPane.setPrefWidth(80);
        scrollPane.setMinWidth(80);
        scrollPane.setMaxWidth(80);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        this.getChildren().add(scrollPane);

        if (!navEntries.isEmpty()) {
            activateButton(navEntries.get(0).button);
        }
    }

    private void addNavButton(VBox container, String svgFileName, String text, String viewKey) {
        Button btn = new Button();
        btn.getStyleClass().add("sidebar-btn");

        VBox layout = new VBox(5);
        layout.setAlignment(Pos.CENTER);

        Pane svgPane = chargerSvg(SVG_DIR + svgFileName, "svg-icon", 22);

        Label lblText = new Label(text);
        lblText.setStyle("-fx-font-size: 10px; -fx-text-fill: inherit;");
        lblText.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(svgPane, lblText);
        btn.setGraphic(layout);

        NavEntry entry = new NavEntry(btn, viewKey);
        navEntries.add(entry);
        btn.setOnAction(e -> activateAndNavigate(viewKey));
        container.getChildren().add(btn);
    }

    private void activateAndNavigate(String viewKey) {
        for (NavEntry entry : navEntries) {
            if (entry.viewKey.equals(viewKey)) {
                activateButton(entry.button);
                break;
            }
        }
        onNavigate.accept(viewKey);
    }

    private void activateButton(Button target) {
        if (activeButton != null && activeButton != target) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
            if (!activeButton.getStyleClass().contains("sidebar-btn")) {
                activeButton.getStyleClass().add("sidebar-btn");
            }
        }
        if (target != null) {
            target.getStyleClass().remove("sidebar-btn");
            if (!target.getStyleClass().contains("sidebar-btn-active")) {
                target.getStyleClass().add("sidebar-btn-active");
            }
            activeButton = target;
        }
    }

    public void setActiveButton(String viewKey) {
        for (NavEntry entry : navEntries) {
            if (entry.viewKey.equals(viewKey)) {
                activateButton(entry.button);
                return;
            }
        }
    }

    /**
     * Charge un fichier SVG et retourne un Pane de taille fixe {@code targetPx x targetPx}.
     * <p>
     * Stratégie robuste :
     * <ol>
     *   <li>Parse les {@code <path>}, {@code <circle>} et {@code <rect>} du fichier SVG.</li>
     *   <li>Groupe toutes les formes dans un {@link Group}.</li>
     *   <li>Applique un {@link Scale} avec pivot en (0, 0) pour obtenir la taille cible.</li>
     *   <li>Encapsule dans un {@link Pane} à dimensions fixes — le seul nœud "managé"
     *       par le layout parent — ce qui évite tout problème de bounds non calculés.</li>
     * </ol>
     */
    public static Pane chargerSvg(String cheminFichier, String styleCss, double targetPx) {
        Group group = new Group();
        double vbW = 512.0, vbH = 512.0;

        try {
            File xmlFile = new File(cheminFichier);
            if (!xmlFile.exists()) {
                throw new IllegalArgumentException("SVG introuvable : " + xmlFile.getAbsolutePath());
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Désactiver la validation DTD pour éviter les blocages réseau (svg11.dtd)
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();

            // --- Récupérer les dimensions du viewBox ---
            if (root.hasAttribute("viewBox")) {
                String[] parts = root.getAttribute("viewBox").trim().split("[\\s,]+");
                if (parts.length >= 4) {
                    vbW = Double.parseDouble(parts[2]);
                    vbH = Double.parseDouble(parts[3]);
                }
            } else {
                String w = root.getAttribute("width").replace("px", "").trim();
                String h = root.getAttribute("height").replace("px", "").trim();
                if (!w.isEmpty()) vbW = Double.parseDouble(w);
                if (!h.isEmpty()) vbH = Double.parseDouble(h);
            }

            // --- Parser paths ---
            NodeList paths = doc.getElementsByTagName("path");
            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                String d = el.getAttribute("d");
                if (!d.isEmpty()) {
                    SVGPath p = new SVGPath();
                    p.setContent(d);
                    p.getStyleClass().add(styleCss);
                    group.getChildren().add(p);
                }
            }

            // --- Parser circles ---
            NodeList circles = doc.getElementsByTagName("circle");
            for (int i = 0; i < circles.getLength(); i++) {
                Element el = (Element) circles.item(i);
                try {
                    double cx = Double.parseDouble(el.getAttribute("cx"));
                    double cy = Double.parseDouble(el.getAttribute("cy"));
                    double r  = Double.parseDouble(el.getAttribute("r"));
                    Circle c = new Circle(cx, cy, r);
                    c.getStyleClass().add(styleCss);
                    group.getChildren().add(c);
                } catch (NumberFormatException ignored) {}
            }

            // --- Parser rects ---
            NodeList rects = doc.getElementsByTagName("rect");
            for (int i = 0; i < rects.getLength(); i++) {
                Element el = (Element) rects.item(i);
                try {
                    double x = Double.parseDouble(el.getAttribute("x"));
                    double y = Double.parseDouble(el.getAttribute("y"));
                    double w = Double.parseDouble(el.getAttribute("width"));
                    double h = Double.parseDouble(el.getAttribute("height"));
                    Rectangle rect = new Rectangle(x, y, w, h);
                    rect.getStyleClass().add(styleCss);
                    group.getChildren().add(rect);
                } catch (NumberFormatException ignored) {}
            }

            System.out.println("[SVG OK] " + cheminFichier
                + " — shapes: " + group.getChildren().size()
                + " — viewBox: " + vbW + "x" + vbH);

        } catch (Exception ex) {
            System.err.println("[SVG ERR] " + cheminFichier + " : " + ex.getMessage());
            // Fallback visible : croix rouge
            SVGPath cross = new SVGPath();
            cross.setContent("M2,2 L14,14 M14,2 L2,14");
            cross.setStyle("-fx-stroke: #EF4444; -fx-stroke-width: 2;");
            group.getChildren().add(cross);
        }

        // --- Appliquer le Scale avec pivot (0,0) ---
        double scale = targetPx / Math.max(vbW, vbH);
        Scale scaleT = new Scale(scale, scale, 0, 0);
        group.getTransforms().add(scaleT);

        // --- Encapsuler dans un Pane managé de taille fixe ---
        // Pane ne repositionne pas ses enfants → le Group reste à (0,0).
        Pane wrapper = new Pane(group);
        wrapper.setPrefSize(targetPx, targetPx);
        wrapper.setMinSize(targetPx, targetPx);
        wrapper.setMaxSize(targetPx, targetPx);

        return wrapper;
    }
}
