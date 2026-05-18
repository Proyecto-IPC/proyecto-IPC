package mapademo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class MapaDemoApp extends Application {

    private static Parent rootNode;

    @Override
    public void start(Stage stage) throws Exception {
        rootNode = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/clubRunningLaSafor/icon.png")));
        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add(getClass().getResource("/resources/style.css").toExternalForm());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.TAB ||
                code == KeyCode.UP || code == KeyCode.DOWN ||
                code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                setModoTeclado(true);
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            setModoTeclado(false);
        });

        stage.setTitle("Running La Safor");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void setModoTeclado(boolean activo) {
        if (rootNode != null) {
            if (activo) {
                if (!rootNode.getStyleClass().contains("keyboard-mode")) {
                    rootNode.getStyleClass().add("keyboard-mode");
                }
            } else {
                rootNode.getStyleClass().remove("keyboard-mode");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
