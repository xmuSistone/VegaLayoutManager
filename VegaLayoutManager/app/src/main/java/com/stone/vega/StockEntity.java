package com.stone.vega;

/**
 * Created by xmuSistone on 2017/9/20.
 */

public class StockEntity {

    private String name;
    private float price;
    private int flag;
    private String gross;

    public StockEntity(String name, float price, int flag, String gross) {
        this.name = name;
        this.price = price;
        this.flag = flag;
        this.gross = gross;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public int getFlag() {
        return flag;
    }

    public String getGross() {
        return gross;
    }
}
