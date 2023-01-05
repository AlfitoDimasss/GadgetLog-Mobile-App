package com.pietro.gadgetlog.model;

import java.io.Serializable;

public class Gadget implements Serializable {
    private String id, name, brand, img, price;

    public Gadget(String name, String brand, String img) {
        this.name = name;
        this.brand = brand;
        this.img = img;
    }

    public Gadget(String name, String brand, String img, String price) {
        this.name = name;
        this.brand = brand;
        this.img = img;
        this.price = price;
    }

    public Gadget(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
