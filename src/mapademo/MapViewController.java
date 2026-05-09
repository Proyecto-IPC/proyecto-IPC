/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);
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
