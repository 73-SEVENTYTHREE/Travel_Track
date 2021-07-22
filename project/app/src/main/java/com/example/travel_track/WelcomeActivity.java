package com.example.travel_track;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {
    private final Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Button getLocation = (Button) findViewById(R.id.GetLocation);
            getLocation.setEnabled(true);
            return false;
        }
    });

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    private String cityName = "";
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    TextView CityName = (TextView) findViewById(R.id.CityName);
                    cityName = amapLocation.getCity();
                    CityName.setText(cityName);
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                    Toast.makeText(WelcomeActivity.this, "获取定位失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    public class startLocation extends Thread{
        @Override
        public void run(){
            isGettingLocation = true;
            //初始化定位
            mLocationClient = new AMapLocationClient(getApplicationContext());
            //设置定位回调监听
            mLocationClient.setLocationListener(mLocationListener);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setNeedAddress(true);
            mLocationOption.setOnceLocation(true);
            mLocationOption.setOnceLocationLatest(true);
            mLocationOption.setMockEnable(true);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //启动定位
            mLocationClient.startLocation();
            isGettingLocation = false;
        }
    }
    public volatile boolean isGettingLocation = false;
    public void turnDisabled(Button b){
        b.setEnabled(false);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button getLocation = (Button) findViewById(R.id.GetLocation);
        Button goToMap = (Button) findViewById(R.id.GoToMap);
        getLocation.setOnClickListener(
                v -> {
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0);
                        }
                    };
                    timer.schedule(timerTask, 0, 3000);
                    if(isGettingLocation){
                        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
                        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
                    }
                    Thread startGetLocation = new startLocation();
                    startGetLocation.start();
                }
        );
        goToMap.setOnClickListener(
                v -> {
                    EditText Nickname = findViewById(R.id.Nickname);
                    String name = String.valueOf(Nickname.getText());
                    Intent intent = new Intent(WelcomeActivity.this, MapActivity.class);
                    intent.putExtra("city", !Objects.equals(cityName, "") ? cityName : "未知城市");
                    intent.putExtra("nickname", !Objects.equals(name, "") ? name : "匿名用户");
                    startActivity(intent);
                }
        );
    }
}

