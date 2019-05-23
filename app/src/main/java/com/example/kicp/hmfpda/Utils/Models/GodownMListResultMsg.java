package com.example.kicp.hmfpda.Utils.Models;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.Models.GodownMEntity;
import com.example.kicp.hmfpda.Models.UserEntity;
import com.example.kicp.hmfpda.Utils.Enum.StatusCodeEnum;

import java.util.List;

/**
 * 组装入库 请求类.
 */
public class GodownMListResultMsg extends HttpResponseMsg {
    public List<GodownMEntity> Result;

    public void setResult(){
        if(StatusCode == StatusCodeEnum.Success.getValue() && !(Data == null || Data=="")){
            Result =  JSON.parseArray(Data.toString(), GodownMEntity.class);
        }
        else{
            Result = null;
        }
    }
}
