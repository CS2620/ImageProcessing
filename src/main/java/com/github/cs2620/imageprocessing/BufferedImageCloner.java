/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cs2620.imageprocessing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author bricks
 */
public class BufferedImageCloner {
  
  //THis example (and others) can be found at: https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
  static BufferedImage clone(BufferedImage source){
    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
    Graphics g = b.getGraphics();
    g.drawImage(source, 0, 0, null);
    g.dispose();
    return b;
  }
  
}
