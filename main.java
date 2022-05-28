import processing.core.*;
import processing.svg.*;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.toDegrees;


public class main extends PApplet{
    public PGraphics g = new PGraphics();
    Random rand = new Random();
    ArrayList<circle> circles = new ArrayList<circle>();
    int[][] red = {
            {94, 25, 20},
            {194, 24, 7},
            {178, 34, 34},
            {139, 0, 0},
            {128, 0, 0},
            {121, 6, 4},
            {0,0,0},
            {101, 0, 11},
            {102, 0, 0}
    };
    int width = 500;
    int height = 500;
    Color backgroundColor = new Color(0,0,0);

    int minCircleSize = 10;
    int maxCircleSize = 200;
    //recursionDecay controls how much the chance of a circle forming another circle declines with each additional circle. 1.0f means no decay.
    float recursionDecay = 0.7f;

    public void settings(){
        size(width, height);
        smooth(16);
    }

    public void setup() {
        background(backgroundColor.getRGB());

//        fill(0);
        float x, y, radius;
        //Step 1: Set First Circle. Must not overlap with bounds of window.
        int radiusMin = 100;
        int radiusMax = 200;
        radius = random(radiusMin, radiusMax);
        x = random(0 + radius/2, width - radius/2);
        y = random(0 + radius/2, height - radius/2);
//        x = width/2;
//        y = height/2;
        int R = (int) random(0, 255);
        int G = (int) random(0, 255);
        int B = (int) random(0, 255);
        fill(R,G,B, 125f);
//        circle(x,y,radius);
        generateNoiseInCircle( (int) x, (int) y, (int) radius, R, G, B, 10);
        circleInCircle(x,y,radius/2, 0.9f);
        R = (int) random(0, 255);
        G = (int) random(0, 255);
        B = (int) random(0, 255);
        //This circle isn't always intersecting. Fix it.
        generateIntersectingCircle((int) x, (int) y, (int) radius, R, G, B, 10, 0.5f);

        for (circle c: circles) {
            generateAuraAroundCircle(c.x, c.y, c.r);
        }
        //TODO: Figure out how to make the noise appear less repetitive.
        //TODO: Replace three-line R,G,B generation with a simple random color generation function.
    }


    /**
     * Generate intersecting circles, where in the areas where they intersect, the colors either combine or become the opposite of the colors combined.
     *
     * @param xc : X-value of original circle
     * @param yc : Y-value of original circle
     * @param rc : Radius of original circle
     */
    public void generateIntersectingCircle(int xc, int yc, int rc, int RMain, int GMain, int BMain, int variance, float recursionChance) {
        //For now, new circle is either equal in size to the first, or smaller.
        int newRadius = (int) random(minCircleSize, rc);
        //Generate angle at which the new circle is oriented relative to the original circle
        float theta = random(0, TWO_PI);
        // Identify distance of new circle's center from old. The two radii must overlap.
        float dist = random(rc - newRadius, rc + newRadius);
        //Generate newX and new Y using dist and theta
        float newX = xc + dist * cos(theta);
        float newY = yc + dist * sin(theta);
        int rMin = RMain - variance;
        int rMax = RMain + variance;
        int gMin = GMain - variance;
        int gMax = GMain + variance;
        int bMin = BMain - variance;
        int bMax = BMain + variance;
        int R,G,B;

        int r = newRadius;
        int ox = (int) newX;
        int oy = (int) newY;
        circles.add(new circle(ox, oy, r));

        for (int x = -r; x < r ; x++)
        {
            int height = (int)Math.sqrt(r * r - x * x);
            for (int y = -height; y < height; y++) {
                R = (int) map(noise(x,y), 0, 1, rMin, rMax);
                G = (int) map(noise(x,y), 0, 1, gMin, gMax);
                B = (int) map(noise(x,y), 0, 1, bMin, bMax);
                mixNonBackgroundColors(ox + x, oy + y, color(R,G,B));
            }
        }
        if (random(1) < recursionChance) {
            Color c = generateRandomColor();
            generateIntersectingCircle(ox, oy, r, c.getRed(), c.getGreen(), c.getBlue(), variance, recursionChance * recursionDecay);
        }
        if (random(0, 1) < recursionChance) {
            circleInCircle(newX, newY, newRadius, recursionChance * recursionDecay);
        }

    }

