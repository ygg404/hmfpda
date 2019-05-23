package com.example.kicp.hmfpda.Models;


/**
 * 扫码类
 */
public class BarcodeEntity {

    public String realBarCode;
    public short grade;
    public String errorMessage;

    public BarcodeEntity(){
        realBarCode = "";
        grade = -1;
        errorMessage = null;
    }
}
