package com.pietro.gadgetlog.model;

public class Laptop extends Gadget {
    public Laptop(String name, String brand, String img) {
        super(name, brand, img);
    }

    public Laptop(String name, String brand, String img, String price) {
        super(name, brand, img, price);
    }

    public Laptop() {}
}
