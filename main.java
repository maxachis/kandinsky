import processing.core.*;
import java.awt.*;
import java.util.ArrayList;

public class main extends PApplet{
    public PGraphics g = new PGraphics();
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
    int width = 1000;
    int height = 1000;
    Color backgroundColor = new Color(0,0,0);

    int minCircleSize = 10;
    int maxCircleSize = 200;
    //recursionDecay controls how much the chance of a circle forming another circle declines with each additional circle. 1.0f means no decay.
    float recursionDecay = 0.7f;

    //Indicates how many primary circles (circles which are randomly placed and spawn the recursive circles) there will be.
    int numPrimaryCircles = 3;
    int rMax = 125;
    int gMax = 125;
    int bMax = 125;

    //Convolution Matrix
    //Guassian Blur
//    float v = 1.0f / 16.0f;
//    float[][] matrix = {
//            { v*1, v*2, v*1 },
//            { v*2, v*4, v*2 },
//            { v*2, v*1, v*1 } };
    //Hi Pass Filter
    float[][] matrix = {
            { -1, -1, -1 },
            { -1,  9, -1 },
            { -1, -1, -1 } };


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
        for (int i = 0; i < numPrimaryCircles; i++) {
            radius = random(radiusMin, radiusMax);
            x = random(0 + radius/2, width - radius/2);
            y = random(0 + radius/2, height - radius/2);
            Color c = generateRandomColor();
            generateNoiseInCircle( (int) x, (int) y, (int) radius, c.getRed(), c.getGreen(), c.getBlue(), 10);
            circleInCircle(x,y,radius/2, 0.9f);
            c = generateRandomColor();
            generateIntersectingCircle((int) x, (int) y, (int) radius, c.getRed(), c.getGreen(), c.getBlue(), 10, 0.5f);
        }

        noFill();
        for (circle cir: circles) {
            generateAuraAroundCircle(cir.x, cir.y, cir.r);
            //TODO: Look into possibly changing the color and size of the stroke for circle.
            circle(cir.x, cir.y, cir.r*2);
        }
        //TODO: Figure out how to make the noise appear less repetitive.
//        convoluteImage();
        stroke(255);
        crossScreenTrapezium();
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
                (int) random(0, rMax),
                (int) random(0, gMax),
                (int) random(0, bMax)
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
                ax = x + (int) (r *  cos(theta));
                ay =  y + (int) (r * sin(theta));
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
        float newRadius = random(minCircleSize, radius * 0.90f);
        //New x and y values must not yield a circle whose radius is outside the outer radius.
        //Radius diff exists as a subradius within which the new center can exist without newRadius stretching outside of the original radius
        float radiusDiff = radius - newRadius;
        float distFromOldCenter = radiusDiff * random(0,1);
        float theta = random(0,1) * 2 * PI;
        //If you want to have the new circle guaranteed to touch the line of the inner circle, replace distFromOldCenter with radiusDiff
        float newX = x  + distFromOldCenter * cos(theta);
        float newY = y  + distFromOldCenter * sin(theta);
        generateNoiseInCircle( (int) newX, (int) newY, (int) newRadius, R, G, B, 10);
        if (random(0, 1) < recursionChance) {
            circleInCircle(newX, newY, newRadius, recursionChance * recursionDecay);
        }
        if (random(1) < recursionChance) {
            Color c = generateRandomColor();
            generateIntersectingCircle((int) newX, (int) newY, (int) newRadius, c.getRed(), c.getGreen(), c.getBlue(), 10, recursionChance * recursionDecay);
        }
    }

    public void crossScreenTrapezium() {
        //Demarcate two points at random points OUTSIDE of screen
        int x1, x2, y1, y2;
        //Set first point
        //Set random x
        x1 = (int) random(0-width, width);
        //Depending on where x1 is, y1 can be in several locations, but must be outside of the square
        if (x1 < 0) {
            y1 = (int) random(0-height, height * 2);
        } else {
            y1 = (int) random(0-height, 0);
        }
        //If x1 within window width, x2 can be anywhere.
        x2 = (int) random(0, width*2);
        if (x2 > width) {
            y2 = (int) random(0-height, height*2);
        } else{
            y2 = (int) random(height, height*2);
        }
        line(x1, y1, x2, y2);


        //Generate two pairs of points, each at right angles to the line, intersecting the line at each of the two original points
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

    public void convoluteImage() {
        int matrixsize = 3;
        loadPixels();
        // Begin our loop for every pixel in the smaller image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++ ) {
                Color c = convolution(x, y, matrix, matrixsize);
                int loc = x + y*width;
                pixels[loc] = c.getRGB();
            }
        }
        updatePixels();
    }

    Color convolution(int x, int y, float[][] matrix, int matrixsize)
    {
        float rtotal = 0.0f;
        float gtotal = 0.0f;
        float btotal = 0.0f;
        int offset = matrixsize / 2;
        for (int i = 0; i < matrixsize; i++){
            for (int j= 0; j < matrixsize; j++){
                // What pixel are we testing
                int xloc = x+i-offset;
                int yloc = y+j-offset;
                int loc = xloc + width*yloc;
                // Make sure we haven't walked off our image, we could do better here
                loc = constrain(loc,0,pixels.length-1);
                // Calculate the convolution
                rtotal += (red(pixels[loc]) * matrix[i][j]);
                gtotal += (green(pixels[loc]) * matrix[i][j]);
                btotal += (blue(pixels[loc]) * matrix[i][j]);
            }
        }
        // Make sure RGB is within range
        rtotal = constrain(rtotal, 0, 255);
        gtotal = constrain(gtotal, 0, 255);
        btotal = constrain(btotal, 0, 255);
        // Return the resulting color
        return new Color((int) rtotal, (int) gtotal, (int) btotal);
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
