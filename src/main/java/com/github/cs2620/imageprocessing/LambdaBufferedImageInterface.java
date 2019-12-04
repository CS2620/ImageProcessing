/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cs2620.imageprocessing;

import java.awt.image.BufferedImage;

/**
 *
 * @author bricks
 */
public interface LambdaBufferedImageInterface {
  BufferedImage call(byte[] input);
  
}
