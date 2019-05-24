package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Models.ReturnEntity;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

import java.util.List;

/**
 * 退货主单 获取类
 */
public class ReturnListResultMsg extends HttpResponseMsg {
    public List<ReturnEntity> Result(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            return JSON.parseArray(Data.toString(), ReturnEntity.class);
        }
        else{
            return null;
        }
    }
}