    public Color generateRandomColor() {
        return new Color(
                (int) random(0, 255),
                (int) random(0, 255),
                (int) random(0, 255)
        );
    }

    public void generateAuraAroundCircle(int x, int y, int r) {
        //Iterate around the radius of the circle, with the chance of a pixel decreasing with every further radius away
        float thetaInc = 0.006f;
        float chance = (float) r/ (float) maxCircleSize;
        int ax, ay;
        r+=1;
        int noiseVar = 100;
        while (chance > 0.001f) {
            for (float theta = 0; theta < TWO_PI; theta += thetaInc) {
                ax = (int) x + (int) (r *  cos(theta));
                ay = (int) y + (int) (r * sin(theta));
                if (get(ax,ay) == backgroundColor.getRGB()) {
                    if (random(0, 1.0f) < chance) {
                        set(ax,ay, color(
                                (int) map(noise(ax,ay), 0, 1, 125 - noiseVar, 125 + noiseVar),
                                (int) map(noise(ax,ay), 0, 1, 125 - noiseVar, 125 + noiseVar),
                                (int) map(noise(ax,ay), 0, 1, 125 - noiseVar, 125 + noiseVar),
                                (int) random(255))
                        );
                    }
                }
            }
            chance/= 1.1f;
            r+=1;
        }


    }

    //Combine two colors if they are different from the background
    public void mixNonBackgroundColors(int x, int y, int color) {
        //Get color of pixel
        int curColor = get(x,y);
        //If curColor not background, mix colors
        if (curColor != backgroundColor.getRGB()) {
            set(x,y, lerpColor(curColor, color, 0.5f));
        } else {
            set(x,y, color);
        }

    }

    public void generateNoiseInCircle(int ox,int oy, int r, int RMain, int GMain, int BMain, int variance) {
        //TODO: Add a circle border around this noise, to make it look neater?
        circles.add(new circle(ox, oy, r));
        int R, G, B;
        int rMin = RMain - variance;
        int rMax = RMain + variance;
        int gMin = GMain - variance;
        int gMax = GMain + variance;
        int bMin = BMain - variance;
        int bMax = BMain + variance;

        for (int x = -r; x < r ; x++)
        {
            int height = (int)Math.sqrt(r * r - x * x);
            for (int y = -height; y < height; y++) {
                R = (int) map(noise(x,y), 0, 1, rMin, rMax);
                G = (int) map(noise(x,y), 0, 1, gMin, gMax);
                B = (int) map(noise(x,y), 0, 1, bMin, bMax);
                mixNonBackgroundColors(ox + x, oy + y, color(R,G,B));
            }
        }
//        generateAuraAroundCircle(ox, oy, r);
    }

    /**
     * Place a circle within a circle defined by these coordinates. Then a chance of having another circle inside it as well
     * @param x
     * @param y
     * @param radius
     * @param recursionChance
     */
    public void circleInCircle(float x, float y, float radius, float recursionChance) {
        // Set range of new circle
        int R = (int) random(0, 255);
        int G = (int) random(0, 255);
        int B = (int) random(0, 255);
        fill(R,G,B, 125f);
        float newRadius = random(0, radius * 0.90f);
        //New x and y values must not yield a circle whose radius is outside the outer radius.
        //Radius diff exists as a subradius within which the new center can exist without newRadius stretching outside of the original radius
        float radiusDiff = radius - newRadius;
        float distFromOldCenter = radiusDiff * random(0,1);
        float theta = random(0,1) * 2 * PI;
        //If you want to have the new circle guaranteed to touch the line of the inner circle, replace distFromOldCenter with radiusDiff
        float newX = x  + distFromOldCenter * cos(theta);
        float newY = y  + distFromOldCenter * sin(theta);
//        circle(newX, newY, newRadius * 2);
        generateNoiseInCircle( (int) newX, (int) newY, (int) newRadius, R, G, B, 10);
        if (random(0, 1) < recursionChance) {
            circleInCircle(newX, newY, newRadius, recursionChance * recursionDecay);
        }
        if (random(1) < recursionChance) {
            R = (int) random(0, 255);
            G = (int) random(0, 255);
            B = (int) random(0, 255);
            generateIntersectingCircle((int) newX, (int) newY, (int) newRadius, R, G, B, 10, recursionChance * recursionDecay);
        }
    }

