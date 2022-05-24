import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.Random;

import static java.lang.Math.toDegrees;


public class main2 extends PApplet{
    public PGraphics g = new PGraphics();
    int width = 500;
    int height = 500;

    public void settings(){
        size(width, height /*, processing.javafx.PGraphicsFX2D*/);
    }

    /**
     * Parametric value for a circle.
     *
     * @param  cx   x-value of center
     * @param  cy   y-value of center
     * @param  r    radius
     * @param  a    angle
     * @return      the xy values of the circle point
     */
    float[] parametricEquation(double cx, double cy, double r, double a){
        float[] result = new float[2];
        result[0] = (float) (cx + r * cos((float) toDegrees(a)));
        result[1] = (float) (cy + r * sin((float) toDegrees(a)));
        return result;
    }

    /**
     * Get opposite angle of given angle, in degrees
     * @param   a   given angle
     * @return      opposite angle
     */
    double oppositeAngle(double a) {
        return (toDegrees(a) + 180 % 360);
    }

    public void setup() {
        PImage img = loadImage("save.jpg");
        beginShape();
        texture(img);
        endShape();
//        image(pg, 120, 60);
    }


    public static void main(String... args){
        PApplet.main("main");
    }
}
