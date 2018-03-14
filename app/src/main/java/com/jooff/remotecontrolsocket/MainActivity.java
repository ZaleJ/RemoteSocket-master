package com.jooff.remotecontrolsocket;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

import static com.jooff.remotecontrolsocket.LoginActivity.mSocket;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";   //在Log语句中标记MainActivity，大概是为了防止拼错。。
    private ArrayList<RemoteSocket> mList;              //定义一个RemoteSocket类的列表，
    private OutputStream mOutputStream;                 //输出流
    private InputStream mInputStream;                   //输入流
    private SocketAdapter sa;                           //套接字适配器？？
    private boolean turned=false;                       //设置是否有过按键操作改变cambat状态

    @Bind(R.id.rv) RecyclerView rv;                     //通过bind方法绑定avtivity.xml中的RecyclerView
    //@Bind(R.id.bmb) BoomMenuButton bmb;                 //通过bind方法绑定avtivity.xml中的BoomMenuButton

    @Override
    public void onCreate(Bundle savedInstanceState) {   //重写主程序创建方法
        super.onCreate(savedInstanceState);             //super父类主程序形参
        setContentView(R.layout.activity_main);         //设置布局xml文件
        ButterKnife.bind(this);                  //黄油刀绑定控件
        setTitle("已连接到：" + mSocket.getInetAddress().toString().substring(1, mSocket.getInetAddress().toString().length()));//设置标题
        initView();         //初始化视图
        initSocket();       //初始化socket
    }


    private void initView() {
        mList = new ArrayList<>();      //初始化ArrayList

        //生成4个RemoteSocket控件设置image与name并放入mList
//        for (int i = 0; i < 2; i++) {
//            RemoteSocket rs = new RemoteSocket();
//            rs.setSocketImage(R.drawable.ic_power_settings_new_grey_500_24dp);
//            rs.setSocketName("S" + String.valueOf(i + 1));
//            mList.add(rs);
//        }
//////////////////////////////////////////////////////
            RemoteSocket rs = new RemoteSocket();
            rs.setSocketImage(R.drawable.ic_power_settings_new_grey_500_24dp);
            rs.setSocketName("S" + String.valueOf(1));
            mList.add(rs);
//////////////////////////////////////////////////////
        sa = new SocketAdapter(mList);  //将mList放入套接字适配器sa中，

        //为套接字适配器sa相应监听检测状态变化监听器
        sa.setOnCheckChangeListener(new SocketAdapter.OnCheckChangeListener() {
            @Override

            //监听器相应线程
            public void onCheckChange(CompoundButton v, final int position, final boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: set");
                        try {
                            //Log.d(TAG, String.valueOf(position + 1));
////////////////////////////////////////////////////////////////////第一个按键翻转操作
                            if(!turned){
                                Log.d(TAG, String.valueOf(position + 1));
                                mOutputStream.write(String.valueOf(position + 1).getBytes());   //将相对应的cambat的值写入输出流，如S1,S2
                                mOutputStream.flush();          // 清空缓存区
                                turned=true;
                            }else{
                                Log.d(TAG, String.valueOf(position + 2));
                                mOutputStream.write(String.valueOf(position + 2).getBytes());   //将相对应的cambat的值写入输出流，如S1,S2
                                mOutputStream.flush();          // 清空缓存区
                                turned=false;
                            }
/////////////////////////////////////////////////////////////////////第一个按键翻转操作

                            //原双按键翻转操作
//                            mOutputStream.write(String.valueOf(position + 1).getBytes());   //将相对应的cambat的值写入输出流，如S1,S2
//                            mOutputStream.flush();          // 清空缓存区
                            Log.d(TAG, "run: flush");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        });

        // 设置adapter
        rv.setAdapter(sa);
        // 设置布局管理器
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

//        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) {
//            TextInsideCircleButton.Builder builder = new TextInsideCircleButton.Builder()
//                    .normalColor(Color.parseColor("#009688"))
//                    .normalImageRes(R.drawable.butterfly)
//                    .normalText("S" + String.valueOf(i + 1))
//                    .listener(new OnBMClickListener() {
//                        @Override
//                        public void onBoomButtonClick(int index) {
//                            showTimePicker("S" + String.valueOf(index + 1));
//                        }
//                    });
//            bmb.addBuilder(builder);
//        }
//        bmb.setHighlightedColor(Color.TRANSPARENT);
//        bmb.setShadowEffect(false);
//        bmb.setNormalColor(Color.TRANSPARENT);
    }

    private void initSocket() {
        try {
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new ReceiveThread()).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOutputStream.write("k".getBytes());
                    mOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showTimePicker(final String relayName) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_time, null);
        final TimePicker mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            String hour = timeTwoNumber(String.valueOf(mTimePicker.getHour()));
                            String minute = timeTwoNumber(String.valueOf(mTimePicker.getMinute()));
                            String alarm = relayName + hour + ":" + minute;
                            onTimeChoose(alarm);
                        }
                    }
                }).show();
    }

    public String timeTwoNumber(String s) {
        if (s.length() == 1) {
            return "0" + s;
        } else return s;
    }

    public void onTimeChoose(final String alarm) {
        SocketAdapter.SocketHolder holder = (SocketAdapter.SocketHolder) rv.findViewHolderForLayoutPosition(Integer.valueOf(String.valueOf(alarm.charAt(1))) - 1);
        holder.alarmImage.setVisibility(View.VISIBLE);
        holder.alarmTime.setText(alarm.substring(2));
        Toast.makeText(MainActivity.this, alarm, Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOutputStream.write(alarm.getBytes());
                    mOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class ReceiveThread implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "run: ok");
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    StringBuilder sb = new StringBuilder();
                    int readSize = mInputStream.read(buffer);
                    sb.append(new String(buffer, 0, readSize));
                    final String s = sb.toString();
                    Observable.create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String> subscriber) {
                            subscriber.onNext(s);
                            subscriber.onCompleted();
                        }
                    }).filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            return s.length() == 4;
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onNext(String s) {
                                    for (int i = 0; i < 4; i++) {
                                        if (s.charAt(i) == '0') {
                                            mList.get(i).setSwitchCompat(true);
                                            mList.get(i).setSocketImage(R.drawable.ic_power_settings_new_black_24dp);
                                            Log.d(TAG, "run: true " + i);
                                        }

                                    }
                                    sa.notifyDataSetChanged();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

