package com.github.cs2620.imageprocessing;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.staticfiles.Location;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) {

    new Main();

  }

  public Main() {
    System.out.println("Starting server 2.0");
    Javalin app = Javalin.create(config -> {
      config.addStaticFiles("./static", Location.EXTERNAL);
      config.addStaticFiles("./images", Location.EXTERNAL);
    }).start(7000);

    app.get("/listThumbnails", ctx -> {
      Stream<Path> paths = Files.list(Paths.get("./images"));

      String toReturn = String.join("\n", paths.map(p -> p.getFileName().toString()).toArray(String[]::new));

      ctx.result(toReturn);

    });

    app.get("/img/:name/:modString", ctx -> {

      String filename = ctx.pathParam("name");
      String mod = ctx.pathParam("modString");

      System.out.println("Got a request for an image with name " + filename + " and modification " + mod);

      Stream<Path> paths = Files.list(Paths.get("./images"));

      Optional<String> optionalPath = paths.map(p -> p.getFileName().toString()).filter(p -> p.equals(filename)).findFirst();

      if (!optionalPath.isPresent()) {//invalid requset
        System.out.println("Invalid image path " + filename);
        throw new BadRequestResponse();
      }

      String path = optionalPath.get();

      MyImage image = new MyImage("./images/" + path);

      String[] mods = mod.split(":");
      for (int i = 0; i < mods.length; i++) {

        String thisMod = mods[i];
        String arg = "";
        if (thisMod.contains(",")) {
          String[] splits = thisMod.split(",");
          thisMod = splits[0];
          arg = splits[1];
        }

        switch (thisMod) {
          case "slice":
            final int intArg = Integer.parseInt(arg);
            image.all(p -> p.slice(intArg));
            break;
          case "slice_rebuild":
            int intArg2 = Integer.parseInt(arg);            
            image.sliceRebuild(intArg2);
            break;
          case "toGray":
          case "toGrey":
            image.all(p -> p.toGrayscale());
            break;

          case "toRed":
            image.all(p -> p.toGrayscaleRed());
            break;
          case "toGreen":
            image.all(Pixel::toGrayscaleGreen);
            break;
          case "toBlue":
            image.all(Pixel::toGrayscaleBlue);
            break;
          case "histogram":
            image = image.getGrayscaleHistogramImage();
            break;
          case "simpleAdjustForExposure":
            image.simpleAdjustForExposure();
            break;
          case "autoAdjustForExposure":
            image.autoAdjustForExposure();
            break;
          case "kernel-blur":
            image.applyKernelBlur();
            break;
          case "kernel-edge":
            image.applyKernelEdge();
            break;
          case "kernel-sharp":
            image.applyKernelSharp();
            break;
          case "crop":
            image.crop();
            break;
          case "crop_scaleLinear":
            image.crop();
            image.scaleLinear();
            break;
          case "crop_scaleBilinear":
            image.crop();
            image.scaleBilinear();
            break;
          case "flip_horizontal":
            image.flipHorizontal();
            break;
          case "flip_vertical":
            image.flipVertical();
            break;
          case "rotate_clockwise":
            image.rotateClockwise();
            break;
          case "rotate_counter_clockwise":
            image.rotateCounterClockwise();
            break;
          case "color_reduce":
            image.colorReduce();
            break;
          case "dither_bw":
            image.ditherBW();
            break;
          case "dither_color":
            image.ditherColor();
            break;
          case "threshold":
            image.threshold();
            break;
          case "basic_green_screen":
            image.basicGreenScreen();
            break;
          case "basic_green_screen_mask":
            image.setBasicGreenScreenMask();
            break;
          case "advanced_green_screen":
            image.advancedGreenScreen();
            break;
          case "advanced_green_screen_mask":
            image.setAdvancedGreenScreenMask();
            break;
          case "yuv_green_screen":
            image.YUVGreenScreen();
            break;
          case "yuv_green_screen_mask":
            image.setYUVGreenScreenMask();
            break;
          case "to_yuv_v":
            image.toYUV_V();
            break;
          case "to_yuv_y":
            image.toYUV_Y();
            break;
          case "to_yuv_u":
            image.toYUV_U();
            break;
          case "rotate_arbitrary":
            float floatArg = Float.parseFloat(arg);  
            image.rotateArbitrary(floatArg);
            break;
          case "compress":
            image.compress();
            break;
          case "none":
            break;
          default:
            System.out.println("Invalid image modifier " + mod);
            throw new BadRequestResponse();
        }
      }

      ctx.result(image.getInputStream()).contentType("image/png");
    });
  }
}
