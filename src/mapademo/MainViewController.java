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
    private BorderPane rootPane;

    // Referencia estática para que otras vistas puedan navegar
    private static MainViewController instancia;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;          // Guardamos la referencia a esta instancia
        cargarVista("LoginView.fxml");
    }

    // Permite a cualquier controlador acceder al MainShell
    public static MainViewController getInstancia() {
        return instancia;
    }

    // Carga una vista FXML en el centro del BorderPane
    public void cargarVista(String fxmlPath) {
        try {
            Pane vista = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}