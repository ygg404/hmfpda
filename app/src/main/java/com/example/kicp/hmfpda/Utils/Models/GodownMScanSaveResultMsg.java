package com.example.kicp.hmfpda.Utils.Models;

import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

/**
 * 关联箱扫描保存结果.
 */
public class GodownMScanSaveResultMsg extends HttpResponseMsg {
    public int Qty(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return Integer.parseInt( Data.toString() );
        }
        else{
            return 0;
        }
    }
}
