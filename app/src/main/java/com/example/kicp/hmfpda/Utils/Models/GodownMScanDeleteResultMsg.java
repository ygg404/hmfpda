package com.example.kicp.hmfpda.Utils.Models;

import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

/**
 * 组装入库删除.
 */
public class GodownMScanDeleteResultMsg extends HttpResponseMsg {
    public String Result(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return Data.toString() ;
        }
        else{
            return null;
        }
    }
}
