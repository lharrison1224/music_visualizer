/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioviz;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author dale
 * Music: http://www.bensound.com/royalty-free-music
 * http://www.audiocheck.net/testtones_sinesweep20-20k.php
 * http://stackoverflow.com/questions/11994366/how-to-reference-primarystage
 */
public class PlayerController implements Initializable {
    
    @FXML
    private AnchorPane vizPane;
    
    @FXML
    private MediaView mediaView;
    
    @FXML
    private Text filePathText;
    
    @FXML
    private Text lengthText;
    
    @FXML
    private Text currentText;
    
    @FXML
    private Text bandsText;
    
    @FXML
    private Text visualizerNameText;
    
    @FXML
    private Text errorText;
    
    @FXML
    private Menu visualizersMenu;
    
    @FXML
    private Menu bandsMenu;
    
    @FXML
    private Slider timeSlider;
    
    private Media media;
    private MediaPlayer mediaPlayer;
    
    private Integer numBands = 40;
    private final Double updateInterval = 0.05;
    
    private ArrayList<Visualizer> visualizers;
    private Visualizer currentVisualizer;
    private final Integer[] bandsList = {1, 2, 4, 8, 16, 20, 40, 60, 100, 120, 140};
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        bandsText.setText(Integer.toString(numBands));
        
        visualizers = new ArrayList<>();
        
        visualizers.add(new EllipseVisualizer1());
        visualizers.add(new EllipseVisualizer2());
        visualizers.add(new EllipseVisualizer3());
        visualizers.add(new Lkh6ybCoolVisualizer());
        
        for(Visualizer visualizer : visualizers){
            MenuItem menuItem = new MenuItem(visualizer.getName());
            menuItem.setUserData(visualizer);
            menuItem.setOnAction((ActionEvent event) -> {
                selectVisualizer(event);
            });
            
            visualizersMenu.getItems().add(menuItem);
        }
        
        currentVisualizer = visualizers.get(0);
        visualizerNameText.setText(currentVisualizer.getName());
        
        for(Integer bands : bandsList){
            MenuItem menuItem = new MenuItem(Integer.toString(bands));
            menuItem.setUserData(bands);
            menuItem.setOnAction((ActionEvent event) -> {
                selectBands(event);
            });
            
            bandsMenu.getItems().add(menuItem);
        }
        
    }
    
    private void selectVisualizer(ActionEvent event) {
        MenuItem menuItem = (MenuItem)event.getSource();
        Visualizer visualizer = (Visualizer)menuItem.getUserData();
        changeVisualizer(visualizer);
    }
    
    private void selectBands(ActionEvent event) {
        MenuItem menuItem = (MenuItem)event.getSource();
        numBands = (Integer)menuItem.getUserData();
        if (currentVisualizer != null) {
            currentVisualizer.start(numBands, vizPane);
        }
        if (mediaPlayer != null) {
            mediaPlayer.setAudioSpectrumNumBands(numBands);
        }
        bandsText.setText(Integer.toString(numBands));
    }
    
    private void changeVisualizer(Visualizer visualizer) {
        if (currentVisualizer != null) {
            currentVisualizer.end();
        }
        currentVisualizer = visualizer;
        currentVisualizer.start(numBands, vizPane);
        visualizerNameText.setText(currentVisualizer.getName());
    }
    
    private void openMedia(File file) {
        filePathText.setText("");
        errorText.setText("");
        
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        
        try {
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnReady(() -> {
                handleReady();
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                handleEndOfMedia();
            });
            mediaPlayer.setAudioSpectrumNumBands(numBands);
            mediaPlayer.setAudioSpectrumInterval(updateInterval);
            mediaPlayer.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
                handleUpdate(timestamp, duration, magnitudes, phases);
            });
            mediaPlayer.setAutoPlay(true);
            filePathText.setText(file.getPath());
        } catch (Exception ex) {
            errorText.setText(ex.toString());
        }
    }
    
    private void handleReady() {
        Duration duration = mediaPlayer.getTotalDuration();
        lengthText.setText(duration.toString());
        Duration ct = mediaPlayer.getCurrentTime();
        currentText.setText(ct.toString());
        currentVisualizer.start(numBands, vizPane);
        timeSlider.setMin(0);
        timeSlider.setMax(duration.toMillis());
    }
    
    private void handleEndOfMedia() {
        mediaPlayer.stop();
        mediaPlayer.seek(Duration.ZERO);
        timeSlider.setValue(0);
    }
    
    private void handleUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        Duration ct = mediaPlayer.getCurrentTime();
        double ms = ct.toMillis();
        currentText.setText(String.format("%.1f ms", ms));
        timeSlider.setValue(ms);
        
        currentVisualizer.update(timestamp, duration, magnitudes, phases);
    }
    
    @FXML
    private void handleOpen(Event event) {
        Stage primaryStage = (Stage)vizPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            openMedia(file);
        }
    }
    
    @FXML
    private void handlePlay(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        
    }
    
    @FXML
    private void handlePause(ActionEvent event) {
        if (mediaPlayer != null) {
           mediaPlayer.pause(); 
        }
    }
    
    @FXML
    private void handleStop(ActionEvent event) {
        if (mediaPlayer != null) {
           mediaPlayer.stop(); 
        }
        
    }
    
    @FXML
    private void handleSliderMousePressed(Event event) {
        if (mediaPlayer != null) {
           mediaPlayer.pause(); 
        }  
    }
    
    @FXML
    private void handleSliderMouseReleased(Event event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(timeSlider.getValue()));
            System.out.println(timeSlider.getValue());
            currentVisualizer.start(numBands, vizPane);
            mediaPlayer.play();
        }  
    }
}
