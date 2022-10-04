package layout.utils;



import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.scene.paint.Paint;


import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Makes the node's background "glow" alternating between two colors.
 * If the node has a {@link Background}, then the {@link CornerRadii} and {@link Insets}
 * of the last of its {@link BackgroundFill}s are used for the glowing background
 *
 * @author negste
 */
public class GlowBackground2 {

    private Timeline timeline;

    /**
     * Constructs the animation
     *
     * @param node       the node to animate
     * @param colorA     the color to start with
     * @param colorB     the other color
     * @param colorSteps how many interpolations between the two colors
     */
    public GlowBackground2(Shape node, Color colorA, Color colorB, int colorSteps) {

        int totalFrames = colorSteps * 2;
        double millisPerFrame = 1000 / totalFrames;
        
        timeline= new Timeline(); 

        for (int i = 0; i < totalFrames; i++) {
        	
            Color color;
            double frac = i * 2.0 / totalFrames;
            Duration dur = Duration.millis(i * millisPerFrame);
            
            
            if (i <= colorSteps) {
                color = colorA.interpolate(colorB, frac);
            } else {
                color = colorB.interpolate(colorA, frac - 1);
            }
            
            getTimeline().getKeyFrames().add(
                    new KeyFrame(dur,
                            new KeyValue(node.fillProperty(), color)));
            
           // System.out.println("Color: " + color.getRed() + " " + color.getGreen() + " " + color.getBlue()); 
        }

    }

	public Timeline getTimeline() {
		return timeline;
	}




//    @Override
//    AnimationFX resetNode() {
//        getNode().setBackground(originalBackground);
//        return this;
//    }
//
//    @Override
//    void initTimeline() {
//        setTimeline(new Timeline()); //will be populated at the end of constructor
//    }

}