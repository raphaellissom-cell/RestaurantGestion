package org.example.restaurantgestion;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.restaurantgestion.views.MainView;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        MainView mainView = new MainView();
        mainView.setSnapToPixel(true);

        Scene scene = new Scene(mainView, bounds.getWidth(), bounds.getHeight());
        scene.setFill(Color.web("#F9FAFB"));

        stage.setTitle("monRestau");
        stage.setMinWidth(1180);
        stage.setMinHeight(720);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
