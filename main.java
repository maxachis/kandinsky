import processing.core.*;
import processing.svg.*;

import java.util.Random;

import static java.lang.Math.toDegrees;


public class main extends PApplet{
    public PGraphics g = new PGraphics();
    Random rand = new Random();
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

    public void settings(){
        size(width, height);
        smooth(16);
    }

    public void setup() {
        background(125);

//        fill(0);
        float x, y, radius;
        //Step 1: Set First Circle. Must not overlap with bounds of window.
        int radiusMin = 200;
        int radiusMax = 300;
        radius = random(radiusMin, radiusMax);
        x = random(0 + radius/2, width - radius/2);
        y = random(0 + radius/2, height - radius/2);
        int R = (int) random(0, 255);
        int G = (int) random(0, 255);
        int B = (int) random(0, 255);
        fill(R,G,B, 125f);
//        circle(x,y,radius);
        generateNoiseInCircle( (int) x, (int) y, (int) radius, R, G, B, 10);
        circleInCircle(x,y,radius/2, 0.9f);
        //TODO: Generate intersecting circles, where in the areas where they intersect, the colors either combine or becoming the opposite of the colors combined.
        //TODO: Generate "auras" outside the circle, consisting of a lighter color that gradually fades into the background the further away it gets from the original radius.
        //TODO: Figure out how to make the noise appear less repetitive.
    }

    public void generateNoiseInCircle(int x,int y, int r, int RMain, int GMain, int BMain, int variance) {
        //TODO: Add a circle border around this noise, to make it look neater?
        int R, G, B;
        int rMin = RMain - variance;
        int rMax = RMain + variance;
        int gMin = GMain - variance;
        int gMax = GMain + variance;
        int bMin = BMain - variance;
        int bMax = BMain + variance;

        for (int i = y-r; i < y+r; i++) {
            for (int j = x; Math.pow(j - x, 2) + Math.pow(i - y, 2) <= Math.pow(r, 2); j--) {
                //in the circle
                R = (int) map(noise(i,j), 0, 1, rMin, rMax);
                G = (int) map(noise(i,j), 0, 1, gMin, gMax);
                B = (int) map(noise(i,j), 0, 1, bMin, bMax);
                set(i,j, color(R,G,B));
            }
            for (int j = x + 1; (j - x) * (j - x) + (i - y) * (i - y) <= r * r; j++) {
                //in the circle
                R = (int) map(noise(i,j), 0, 1, rMin, rMax);
                G = (int) map(noise(i,j), 0, 1, gMin, gMax);
                B = (int) map(noise(i,j), 0, 1, bMin, bMax);
                set(i,j, color(R,G,B));
            }
        }
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
            circleInCircle(newX, newY, newRadius, recursionChance);
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
