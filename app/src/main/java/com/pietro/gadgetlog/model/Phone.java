package com.pietro.gadgetlog.model;

public class Phone extends Gadget {
    public Phone(String name, String brand, String img) {
        super(name, brand, img);
    }

    public Phone() {}

    public Phone(String name, String brand, String img, String price) {
        super(name, brand, img, price);
    }
}
