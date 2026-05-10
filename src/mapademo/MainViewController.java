package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainViewController implements Initializable {

    @FXML
    private BorderPane rootPane;  // El BorderPane principal del FXML

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Se ejecuta automáticamente al cargar el FXML
        // De momento vacío, aquí luego cargaremos la vista de Login
    }

    // Carga cualquier vista FXML en el centro del BorderPane
    public void cargarVista(String fxmlPath) {
        try {
            Pane vista = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}