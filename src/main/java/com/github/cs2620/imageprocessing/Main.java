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
    
    
    public Main(){
     System.out.println("Starting server 2.0");
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("./static", Location.EXTERNAL);
            config.addStaticFiles("./images", Location.EXTERNAL);
        }).start(7000);   
        
        
        app.get("/img/:name/:modString", ctx->{
            
            String filename = ctx.pathParam("name");
            String mod = ctx.pathParam("modString");
            
            System.out.println("Got a request for an image with name " + filename + " and modification " + mod);
            
            
            
            
            Stream<Path> paths = Files.list(Paths.get("./images"));
            
            Optional<String> optionalPath = paths.map(p->p.getFileName().toString()).filter(p->p.equals(filename)).findFirst();
           
            if(!optionalPath.isPresent()){//invalid requset
                System.out.println("Invalid image path " + filename);
                throw new BadRequestResponse();
            }
            
            String path = optionalPath.get();
            
            
            
           
            
            
            MyImage image = new MyImage("./images/" + path);
            
            switch(mod){
                case "toRed":
                    image.all(Pixel::toGrayscaleRed);
                    break;
                case "toGreen":
                    image.all(Pixel::toGrayscaleGreen);
                    break;
                case "toBlue":
                    image.all(Pixel::toGrayscaleBlue);
                    break;
                case "histogram":
                    image = image.getGrayscaleHistogramImage();
                case "none":
                    break;
                default:
                    System.out.println("Invalid image modifier " + mod);
                    throw new BadRequestResponse();
            }
            
            
            ctx.result(image.getInputStream()).contentType("image/png");
        });
    }
}
