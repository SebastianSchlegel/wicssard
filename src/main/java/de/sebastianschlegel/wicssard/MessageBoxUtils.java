package de.sebastianschlegel.wicssard;

import javafx.scene.control.Alert;

public final class MessageBoxUtils {

    private MessageBoxUtils () {
    }

    public static void showInfo () {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("wiCSSard - a simple CSS color style migration tool");
        alert.setContentText("(C) 2018 Sebastian Schlegel\nApache License Version 2.0");
        alert.showAndWait();
    }

    public static void showError (String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText("An exception occurred:\n" + e.getMessage());
        alert.showAndWait();
    }
}