    public void applyTexture() {
        PGraphics g = new PGraphics();
        PImage img = loadImage("corgi.jpg");
        int x,y, radius;
        PShape customShape = createShape();
        customShape.beginShape(TRIANGLE_FAN);
        int numSides = 100;
        float theta = TWO_PI / numSides;
        customShape.vertex(width/2, height/2);
        for (int i=0; i<numSides+1; i++) {
            float fx = sin(i * theta) * width/2 + width/2;
            float fy = cos(i * theta) * width/2 + height/2;
            customShape.vertex(fx, fy);
        }
        customShape.endShape();
        PShape ellipseShape = createShape(ELLIPSE, 0, 0, width, height);
//        addTextureUV(customShape, img);
        addTextureUV(ellipseShape, img);

        this.g.setSize(500,500);
        this.g.fill(0);
        this.g.beginShape();
        this.g.texture(img);
        this.g.vertex(40, 80);
        this.g.vertex(320, 20);
        this.g.vertex(380, 360);
        this.g.vertex(160, 380);
//        this.g.circle(width/2, height/2, 200);
        this.g.endShape();
//        image(pg, 120, 60);
//        this.g.circle(x, y, radius);
        image(g, 0, 0);
//
    }
//    Create a texture that randomly varies within a certain degree of space.
    public PGraphics createTexture(int RMain, int GMain, int BMain, int variance) {
//        PGraphics g = createGraphics(200, 200);
        PGraphics g = new PGraphics();
        g.setSize(200, 200);
        int R, G, B;
        int rMin = RMain - variance;
        int rMax = RMain + variance;
        int gMin = GMain - variance;
        int gMax = GMain + variance;
        int bMin = BMain - variance;
        int bMax = BMain + variance;
        noiseDetail(4,(float) 0.1);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                R = (int) map(noise(x,y), 0, 1, rMin, rMax);
//                G = (int) map(noise(x,y), 0, 1, gMin, gMax);
//                B = (int) map(noise(x,y), 0, 1, bMin, bMax);
//                set(x,y, color(R,G,B));
//            }
//        }
        g.loadPixels();
        for (int i = 0; i < g.pixels.length; i++) {
            //TODO: Use a seed to compare different values of noise detail
//            R = (int) random(RMain - variance, RMain + variance);
//            G = (int) random(GMain - variance, GMain + variance);
//            B = (int) random(BMain - variance, BMain + variance);
            R = (int) map(noise(i), 0, 1, rMin, rMax);
            G = (int) map(noise(i), 0, 1, gMin, gMax);
            B = (int) map(noise(i), 0, 1, bMin, bMax);
            g.pixels[i] = color(R, G, B);

        }
        g.updatePixels();
        return(g);
    }

    public void mouseClicked() {
        save("save.jpg");
    }


    public void draw(){
    }

    void addTextureUV(PShape s, PImage img) {
        s.setStroke(false);
        s.setTexture(img);
        s.setTextureMode(NORMAL);
        for (int i = 0; i < s.getVertexCount(); i++) {
            PVector v = s.getVertex(i);
            s.setTextureUV(i, map(v.x, 0, width, 0, 1), map(v.y, 0, height, 0, 1));
        }
    }

    public static void main(String... args){
        PApplet.main("main");
    }
}
