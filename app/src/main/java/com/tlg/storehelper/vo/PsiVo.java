package com.tlg.storehelper.vo;

public class PsiVo {
    /**收货 receiving*/
    public int r;
    /**发货 delivery*/
    public int d;
    /**销售数量 sales*/
    public int s;
    /**销售金额 sales amount*/
    public int sa;
    /**库存 inventory*/
    public int i;

    public PsiVo(int receiving, int delivery, int sales, int salesAmount, int inventory) {
        this.r = receiving;
        this.d = delivery;
        this.s = sales;
        this.sa = salesAmount;
        this.i = inventory;
    }
}
