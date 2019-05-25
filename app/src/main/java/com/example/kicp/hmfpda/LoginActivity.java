package com.example.kicp.hmfpda;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.kicp.hmfpda.Models.UserEntity;
import com.example.kicp.hmfpda.Utils.Adialog;
import com.example.kicp.hmfpda.Utils.ApiHelper;
import com.example.kicp.hmfpda.Utils.Config.Config;
import com.example.kicp.hmfpda.Utils.Models.HttpResponseMsg;
import com.example.kicp.hmfpda.Utils.Models.PdaLoginResultMsg;
import com.example.kicp.hmfpda.Utils.Models.Token;
import com.example.kicp.hmfpda.Utils.Models.TokenResultMsg;
import com.example.kicp.hmfpda.Utils.ProgersssDialog;
import com.example.kicp.hmfpda.Utils.Public;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import static com.example.kicp.hmfpda.Utils.Config.Config.ConfigInit;
import static com.example.kicp.hmfpda.Utils.FileHelper.FileHelper.FileInit;


/**
 * 登录界面
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {

    public static String currentStaffId = ""; //当前的用户id
    public static Token TokenResult = null;    //令牌
    public static boolean onlineFlag = true;    //在线标志
    public static String CreateUserId = ""; //用户id

    @ViewInject(R.id.login_id)
    private EditText id_login;
    @ViewInject(R.id.login_password)
    private EditText password_login;
    @ViewInject(R.id.login_button)
    private Button button_login;
    @ViewInject(R.id.line_group)
    private RadioGroup radioGroup;
    @ViewInject(R.id.radio_online)
    private RadioButton onlineRadiobtn;
    @ViewInject(R.id.radio_offline)
    private RadioButton offlineRadiobtn;

    private ProgersssDialog mProgersssDialog;
    private Adialog mAdialog;
    private Context mContext;

    Handler eHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //登录加载dialog关闭
            mProgersssDialog.cancel();
            switch (msg.what) {
                case 0:
                    //do something,refresh UI;
                    mAdialog.failDialog(  msg.obj.toString() );
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 登录线程（在线）
     */
    Runnable loadRun = new Runnable(){
        @Override
        public void run() {

            // TODO Auto-generated method stub
            String username = id_login.getText().toString().trim();
            String pwd      = password_login.getText().toString().trim();

            Message message = new Message();
            HashMap<String,String> query = new HashMap<String, String>();
            query.put("account",username);
            query.put("password",pwd);
            try{
                //获取token
                TokenResultMsg tokenmsg = ApiHelper.GetSignToken( Config.StaffId );
                if(tokenmsg.StatusCode!= 200){
                    throw new Exception( tokenmsg.Info );
                }
                TokenResult = tokenmsg.Result();
                currentStaffId = id_login.getText().toString().trim();

                //登录
                PdaLoginResultMsg msgc = ApiHelper.GetHttp(PdaLoginResultMsg.class, Config.WebApiUrl + "PdaLogin?",query,
                        Config.StaffId, Config.AppSecret ,true);
                if(msgc.StatusCode != 200)
                {
                    throw new Exception(msgc.Info);
                }
                CreateUserId = msgc.Data.toString(); // 登录成功获取用户ID
                //登录加载dialog关闭
                mProgersssDialog.cancel();

                //登录成功
                //新建一个Intent
                Intent intent = new Intent();
                //制定intent要启动的类
                intent.setClass(LoginActivity.this, MainActivity.class);
                //启动一个新的Activity 跳转主菜单
                startActivity(intent);
                //关闭当前的
                LoginActivity.this.finish();

            }catch (Exception ex){
                message.obj = ex.getMessage();
                eHandler .sendMessage(message);
                return;
            }

        }
    };

    /**
     * 登录线程（离线）
     */
    Runnable loadRunOffline = new Runnable() {
        @Override
        public void run() {
            String username = id_login.getText().toString().trim();
            String pwd      = password_login.getText().toString().trim();

            String userFile = mContext.getFilesDir().getPath().toString() + Public.UserPath;
            short loginResult = 0;  //-1：账户密码不匹配； 0:账号不存在； 1: 离线登录成功；
            Message message = new Message();
            try {
                String line;
                File file = new File(userFile);
                if (file.exists()){
                    FileReader fr = new FileReader(userFile);
                    BufferedReader br = new BufferedReader(fr);
                    while ((line = br.readLine()) != null) {
                        UserEntity user = new UserEntity();
                        String[] lineMembers = line.split(",");
                        user.UserId = lineMembers[0];
                        user.PadAccount = lineMembers[1];
                        user.RealName = lineMembers[2];
                        user.PdaPassWord = lineMembers[3];
                        if (username.equals(user.PadAccount) && !pwd.equals(user.PdaPassWord)) {
                            loginResult = -1;
                        }

                        if (username.equals(user.PadAccount) && pwd.equals(user.PdaPassWord)) {
                            loginResult = 1;
                            CreateUserId = user.UserId;
                        }
                    }
                    br.close();
                    fr.close();

                    if(loginResult == -1){
                        throw  new Exception("账号和密码不匹配！");
                    }else if(loginResult == 0){
                        throw  new Exception("该账户不存在！");
                    }else{
                        //登录加载dialog关闭
                        mProgersssDialog.cancel();
                        //离线登录成功
                        currentStaffId = String.valueOf(Config.StaffId);
                        //新建一个Intent
                        Intent intent = new Intent();
                        //制定intent要启动的类
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        //启动一个新的Activity 跳转主菜单
                        startActivity(intent);
                        //关闭当前的
                        LoginActivity.this.finish();
                    }
                }
                else{
                    throw  new Exception("用户离线登录文件不存在！");
                }

            }catch (Exception ex){
                message.obj = ex.getMessage().toString();
                eHandler.sendMessage(message);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        x.view().inject(this);

        bindViews();
    }

    /**
     * 初始化控件
     */
    private void bindViews() {
        mContext = this.getApplicationContext();
        mAdialog = new Adialog(LoginActivity.this);
        LimitsEditEnter(id_login);
        LimitsEditEnter(password_login);
        onlineRadiobtn.toggle();
        radioGroup.setOnCheckedChangeListener(listener);

        try {
            //AppConfig 配置文件读取
            ConfigInit(mContext);
            //初始化文件
            FileInit(mContext);
        }catch (Exception ex){
            mAdialog.failDialog(ex.getMessage());
        }


        mProgersssDialog = null;

    }

    //登录按钮
    @Event(value = R.id.login_button,type = View.OnClickListener.class)
    private void loginThread(View v){
        mProgersssDialog = new ProgersssDialog(LoginActivity.this);
        if(onlineFlag){
            //在线
            new Thread(loadRun).start();
        }
        else{
            //离线
            new Thread(loadRunOffline).start();
        }
    }

    /**
     * 离线、在线选择方式
     */
    private RadioGroup.OnCheckedChangeListener listener =new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.radio_offline:
                    onlineFlag = false;
                    break;
                case R.id.radio_online:
                    onlineFlag = true;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 限制回车换行
     * @param et
     */
    private void LimitsEditEnter(final EditText et){
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //TODO:回车键按下时要执行的操作
                return (keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_ENTER);
            }
        });

        //监听输入框禁止输入换行符
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("\n")) {
                    et.setText(s.toString().replace("\n",""));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                    ;
            }
        });

    }
}
