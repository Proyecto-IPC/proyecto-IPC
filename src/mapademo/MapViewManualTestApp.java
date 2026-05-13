/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapademo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 *
 * @author david
 */
public class MapViewManualTestApp extends Application {

    private AnotacionesManager anotacionesManager;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MapView.fxml"));
        Parent root = loader.load();
        MapViewController controller = loader.getController();

        anotacionesManager = new AnotacionesManager();
        anotacionesManager.setMapController(controller);

        SportActivityApp app = SportActivityApp.getInstance();
        app.registerUser(
            "testmap",
            "testmap@example.com",
            "Pass123!",
            LocalDate.of(2000, 1, 1),
            (String) null
        );

        app.login("testmap", "Pass123!");

        Activity activity = app.importActivity(new File("gpx/valencia_run.gpx"));
        controller.setActivity(activity);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        Scene scene = new Scene(root);
        stage.setTitle("Prueba manual - vista de mapa");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
