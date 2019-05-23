package com.example.kicp.hmfpda.Utils.Models;

import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

/**
 * 订单删除类
 */
public class OrderScanDeleteResultMsg extends HttpResponseMsg{
    public String Result;

    public void setResult(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            Result =   Data.toString() ;
        }
        else{
            Result = null;
        }
    }
}
