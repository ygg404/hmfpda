package com.example.kicp.hmfpda.Utils.Enum;

/**
 * Http 状态码
 */

public enum StatusCodeEnum
{

    Success(200), //请求(或处理)成功
    Error(500), //内部请求出错
    Unauthorized(401),//未授权标识
    ParameterError(400),//请求参数不完整或不正确
    TokenInvalid(403),//请求TOKEN失效
    HttpMehtodError(405),//HTTP请求类型不合法
    HttpRequestError(406),//HTTP请求不合法
    URLExpireError(407);//HTTP请求不合法

    private final int value;
    //构造方法必须是private或者默认
    private StatusCodeEnum(int value) {
        this.value = value;
    }


    public int getValue() {
        return this.value;
    }


    public StatusCodeEnum valueOf(int value) {
        switch (value) {
            case 200:
                return StatusCodeEnum.Success;
            case 500:
                return StatusCodeEnum.Error;
            case 401:
                return StatusCodeEnum.Unauthorized;
            case 400:
                return StatusCodeEnum.ParameterError;
            case 403:
                return StatusCodeEnum.TokenInvalid;
            case 405:
                return StatusCodeEnum.HttpMehtodError;
            case 406:
                return StatusCodeEnum.HttpRequestError;
            case 407:
                return StatusCodeEnum.URLExpireError;
            default:
                return null;
        }
    }

}
