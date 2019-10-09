/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cs2620.imageprocessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class MyImage {

  BufferedImage bufferedImage;

  /**
   * Create a new image instance from the given file
   *
   * @param filename The file to load
   */
  public MyImage(String filename) {
    try {
      bufferedImage = ImageIO.read(new File(filename));
    } catch (IOException ex) {
      Logger.getLogger(MyImage.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public MyImage(int w, int h) {
    bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
  }

  /**
   * Run a pixel operation on each pixel in the image
   *
   * @param pi The pixel operation to run
   */
  public void all(PixelInterface pixelInterface) {

    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        int color_int = bufferedImage.getRGB(x, y);

        Pixel p = new Pixel(color_int);

        pixelInterface.PixelMethod(p);

        bufferedImage.setRGB(x, y, p.getColor().getRGB());

      }
    }

  }

  /**
   * Save the file to the given location
   *
   * @param filename The location to save to
   */
  public void save(String filename) {

    try {
      ImageIO.write(bufferedImage, "PNG", new File(filename));
    } catch (IOException ex) {
      Logger.getLogger(MyImage.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public InputStream getInputStream() throws IOException {

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", os);
    InputStream is = new ByteArrayInputStream(os.toByteArray());
    return is;

  }

  public int[] getGrayscaleHistogram() {
    int[] histogram = new int[256];

    //Bin each pixel in the histogram
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        int color_int = bufferedImage.getRGB(x, y);

        Pixel p = new Pixel(color_int);

        int grayscale = (int) (p.getValue() * 255);
        histogram[grayscale]++;

      }
    }

    return histogram;
  }

  public MyImage getGrayscaleHistogramImage() {
    int[] histogram = getGrayscaleHistogram();
    //Find the biggest bin
    int max = 0;
    for (int h = 0; h < 256; h++) {
      if (histogram[h] > max) {
        max = histogram[h];
      }
    }

    System.out.println("The biggest histogram value is " + max);

    MyImage toReturn = new MyImage(256, 50);

    //Go across and create the histogram
    for (int x = 0; x < 256; x++) {
      int localMax = histogram[x] * 50 / max;
      for (int y = 0; y < 50; y++) {
        int localY = 50 - y;

        if (histogram[x] == 0) {
          toReturn.bufferedImage.setRGB(x, y, Color.RED.getRGB());
        } else {

          if (localY < localMax) {
            toReturn.bufferedImage.setRGB(x, y, new Pixel(x, x, x).getColor().getRGB());
          } else {
            toReturn.bufferedImage.setRGB(x, y, new Pixel((x + 128) % 255, (x + 128) % 255, (x + 128) % 255).getRGB());
          }
        }

      }
    }

    return toReturn;

  }

  public void simpleAdjustForExposure() {

    int[] histogram = getGrayscaleHistogram();
    int firstIndexWithValue = 255;
    int lastIndexWithValue = 0;
    for (int i = 0; i < 256; i++) {
      if (histogram[i] != 0) {
        if (i > lastIndexWithValue) {
          lastIndexWithValue = i;
        }
        if (i < firstIndexWithValue) {
          firstIndexWithValue = i;
        }
      }
    }

    System.out.println("The first histogram bin with a non-zero value is: " + firstIndexWithValue);
    System.out.println("The last histogram bin with a non-zero value is: " + lastIndexWithValue);

    //Now stretch the pixels to fill the whole image.
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        int color_int = bufferedImage.getRGB(x, y);

        Pixel p = new Pixel(color_int);

        int grayscale = (int) (p.getValue() * 255);
        float newGrayscale = (grayscale - firstIndexWithValue) / (float) (lastIndexWithValue - firstIndexWithValue);
        p.setValue(newGrayscale);

        bufferedImage.setRGB(x, y, p.getColor().getRGB());

      }
    }

    histogram = getGrayscaleHistogram();
    firstIndexWithValue = 255;
    lastIndexWithValue = 0;
    for (int i = 0; i < 256; i++) {
      if (histogram[i] != 0) {
        if (i > lastIndexWithValue) {
          lastIndexWithValue = i;
        }
        if (i < firstIndexWithValue) {
          firstIndexWithValue = i;
        }
      }
    }

    System.out.println("After adjusting for exposure, the first histogram bin with a non-zero value is: " + firstIndexWithValue);
    System.out.println("After adjusting for exposure, the last histogram bin with a non-zero value is: " + lastIndexWithValue);

  }

  public void autoAdjustForExposure() {

    int numPixels = bufferedImage.getWidth() * bufferedImage.getHeight();
    float idealPixels = numPixels / 256.0f;
    int[] currentHistogram = getGrayscaleHistogram();

    int[] finalHistogram = new int[256];
    int[] map = new int[256];
    int finalIndex = 0;
    float currentOverflow = 0;

    for (int i = 0; i < 256; i++) {

      int finalSize = finalHistogram[finalIndex];
      int currentValues = currentHistogram[i];
      int newFinalSize = finalSize + currentValues;

      if (newFinalSize < idealPixels) {
        finalHistogram[finalIndex] = newFinalSize;
        map[i] = finalIndex;
      } else {
        finalHistogram[finalIndex] = newFinalSize;
        map[i] = finalIndex;
        currentOverflow += (finalHistogram[finalIndex] - idealPixels);
        finalIndex++;
        while (currentOverflow > idealPixels) {
          finalIndex++;
          currentOverflow -= idealPixels;
        }

      }
    }

    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        int color_int = bufferedImage.getRGB(x, y);

        Pixel p = new Pixel(color_int);

        int grayscale = (int) (p.getValue() * 255);
        float newGrayscale = map[grayscale] / 256.0f;
        p.setValue(newGrayscale);

        bufferedImage.setRGB(x, y, p.getColor().getRGB());

      }
    }

  }

  void applyKernelEdge() {
    float[][] kernel = new float[3][3];

    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        kernel[y][x] = 0;
        if (x == 1 && y == 0) {
          kernel[y][x] = -1.0f;
        }
        if (x == 1 && y == 2) {
          kernel[y][x] = -1.0f;
        }
        if (x == 0 && y == 1) {
          kernel[y][x] = -1.0f;
        }
        if (x == 2 && y == 1) {
          kernel[y][x] = -1.0f;
        }
        if (x == 1 && y == 1) {
          kernel[y][x] = 4.0f;

        }
      }
    }

    doKernel(kernel);

  }

  void applyKernelBlur() {
    float[][] kernel = new float[3][3];

    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        kernel[y][x] = 1 / 9.0f;

      }
    }

    doKernel(kernel);

  }

  void applyKernelSharp() {
    float[][] kernel = new float[3][3];

    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        kernel[y][x] = 0;
        if (x == 1 && y == 0) {
          kernel[y][x] = -1.0f;
        }
        if (x == 1 && y == 2) {
          kernel[y][x] = -1.0f;
        }
        if (x == 0 && y == 1) {
          kernel[y][x] = -1.0f;
        }
        if (x == 2 && y == 1) {
          kernel[y][x] = -1.0f;
        }
        if (x == 1 && y == 1) {
          kernel[y][x] = 5.0f;

        }
      }
    }

    doKernel(kernel);

  }

  private void doKernel(float[][] kernel) {
    BufferedImage temp = BufferedImageCloner.clone(bufferedImage);
    for (int y = 0; y < temp.getHeight(); y++) {
      for (int x = 0; x < temp.getWidth(); x++) {
        int color_int = temp.getRGB(x, y);

        Pixel p = new Pixel(color_int);
        p.toGrayscale();
        temp.setRGB(x, y, p.getColor().getRGB());
      }
    }

    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {
        float sum = 0;
        for (int ky = -1; ky <= 1; ky++) {
          for (int kx = -1; kx <= 1; kx++) {
            int color_int = 0;

            int px = x + kx;
            int py = y + ky;
            if (px >= 0 && px < bufferedImage.getWidth() && py >= 0 && py < bufferedImage.getHeight()) {
              color_int = temp.getRGB(px, py);
            }
            Pixel p = new Pixel(color_int);
            p.toGrayscale();
            float grayscale = p.getRed();
            float kernelValue = kernel[kx + 1][ky + 1];
            sum += (grayscale * kernelValue);

          }
        }
        int intSum = (int) (sum);

        bufferedImage.setRGB(x, y, new Pixel(intSum, intSum, intSum).getRGB());
      }
    }
  }

  void crop() {
    BufferedImage temp = new BufferedImage(bufferedImage.getWidth() / 4, bufferedImage.getHeight() / 4, BufferedImage.TYPE_4BYTE_ABGR);
    for (int y = 0; y < temp.getHeight(); y++) {
      for (int x = 0; x < temp.getWidth(); x++) {
        int color_int = bufferedImage.getRGB(x + bufferedImage.getWidth() / 2 - bufferedImage.getWidth() / 8, y + bufferedImage.getHeight() / 2 - bufferedImage.getHeight() / 8);

        Pixel p = new Pixel(color_int);
        temp.setRGB(x, y, p.getColor().getRGB());
      }
    }

    bufferedImage = temp;
  }

  void scaleLinear() {
    BufferedImage temp = new BufferedImage(bufferedImage.getWidth() * 4, bufferedImage.getHeight() * 4, BufferedImage.TYPE_4BYTE_ABGR);
    for (int y = 0; y < temp.getHeight(); y++) {
      for (int x = 0; x < temp.getWidth(); x++) {
        int color_int = bufferedImage.getRGB((int) (x / 4.0f), (int) (y / 4.0f));

        Pixel p = new Pixel(color_int);
        temp.setRGB(x, y, p.getColor().getRGB());
      }
    }

    bufferedImage = temp;
  }

  void scaleBilinear() {
    BufferedImage temp = new BufferedImage(bufferedImage.getWidth() * 4, bufferedImage.getHeight() * 4, BufferedImage.TYPE_4BYTE_ABGR);
    for (int y = 0; y < temp.getHeight(); y++) {
      for (int x = 0; x < temp.getWidth(); x++) {

        float landX = x / 4.0f;
        float landY = y / 4.0f;

        int lesserX = (int) landX;
        int greaterX = lesserX + 1;
        int lesserY = (int) landY;
        int greaterY = lesserY + 1;

        int[][] coordsX = new int[2][2];
        int[][] coordsY = new int[2][2];

        coordsX[0][0] = lesserX;
        coordsY[0][0] = lesserY;

        coordsX[0][1] = greaterX;
        coordsY[0][1] = lesserY;

        coordsX[1][0] = lesserX;
        coordsY[1][0] = greaterY;

        coordsX[1][1] = greaterX;
        coordsY[1][1] = greaterY;

        int[][] color_ints = new int[2][2];
        for (int y2 = 0; y2 < 2; y2++) {
          for (int x2 = 0; x2 < 2; x2++) {
            int getX = coordsX[y2][x2];
            int getY = coordsY[y2][x2];
            if (getX >= bufferedImage.getWidth()) {
              getX = bufferedImage.getWidth() - 1;
            }
            if (getY >= bufferedImage.getHeight()) {
              getY = bufferedImage.getHeight() - 1;
            }
            color_ints[y2][x2] = bufferedImage.getRGB(getX, getY);
          }
        }

        //Now that I have my colors, calculate my final color.
        Pixel color_top = Pixel.interpolate(color_ints[0][0], color_ints[0][1], landX - lesserX);
        Pixel color_bottom = Pixel.interpolate(color_ints[1][0], color_ints[1][1], landX - lesserX);

        Pixel finalPixel = Pixel.interpolate(color_top, color_bottom, landY - lesserY);

        temp.setRGB(x, y, finalPixel.getColor().getRGB());
      }
    }

    bufferedImage = temp;
  }

  void sliceRebuild(int start) {

    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {

        int color_int = bufferedImage.getRGB(x, y);

        Pixel finalPixel = new Pixel(color_int);

        int[] slices = new int[8];

        for (int i = 0; i < 8; i++) {
          slices[i] = finalPixel.getSlice(i);
        }
        int rebuild = 0;
        for (int i = start; i < 8; i++) {
          int toAdd = slices[i] == 255 ? 1 : 0;
          rebuild += toAdd << (i);

        }

        bufferedImage.setRGB(x, y, new Pixel(rebuild, rebuild, rebuild).getColor().getRGB());
      }
    }

  }

}
