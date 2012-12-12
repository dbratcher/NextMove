package com.example.nextmove;

import android.graphics.Bitmap;

/** 
 * Represents a linear line as detected by the hough transform. 
 * This line is represented by an angle theta and a radius from the centre. 
 * 
 * @author Olly Oechsle, University of Essex, Date: 13-Mar-2008 
 * @version 1.0 
 */ 
public class HoughLine { 
 
    public double theta; 
    public double r; 
 
    /** 
     * Initialises the hough line 
     */ 
    public HoughLine(double theta, double r) { 
        this.theta = theta; 
        this.r = r; 
    } 

    public boolean vertical() {
	    if (theta < Math.PI * 0.015 || theta > Math.PI * 0.985) {
	    	return true;
	    }
	    return false;
    }
    public boolean horizontal() {
    	if (theta > Math.PI * 0.485 && theta < Math.PI * 0.515) { 
	    	return true;
	    }
	    return false;
    }
    
    public int getX(Bitmap image) {
    	 int height = image.getHeight(); 
         int width = image.getWidth(); 
  
         // During processing h_h is doubled so that -ve r values 
         int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
  
         // Find edge points and vote in array 
         float centerX = width / 2; 
         float centerY = height / 2; 
  
         // Draw edges in output array 
         double tsin = Math.sin(theta); 
         double tcos = Math.cos(theta);
	     int y= height/2;
         int x = (int) ((((r - houghHeight) - ((y - centerY) * tsin)) / tcos) + centerX); 
         return x;
    }
    
    public int getY(Bitmap image) { 
    	int height = image.getHeight(); 
	    int width = image.getWidth(); 
	    
	    // During processing h_h is doubled so that -ve r values 
	    int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
	
	    // Find edge points and vote in array 
	    float centerX = width / 2; 
	    float centerY = height / 2; 
	
	    // Draw edges in output array 
	    double tsin = Math.sin(theta); 
	    double tcos = Math.cos(theta); 
	    int x = width/2;
	    int y = (int) ((((r - houghHeight) - ((x - centerX) * tcos)) / tsin) + centerY); 
	    return y;
	} 
    
    public double slope(Bitmap image){    
    	int height = image.getHeight(); 
	    int width = image.getWidth(); 
	    
	    // During processing h_h is doubled so that -ve r values 
	    int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
	
	    // Find edge points and vote in array 
	    float centerX = width / 2; 
	    float centerY = height / 2; 
	
	    // Draw edges in output array 
	    double tsin = Math.sin(theta); 
	    double tcos = Math.cos(theta); 
	    double x1=1, x2=1, y1=1, y2=1;
	    if (theta < Math.PI * 0.02 || theta > Math.PI * 0.98) {
	    	y1 = 0;
	        x1 = (int) ((((r - houghHeight) - ((y1 - centerY) * tsin)) / tcos) + centerX);
	    	y2 = height;
	        x2 = (int) ((((r - houghHeight) - ((y2 - centerY) * tsin)) / tcos) + centerX);
	    } else if (theta > Math.PI * 0.48 && theta < Math.PI * 0.52) { 
	        // Draw horizontal-sh lines 
	    	x1=0;
	        y1 = (int) ((((r - houghHeight) - ((x1 - centerX) * tcos)) / tsin) + centerY); 
	    	x2=width;
	        y2 = (int) ((((r - houghHeight) - ((x2 - centerX) * tcos)) / tsin) + centerY); 
	    } 
	    return (y2-y1)/(x2-x1);
    }
 
    public double intercept(Bitmap image){    
    	int height = image.getHeight(); 
	    int width = image.getWidth(); 
	    
	    // During processing h_h is doubled so that -ve r values 
	    int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
	
	    // Find edge points and vote in array 
	    float centerX = width / 2; 
	    float centerY = height / 2; 
	
	    // Draw edges in output array 
	    double tsin = Math.sin(theta); 
	    double tcos = Math.cos(theta); 
	    double x1=1, y1=1;
	    if (theta < Math.PI * 0.02 || theta > Math.PI * 0.98) {
	    	y1 = 0;
	        x1 = (int) ((((r - houghHeight) - ((y1 - centerY) * tsin)) / tcos) + centerX);
	    } else if (theta > Math.PI * 0.48 && theta < Math.PI * 0.52) { 
	        // Draw horizontal-sh lines 
	    	x1=0;
	        y1 = (int) ((((r - houghHeight) - ((x1 - centerX) * tcos)) / tsin) + centerY); 
	    } 
	    double slope = this.slope(image);
	    return (y1-x1*slope);
    }
    
    
    /** 
     * Draws the line on the image of your choice with the RGB colour of your choice. 
     */ 
    public void draw(Bitmap image, int color) { 
 
        int height = image.getHeight(); 
        int width = image.getWidth(); 
 
        // During processing h_h is doubled so that -ve r values 
        int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
 
        // Find edge points and vote in array 
        float centerX = width / 2; 
        float centerY = height / 2; 
 
        // Draw edges in output array 
        double tsin = Math.sin(theta); 
        double tcos = Math.cos(theta); 
 
        if (theta < Math.PI * 0.05 || theta > Math.PI * 0.95) { 
            // Draw vertical-ish lines 
            for (int y = 0; y < height; y++) { 
                int x = (int) ((((r - houghHeight) - ((y - centerY) * tsin)) / tcos) + centerX); 
                if (x < width && x >= 0) { 
                    image.setPixel(x, y, color); 
                } 
            } 
        } else if (theta > Math.PI * 0.45 && theta < Math.PI * 0.55) { 
            // Draw horizontal-sh lines 
            for (int x = 0; x < width; x++) { 
                int y = (int) ((((r - houghHeight) - ((x - centerX) * tcos)) / tsin) + centerY); 
                if (y < height && y >= 0) { 
                    image.setPixel(x, y, color); 
                } 
            } 
        } 
    } 
} 