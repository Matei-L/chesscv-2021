package com.fii.chesscv;

public class FenRequest {
    public String facing;
    public String url;

    public FenRequest(String url, String facing) {
        this.facing = facing;
        this.url = url;
    }
}
