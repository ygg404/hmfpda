package com.example.kicp.hmfpda.Utils.Models;

import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

/**
 * 退货扫码 删除
 */
public class ReturnScanDeleteResultMsg extends HttpResponseMsg {
    public String Result(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return Data.toString() ;
        }
        else{
            return null;
        }
    }
}
