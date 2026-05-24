package mapademo;

import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

final class ActivityActions {

    private static final String EDIT_ICON = "/resources/icons/EditIcon.fxml";
    private static final String TRASH_ICON = "/resources/icons/TrashIcon.fxml";

    private ActivityActions() {
    }

    static HBox create(Activity activity, Runnable onChanged) {
        return create(activity, onChanged, onChanged);
    }

    static HBox create(Activity activity, Runnable onRenamed, Runnable onDeleted) {
        HBox actions = new HBox(6);
        actions.getStyleClass().add("activity-row-actions");
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.getChildren().addAll(
                createButton(EDIT_ICON, "Renombrar actividad", false, () -> renameActivity(activity, onRenamed)),
                createButton(TRASH_ICON, "Eliminar actividad", true, () -> deleteActivity(activity, onDeleted))
        );
        return actions;
    }

    private static Button createButton(String iconPath, String tooltip, boolean danger, Runnable action) {
        Button button = new Button();
        button.getStyleClass().add("activity-action-button");
        if (danger) {
            button.getStyleClass().add("activity-action-danger");
        }
        button.setTooltip(new Tooltip(tooltip));
        button.setGraphic(loadIcon(iconPath));
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> event.consume());
        button.setOnAction(event -> {
            action.run();
            event.consume();
        });
        return button;
    }

    private static Node loadIcon(String iconPath) {
        try {
            return FXMLLoader.load(ActivityActions.class.getResource(iconPath));
        } catch (Exception e) {
            return new Region();
        }
    }

    private static void renameActivity(Activity activity, Runnable onChanged) {
        TextInputDialog dialog = new TextInputDialog(activityName(activity));
        dialog.setTitle("Renombrar actividad");
        dialog.setHeaderText(null);
        dialog.setContentText("Nuevo nombre:");
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType rename = new ButtonType("Renombrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(cancel, rename);
        dialog.setResultConverter(button -> button == rename ? dialog.getEditor().getText() : null);
        configureDialog(dialog);
        dialog.getDialogPane().getStyleClass().add("activity-dialog");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancel);
        cancelButton.getStyleClass().add("activity-dialog-cancel-button");
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().lookupButton(rename).getStyleClass().add("activity-dialog-primary-button");
        dialog.showAndWait().ifPresent(name -> {
            String newName = name.trim();
            if (newName.isEmpty()) {
                showWarning("El nombre no puede estar vacío.");
                return;
            }
            if (SportActivityApp.getInstance().renameActivity(activity, newName)) {
                runRefresh(onChanged);
            } else {
                showWarning("No se ha podido renombrar la actividad.");
            }
        });
    }

    private static void deleteActivity(Activity activity, Runnable onChanged) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar actividad");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Eliminar \"" + activityName(activity) + "\" y todos sus datos?");
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.LEFT);
        ButtonType delete = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(cancel, delete);
        configureDialog(confirm);
        confirm.getDialogPane().getStyleClass().add("logout-confirm-dialog");
        confirm.getDialogPane().lookupButton(cancel).getStyleClass().add("logout-cancel-button");
        confirm.getDialogPane().lookupButton(delete).getStyleClass().add("logout-danger-button");
        confirm.showAndWait().ifPresent(result -> {
            if (result == delete) {
                if (SportActivityApp.getInstance().removeActivity(activity)) {
                    runRefresh(onChanged);
                } else {
                    showWarning("No se ha podido eliminar la actividad.");
                }
            }
        });
    }

    private static void runRefresh(Runnable onChanged) {
        if (onChanged != null) {
            onChanged.run();
        }
        MainViewController main = MainViewController.getInstancia();
        if (main != null) {
            main.refrescarDashboardSiExiste();
        }
    }

    private static void configureDialog(Dialog<?> dialog) {
        Window owner = findOwner();
        if (owner != null) {
            dialog.initOwner(owner);
            dialog.setOnShown(event -> centerOnOwner(dialog, owner));
        }
        URL cssUrl = ActivityActions.class.getResource("/resources/style.css");
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    private static Window findOwner() {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.isFocused()) {
                return window;
            }
        }
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                return window;
            }
        }
        return null;
    }

    private static void centerOnOwner(Dialog<?> dialog, Window owner) {
        Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
        dialogWindow.setX(owner.getX() + (owner.getWidth() - dialogWindow.getWidth()) / 2);
        dialogWindow.setY(owner.getY() + (owner.getHeight() - dialogWindow.getHeight()) / 2);
    }

    private static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Actividad");
        alert.setHeaderText(null);
        alert.setContentText(message);
        configureDialog(alert);
        alert.showAndWait();
    }

    private static String activityName(Activity activity) {
        if (activity == null || activity.getName() == null || activity.getName().trim().isEmpty()) {
            return "Actividad sin nombre";
        }
        return activity.getName();
    }
}
