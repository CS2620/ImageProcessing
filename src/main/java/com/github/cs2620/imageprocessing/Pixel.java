/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cs2620.imageprocessing;

import java.awt.Color;


public class Pixel {

    private int r, g, b;
    
    public Pixel(int i){
        
        
        
        
        int r = (i >> 16) & 0xff;
        int g = (i >> 8) & 0xff;
        int b = i & 0xff;
        
        this.r = r;
        this.g = g;
        this.b = b;
    }

    
    

    public void toGrayscale() {
        int gray = (r + g + b) / 3;
        grayscale(gray);

    }
    
    public void lessSaturated(){
        
        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        
        float h = hsb[0];//All values 0-1
        float s = hsb[1];
        float v = hsb[2];
        
        s = .5f;
        
        int i = Color.HSBtoRGB(h, s, v);
        
        int r = (i >> 16) & 0xff;
        int g = (i >> 8) & 0xff;
        int b = i & 0xff;
        
        this.r = r;
        this.g = g;
        this.b = b;
        
        
    }
    
    public void moreSaturated(){
        
        float[] hsb = new float[3];
        Color.RGBtoHSB(r, g, b, hsb);
        
        float h = hsb[0];//All values 0-1
        float s = hsb[1];
        float v = hsb[2];
        
        s = 1;
        
        int i = Color.HSBtoRGB(h, s, v);
        
        int r = (i >> 16) & 0xff;
        int g = (i >> 8) & 0xff;
        int b = i & 0xff;
        
        this.r = r;
        this.g = g;
        this.b = b;
        
        
    }

    public void toRed() {
        grayscale(r);
    }

    public void toGreen() {
        grayscale(g);
    }

    public void toBlue() {
        grayscale(b);
    }

    private void grayscale(int i) {
        r = i;
        g = i;
        b = i;
    }

    public Color getColor() {
        return new Color(r, g, b);
    }

}
