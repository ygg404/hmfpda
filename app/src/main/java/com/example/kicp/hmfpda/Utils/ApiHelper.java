package com.example.kicp.hmfpda.Utils;

import com.alibaba.fastjson.JSON;
import com.example.kicp.hmfpda.LoginActivity;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.TokenResultMsg;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
/**
 * webapi接口帮助类
 */
public class ApiHelper {

    /**获取时间戳
     * 1970年1月1日到当前时间的毫秒数
     * @return
     */
    public static String GetTimeStamp()
    {
        Date nowDate  =  new Date();
        long timeSpan = nowDate.getTime();
        return String.valueOf(timeSpan);
    }

    /**
     * 获取随机数
     * @return
     */
    public static String GetRandom()
    {
        Random rd = new Random(System.currentTimeMillis());
        int i = rd.nextInt(Integer.MAX_VALUE);
        return String.valueOf(i);
    }

    /**
     * 计算机签名
     * @param timeStamp
     * @param nonce
     * @param staffId
     * @param data
     * @param appSecret
     * @return
     */
    public static String GetSignature(String timeStamp, String nonce, int staffId, String data,String appSecret) throws Exception
    {
        MessageDigest md5;
        if (LoginActivity.TokenResult == null)
        {
            throw new Exception("获取令牌失败！");
        }
        try {
            // 生成一个MD5加密计算摘要
            md5 = MessageDigest.getInstance("MD5");
            //拼接签名数据
            String signStr = timeStamp + nonce + staffId + LoginActivity.TokenResult.SignToken.toString() + data + appSecret;

            // 计算md5函数
            md5.update(signStr.getBytes());

        }catch (Exception ex){
            throw new Exception("MD5加密失败！");
        }
        // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
        // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值

        byte[] hash = md5.digest();
        StringBuilder secpwd = new StringBuilder();
        for (int i = 0; i < hash.length; i++)
        {
            int v = hash[i] & 0xFF;
            if (v < 16) secpwd.append(0);
            secpwd.append(Integer.toString(v, 16));
        }
        return secpwd.toString().toUpperCase();
    }

    /**
     * 日期格式 是否有效
     * @param dateValue
     * @return
     */
    public static Date checkDateValid(String dateValue) throws Exception{
        StringBuffer sb = new StringBuffer();
        if (dateValue == null || dateValue.isEmpty()) {
            return null;
        }
        else {
            try {
                SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
                sb.append(dateValue.trim()).insert(4, "-");
                sb.insert(7, "-");
                // fdate.parse( sb.toString() );
                return fdate.parse( sb.toString() );
            } catch(Exception ex)
            {
                throw new Exception("日期格式必须是yyyyMMdd");
            }
        }
    }

    /**
     * GET 接口
     * @param clazz
     * @param webApi
     * @param querymap
     * @param staffId
     * @param appSecret
     * @param sign
     * @param <T>
     * @return
     */
    public static  <T> T GetHttp(Class<T> clazz ,String webApi, HashMap<String, String> querymap, int staffId, String appSecret, boolean sign) throws Exception
    {
        String msg = null;
        T msgClass = null;

        try {
            //签名字符串
            String query = "";
            //请求的数据
            String queryStr = "";

            if(querymap != null){
                //queryMapKeySort 以key按字母大小排列
                HashMap<String, String> queryMapKeySort = new HashMap<String, String>();
                Object[] key_arr = querymap.keySet().toArray();
                Arrays.sort(key_arr);
                for  (Object key : key_arr) {
                    Object value = querymap.get(key);
                    query += ( key.toString() + value.toString() );
                    queryMapKeySort.put( key.toString() , value.toString() );
                }


                for (String key : queryMapKeySort.keySet()) {
                    queryStr += "&" + key + "=" + URLEncoder.encode(queryMapKeySort.get(key), "UTF-8");
                }
                queryStr = queryStr.substring(1, queryStr.length());
            }


            //get请求的url
            URL url=new URL( webApi + queryStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //设置请求方式,请求超时信息
            conn.setRequestMethod("GET");
            conn.setReadTimeout(9000);
            conn.setConnectTimeout(9000);
            // 设置请求的头
            String timestamp = ApiHelper.GetTimeStamp();
            String nonce = ApiHelper.GetRandom();
            conn.setRequestProperty("staffid", String.valueOf(staffId) );
            conn.setRequestProperty("timestamp",timestamp); //发起请求时的时间戳（单位：毫秒）
            conn.setRequestProperty("nonce", nonce); //发起请求时的随机数
            conn.setRequestProperty("query", query);
            if(sign){
                conn.setRequestProperty("signature", GetSignature(timestamp , nonce , staffId, query, appSecret)); //当前请求内容的数字签名
            }
            conn.setUseCaches(false);
            //开启连接
            conn.connect();
            InputStream inputStream=null;
            BufferedReader reader=null;
            //如果应答码为200的时候，表示成功的请求带了，这里的HttpURLConnection.HTTP_OK就是200
            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                //获得连接的输入流
                inputStream=conn.getInputStream();
                //转换成一个加强型的buffered流
                reader=new BufferedReader(new InputStreamReader(inputStream));
                //把读到的内容赋值给result
                String result = reader.readLine();
                msg = result;

            }
            //关闭流和连接
            reader.close();
            inputStream.close();
            conn.disconnect();

            msgClass = JSON.parseObject(  msg , clazz  );
        }catch (Exception ex ){
            throw new Exception( ex.getMessage() == null?ex.toString():ex.getMessage() );
        }

        if( msgClass == null ){
            throw new Exception("网络异常！");
        }
        return msgClass;
    }

    /**
     * 获取token接口
     * @param staffId
     * @return
     */
    public static TokenResultMsg GetSignToken(int staffId) throws Exception{
        String tokenApi = Config.WebApiUrl + "GetToken?";
        HashMap<String,String> query = new HashMap<String, String>();
        query.put("staffId", String.valueOf(staffId));
        TokenResultMsg tokenResultMsg = null;
        try {
            tokenResultMsg = GetHttp(TokenResultMsg.class, tokenApi, query, Config.StaffId, Config.AppSecret, false);
        }catch (Exception ex){
            throw new Exception(ex);
        }
        return tokenResultMsg;
    }

}
