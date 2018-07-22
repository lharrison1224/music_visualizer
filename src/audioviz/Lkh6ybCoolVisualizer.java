/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioviz;

import static java.lang.Integer.min;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author lkh6y
 */
public class Lkh6ybCoolVisualizer implements Visualizer{

    private final String name = "Lkh6ybCoolVisualizer";
    
    private String vizPaneInitialStyle = "";
    private Integer numBands;
    private AnchorPane vizPane;
    private Double width = 0.0;
    private Double height = 0.0;
    private Double bandWidth = 0.0;
    private final Double bandHeightPercentage = 1.3;
    private Double bandHeight = 0.0;
    private Double halfBandHeight = 0.0;
    private Ellipse[] ellipses;
    private final Double minEllipseRadius = 10.0;
    private final Double startHue = 260.0;
    private final Double magnitudeOffset = 70.0;
    private double colorChanging = 0.0;
    private double[] prevBrightness = new double[140];

    @Override
    public void start(Integer numBands, AnchorPane vizPane) {
        end();
        
        vizPaneInitialStyle = vizPane.getStyle();
        this.numBands = numBands;
        this.vizPane = vizPane;
        
        height = vizPane.getHeight();
        width = vizPane.getWidth();
        
        Rectangle clip = new Rectangle(width, height);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        vizPane.setClip(clip);
        
        bandWidth = width / numBands;
        bandHeight = height * bandHeightPercentage;
        halfBandHeight = bandHeight/2;
        ellipses = new Ellipse[numBands];
        double currentRadius = 500.0;
        for(int i = 0; i < numBands; i++){
            Ellipse ellipse = new Ellipse();
//            ellipse.setCenterX(bandWidth / 2 + bandWidth * i);
//            ellipse.setCenterY(height/2);
//            ellipse.setRadiusX(bandWidth/2);
//            ellipse.setRadiusY(minEllipseRadius);

            prevBrightness[i] = 0.0;
            ellipse.setCenterX(500);
            ellipse.setCenterY(375);
            ellipse.setRadiusX(currentRadius);
            ellipse.setRadiusY(currentRadius);
            
            currentRadius -= (double)490/numBands;
            ellipse.setFill(Color.hsb(startHue, 1.0, 0.0, 1.0));
            vizPane.getChildren().add(ellipse);
            ellipses[i] = ellipse;
        }
        
    }

    @Override
    public void end() {
        if (ellipses != null) {
            for (Ellipse ellipse : ellipses) {
                vizPane.getChildren().remove(ellipse);
            }
            ellipses = null;
            vizPane.setClip(null);
            vizPane.setStyle(vizPaneInitialStyle);
        }    
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update(double timestamp, double duration, float[] magnitudes, float[] phases) {
        if(ellipses == null){
            return;
        }
        
        Integer num = min(ellipses.length, magnitudes.length);
        double brightnessValue;
        for(int i = 0; i < num; i++){
            brightnessValue = ((magnitudes[i]+60)/30);
            if(brightnessValue > 1)
                brightnessValue = 1;
            else if(brightnessValue < 0)
                brightnessValue = 0;
               
            if(brightnessValue < prevBrightness[i]){
                brightnessValue = prevBrightness[i];
                if(brightnessValue > 0.5)
                    brightnessValue -= 0.1;
                else if(brightnessValue > 0.15)
                    brightnessValue -= 0.03;
                else
                    brightnessValue -=0.01;
                
                
                
                if(brightnessValue < 0)
                    brightnessValue = 0;
            }
            
            ellipses[num-i-1].setFill(Color.hsb(((colorChanging+i)%360), 1.0, brightnessValue, 1.0));
            prevBrightness[i] = brightnessValue;
        }
        colorChanging++;
        
        
    }
    
}
