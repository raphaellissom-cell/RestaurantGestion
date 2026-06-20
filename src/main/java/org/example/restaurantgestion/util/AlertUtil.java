package org.example.restaurantgestion.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

public class AlertUtil {

    private static Window findFocusedWindow() {
        return Stage.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }

    public static void show(Alert alert) {
        Window owner = findFocusedWindow();
        if (owner instanceof Stage) {
            alert.initOwner((Stage) owner);
        }
        alert.showAndWait();
    }

    public static Optional<ButtonType> showAndWait(Alert alert) {
        Window owner = findFocusedWindow();
        if (owner instanceof Stage) {
            alert.initOwner((Stage) owner);
        }
        return alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        show(alert);
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        show(alert);
    }

    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        show(alert);
    }

    public static Optional<ButtonType> showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        return showAndWait(alert);
    }
}
