package com.jooff.remotecontrolsocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mengpeng.mphelper.ToastUtils;

import java.net.Socket;
import java.util.concurrent.Callable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    public static Socket mSocket;
    private SharedPreferences pref;

    @Bind(R.id.input_ip) EditText ipText;
    @Bind(R.id.input_port) EditText portText;
    @Bind(R.id.remember_ip) CheckBox ipRemember;

    ////////////////一些偏好的使用以及原型

//    // get it
//    SharedPreferences p = mContext.getSharedPreferences("Myprefs", Context.MODE_PRIVATE);
//    // or
//        p = PreferenceManager.getDefaultSharedPreferences(mContext);
//
//    // read
//    p.getString("preference_key", "default value");
//
//    // write
//    p.edit().putString("preference_key", "new value").commit();
//    // or
//    p.edit().putString("preference_key", "new value").apply();
    ////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);    //设置登陆界面xml文件
        ButterKnife.bind(this);              //在onCreate中绑定Activity
        pref = PreferenceManager.getDefaultSharedPreferences(this);//每个应用有一个默认的偏好文件preferences.xml，使用getDefaultSharedPreferences获取
        boolean isRemember = pref.getBoolean("remember_ip", false); //布尔型变量，获取偏好是否记住ip
        if (isRemember) {
        ipText.setText(pref.getString("ip", ""));               //.getString()键值与默认值
            portText.setText(String.valueOf(pref.getInt("port", 8080)));//同上设置键值与默认值
            ipRemember.setChecked(true);            //表示设置记住标签为true，已检查过？
        }
    }

    @OnClick(R.id.btn_login)
    void setLoginButton() {
        final String ip = ipText.getText().toString();
        final int port = Integer.valueOf(portText.getText().toString());
        ToastUtils.getInstance().initToast(this);
        Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mSocket = new Socket(ip, port);
                return true;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        //Toast.makeText(LoginActivity.this, "已连接到 " + ip, Toast.LENGTH_SHORT).show();
                        ToastUtils.onSuccessShowToast( "成功toast");
                        if (ipRemember.isChecked()) {
                            pref.edit().putBoolean("remember_ip", true).apply();
                            pref.edit().putString("ip", ip).apply();
                            pref.edit().putInt("port", port).apply();
                        } else {
                            pref.edit().putBoolean("remember_ip", false).apply();
                        }
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Toast.makeText(LoginActivity.this, R.string.error_invalid_ip, Toast.LENGTH_SHORT).show();
                        ToastUtils.onErrorShowToast( "失败toast");
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        LoginActivity.this.finish();
    }

}

