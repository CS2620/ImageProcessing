package com.github.cs2620.imageprocessing;

import io.javalin.Javalin;


public class Main {
    
    public static void main(String[] args){
        System.out.println("Starting server");
        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx->ctx.result("Hello World"));
    }
    
}
