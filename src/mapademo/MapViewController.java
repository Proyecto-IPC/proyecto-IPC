/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds; // Para zoom centrado
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;

/**
 * FXML Controller class
 *
 * @author david
 */
public class MapViewController implements Initializable {

    @FXML
    private StackPane mapArea;
    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private Group contentGroup;
    @FXML
    private Group zoomGroup;
    @FXML
    private Pane mapPane;
    @FXML
    private ImageView mapImageView;
    @FXML
    private VBox zoomControls;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button centerRouteButton;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox zoomButtonGroup;
    
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        centerRouteButton.setDisable(true);
        
        zoomInButton.setOnAction(event -> zoomIn());
        zoomOutButton.setOnAction(event -> zoomOut());
        
        setZoom(1.0);
    }
    
    private void zoomIn() {
        setZoom(zoomLevel + ZOOM_STEP);
    }
    
    private void zoomOut() {
        setZoom(zoomLevel - ZOOM_STEP);
    }
    
    private void setZoom(double zoom) {
        // El zoom se mantiene entre sus limites
        double newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        
        // Rectangulo visible del ScrollPane
        Bounds viewportBounds = mapScrollPane.getViewportBounds();
        // Tamanyo total del contendido dentro del ScrollPane (mapa escalado)
        Bounds contentBounds = contentGroup.getBoundsInLocal();
        
        // Obtiene las dimensiones visibles y las del contenido
        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        double contentWidth = contentBounds.getWidth();
        double contentHeight = contentBounds.getHeight();
        
        // Calcular el centro visible del ScrollPane
        double centerX = mapScrollPane.getHvalue() * Math.max(contentWidth - viewportWidth, 0) + viewportWidth / 2;
        double centerY = mapScrollPane.getVvalue() * Math.max(contentHeight - viewportHeight, 0) + viewportHeight / 2;
        
        // Ajusta el zoom solicitado a los limites permitidos
        zoomLevel = newZoom;
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);
        
        // Mide el nuevo tamanyo del contenido tras el zoom
        Bounds newContentBounds = contentGroup.getBoundsInLocal();
        double newContentWidth = newContentBounds.getWidth();
        double newContentHeight = newContentBounds.getHeight();
        
        // Calcula la nueva posicion del scroll (H y V)
        double newH = (centerX * (newContentWidth / contentWidth) - viewportWidth / 2) / Math.max(newContentWidth - viewportWidth, 1);
        double newV = (centerY * (newContentHeight / contentHeight) - viewportHeight / 2) / Math.max(newContentHeight - viewportHeight, 1);
        
        // Aplica el nuevo scroll
        mapScrollPane.setHvalue(clamp(newH, 0, 1));
        mapScrollPane.setVvalue(clamp(newV, 0, 1));
    }
    
    // Asegura que nunca se salga de los limites de ScrollPane
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public void setActivity(Activity activity) {
        
    }
    
    private void clearMap() {
        
    }
    
    private void drawActivity(Activity activity) {
        
    }
    
    private void centerRoute() {
        
    }
    
}
