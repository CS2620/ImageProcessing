/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.cs2620.imageprocessing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;
import kotlin.Pair;

/**
 *
 * @author bricks
 */
public class CompressionImage extends MyImage {

  public CompressionImage(String filename) {
    super(filename);
  }

  public CompressionImage(int w, int h) {
    super(w, h);
  }

  void compress(LambaPairInterface lambdaA, LambdaBufferedImageInterface lambdaB) {

    Pair<byte[], String> result = lambdaA.call();
    byte[] compressedBytes = result.component1();
    String filename = result.component2();

    BufferedImage newImage = lambdaB.call(compressedBytes);

    if (newImage == null) {
      Graphics g = bufferedImage.getGraphics();
      g.setColor(Color.RED);
      g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
      g.dispose();
    } else {

      //Now calculate the difference
      int sumDifference = 0;
      for (int y = 0; y < bufferedImage.getHeight(); y++) {
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
          int colorOriginal = bufferedImage.getRGB(x, y);
          int colorNew = newImage.getRGB(x, y);

          Pixel pixelOriginal = new Pixel(colorOriginal);
          Pixel pixelNew = new Pixel(colorNew);

          int diff = Math.abs(pixelOriginal.getGrayscale() - pixelNew.getGrayscale());
          sumDifference += diff;
        }
      }

      bufferedImage = newImage;
      Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

      String byteString = "" + compressedBytes.length + " bytes";
      String bitsPerPixelString = "" + compressedBytes.length * 8 / (bufferedImage.getWidth() * bufferedImage.getHeight()) + " bits per pixel";
      String totalDifference = "" + sumDifference + " difference between the images";

      g.setColor(Color.black);
      g.drawString(byteString, 10, 20);
      g.drawString(bitsPerPixelString, 10, 35);
      g.drawString(filename, 10, 50);
      g.drawString(totalDifference, 10, 65);

      g.setColor(Color.cyan);
      g.drawString(byteString, 11, 21);
      g.drawString(bitsPerPixelString, 11, 36);
      g.drawString(filename, 11, 51);
      g.drawString(totalDifference, 11, 66);
    }
  }

  void compressPGM() {
    compress(() -> this.CompressPGM(), (byte[] a) -> this.DecompressPGM(a));
  }

  private Pair<byte[], String> CompressPGM() {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();

    StringBuilder sb = new StringBuilder();

    sb.append("P2\n"); //P2 is the magic number for PGM in ascii
    sb.append("" + width + " " + height + "\n"); //The size of the image
    sb.append("255\n"); //The depth of the color space

    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {

        int color = bufferedImage.getRGB(x, y);
        Pixel p = new Pixel(color);

        int i = p.getGrayscale();
        sb.append(i);
        if (x != bufferedImage.getWidth() - 1) {
          sb.append(" ");
        } else {
          sb.append("\n");
        }
      }
    }
    String string = sb.toString();
    byte[] toReturn = string.getBytes();
    String strDate = "";

    strDate = saveBytesAsFile(strDate, toReturn);

    return new Pair<byte[], String>(toReturn, strDate);

  }

  private BufferedImage DecompressPGM(byte[] pgm) {

    String[] lines = new String(pgm).split("\n");
    int index = 0;

    //Read the magic number line
    if (!lines[index].equals("P2")) {
      System.out.println("The PGM file did not start with the right magic number. Is this really a PGM file?");
      return null;
    }
    index++;

    //Read the width and height
    String[] WidthHeightSplit = lines[index].split(" ");
    int width = Integer.parseInt(WidthHeightSplit[0]);
    int height = Integer.parseInt(WidthHeightSplit[1]);
    BufferedImage toReturn = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    index++;

    //Get the color depth
    int colorDepth = Integer.parseInt(lines[index]);
    if (colorDepth != 255) {
      System.out.println("We only handle PGM files with a color depth of 255");
      return null;
    }
    index++;

    //Now we are actually going to parse the file itself
    for (int y = 0; y < height; y++) {

      if (index >= lines.length) {
        System.out.println("There are not enough lines in the file to match the declared height of the image.");
        return null;
      }

      String rowString = lines[index];
      String[] valueStrings = rowString.split(" ");

      if (valueStrings.length != width) {
        System.out.println("Mismatch if the length of the row in the file and in the declared width of the image.");
        return null;
      }

      for (int x = 0; x < width; x++) {
        int value = Integer.parseInt(valueStrings[x]);
        Pixel pixel = new Pixel(value, value, value);
        toReturn.setRGB(x, y, pixel.getRGB());

      }
      index++;
    }

    return toReturn;
  }

  

  void compressGray() {
    compress(() -> this.CompressGray(), (byte[] a) -> this.DecompressGray(a));
  }

  Pair<byte[], String> CompressGray() {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();

    StringBuilder sb = new StringBuilder();

    sb.append("toGray\n"); //Our made up magic string
    sb.append("" + width + " " + height + "\n"); //The size of the image

    String string = sb.toString();
    byte[] toReturn = string.getBytes();
    String strDate = "";

    strDate = saveBytesAsFile(strDate, toReturn);

    return new Pair<byte[], String>(toReturn, strDate);
  }

  private String saveBytesAsFile(String strDate, byte[] toReturn) {
    try {
      //Get the date as a string, https://www.javatpoint.com/java-date-to-string
      Date date = Calendar.getInstance().getTime();
      DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd---hh,mm,ss");
      strDate = dateFormat.format(date);

      OutputStream os = new FileOutputStream("temp/" + strDate);

      os.write(toReturn);
      System.out.println("Successfully byte inserted");

      // Close the file 
      os.close();
      System.out.println("Wrote the temporary file to " + strDate);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return strDate;
  }

  BufferedImage DecompressGray(byte[] bytes) {

    String[] lines = new String(bytes).split("\n");
    int index = 0;

    //Read the magic number line
    if (!lines[index].equals("toGray")) {
      System.out.println("The toGray file did not start with the right magic number. Is this really a toGray file?");
      return null;
    }
    index++;

    //Read the width and height
    String[] WidthHeightSplit = lines[index].split(" ");
    int width = Integer.parseInt(WidthHeightSplit[0]);
    int height = Integer.parseInt(WidthHeightSplit[1]);
    BufferedImage toReturn = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    index++;

    //Now we are actually going to set every pixel to gray
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Pixel pixel = new Pixel(128, 128, 128);
        toReturn.setRGB(x, y, pixel.getRGB());
      }
      index++;
    }

    return toReturn;
  }
  
  void compressCustom() {
    compress(() -> this.CompressCustom(), (byte[] a) -> this.DecompressCustom(a));
  }
  
  /*
   * Here is where you should edit the code to improve the lossless compression
   * 
   */
  private Pair<byte[], String> CompressCustom() {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();

    StringBuilder sb = new StringBuilder();
    byte[] imageBytes = new byte[width * height];

    sb.append("Cu\n"); //Custom magic Number
    sb.append("" + width + " " + height + "\n"); //The size of the image
    
    for (int y = 0; y < bufferedImage.getHeight(); y++) {
      for (int x = 0; x < bufferedImage.getWidth(); x++) {

        int color = bufferedImage.getRGB(x, y);
        Pixel p = new Pixel(color);

        int i = p.getGrayscale();
        
        byte toSave = (byte)i;
        imageBytes[y * width + x] = toSave;
      }
    }
    String string = sb.toString();
    byte[] headerBytes = sb.toString().getBytes();
    
    byte[] toReturn = new byte[headerBytes.length + imageBytes.length];
    for(int i = 0; i < headerBytes.length; i++){
      toReturn[i] = headerBytes[i];
    }
    for(int i = 0; i < imageBytes.length; i++){
      toReturn[i + headerBytes.length] = imageBytes[i];
    }
    
    String strDate = "";

    strDate = saveBytesAsFile(strDate, toReturn);

    return new Pair<byte[], String>(toReturn, strDate);

  }

  /**
   * 
   * Here is where you should write the decoder for your custom compression algorithm
   */
  private BufferedImage DecompressCustom(byte[] pgm) {

    String[] lines = new String(pgm).split("\n");
    int index = 0;

    //Read the magic number line
    if (!lines[index].equals("Cu")) {
      System.out.println("The Custom file did not start with the right magic number. Is this really a Custom file?");
      return null;
    }
    index++;

    //Read the width and height
    String[] WidthHeightSplit = lines[index].split(" ");
    int width = Integer.parseInt(WidthHeightSplit[0]);
    int height = Integer.parseInt(WidthHeightSplit[1]);
    BufferedImage toReturn = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    index++;
    
    //We need to find this same place in the byte string
    index = 0; 
    int countNewLine = 0;
    for(int i = 0; i < pgm.length && countNewLine < 2; i++){
      
      byte b = pgm[i];
      if(b == 0x0A)//Hex for 11 or \n
      {
        countNewLine++;
      }
      index++;
    }
    System.out.println(index);

    

    //Now we are actually going to parse the file itself
    for (int y = 0; y < height; y++) {

      for (int x = 0; x < width; x++) {
        
        byte value= pgm[index+y * width + x];
        int positiveValue = Byte.toUnsignedInt(value);
        Pixel pixel = new Pixel(positiveValue, positiveValue, positiveValue);
        toReturn.setRGB(x, y, pixel.getRGB());

      }
      
    }

    return toReturn;
  }

}
