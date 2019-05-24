package com.example.kicp.hmfpda.Utils.Models;

import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

/**
 * 出库扫描明细保存返回结果
 */

public class OrderScanSaveResultMsg extends HttpResponseMsg{
    public int Qty(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return Integer.parseInt( Data.toString() );
        }
        else{
            return 0;
        }
    }
}
