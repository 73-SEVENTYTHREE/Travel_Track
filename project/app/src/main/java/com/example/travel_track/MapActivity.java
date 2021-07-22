package com.example.travel_track;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class MapActivity extends AppCompatActivity{
    MapView mMapView = null;
    TextView textView;
    Button addCity;
    Button checkCities;
    String nickname;
    //保存文件的路径
    String path;

    private double latitude;
    private double longitude;
    private String city = "";
    private List<String> cityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + nickname + ".txt";
        read();
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>(cityList);
        cityList = new ArrayList<String>(hashSet);
        save();
        final String[] cityName = {intent.getStringExtra("city")};

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        AMap map = mMapView.getMap();
        map.moveCamera(CameraUpdateFactory.zoomOut());
        map.moveCamera(CameraUpdateFactory.zoomOut());
        map.moveCamera(CameraUpdateFactory.zoomOut());
        map.moveCamera(CameraUpdateFactory.zoomOut());
        map.moveCamera(CameraUpdateFactory.zoomOut());
        GeocodeSearch geocodeSearch = new GeocodeSearch(MapActivity.this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                city = regeocodeAddress.getCity();
                textView = (TextView) findViewById(R.id.location);
                textView.setText("纬度：" + latitude + " " + "经度" + longitude + "\n" +
                        "城市名称：" + city);
                DistrictSearch search = new DistrictSearch(MapActivity.this);
                DistrictSearchQuery query2 = new DistrictSearchQuery();
                query2.setKeywords(city);//传入关键字
                query2.setShowBoundary(true);//是否返回边界值
                search.setQuery(query2);
                search.setOnDistrictSearchListener(districtResult -> {
                    List<DistrictItem> result = districtResult.getDistrict();
                    String[] latLngs = result.get(0).districtBoundary();
                    Log.e("TAG", latLngs[0]);
                    List<String> pair = Arrays.asList(latLngs[0].split(";"));
                    List<LatLng> lat_lng = new ArrayList<>();
                    for(String item : pair){
                        Double lat = Double.valueOf(item.split(",")[1]);
                        Double lng = Double.valueOf(item.split(",")[0]);
                        lat_lng.add(new LatLng(lat, lng));
                    }
                    Polyline polyline = map.addPolyline(new PolylineOptions().
                            addAll(lat_lng).width(3).color(Color.argb(255, 1, 1, 1)));
                });//绑定监听器
                search.searchDistrictAnsy();//开始搜索
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        Toast.makeText(MapActivity.this, "欢迎来自" + cityName[0] + "的" + nickname, Toast.LENGTH_SHORT).show();
        map.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                map.clear();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
                LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
                //异步查询
                geocodeSearch.getFromLocationAsyn(query);

            }
        });
        addCity = (Button) findViewById(R.id.addCity);
        addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(city.equals("")){
                    Toast.makeText(MapActivity.this, "请选择城市", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(cityList.contains(city)){
                        Toast.makeText(MapActivity.this, "该城市已添加", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        cityList.add(city);
                        save();
                        Toast.makeText(MapActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("TAG", cityList.toString());
                }
            }
        });
        checkCities = (Button) findViewById(R.id.checkMyCities);
        checkCities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, CityListActivity.class);
                intent.putExtra("nickname", nickname);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    //读取数据
    public void read(){
        ObjectInputStream ois=null;
        try {
            Log.e("TAG", new File(path).getAbsolutePath()+"<---");
            //获取输入流
            ois=new ObjectInputStream(new FileInputStream(new File(path)));
            //获取文件中的数据
            CityList record = (CityList) ois.readObject();
            //把数据显示在TextView中
            cityList = record.getList();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (ois!=null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //保存数据
    public void save(){
        ObjectOutputStream fos=null;
        try {

            //如果文件不存在就创建文件
            File file=new File(path);
            if(!file.exists()){
                file.createNewFile();
            }

            //获取输出流
            //这里如果文件不存在会创建文件，这是写文件和读文件不同的地方
            fos=new ObjectOutputStream(new FileOutputStream(file));
            CityList list = new CityList(nickname, cityList);
            fos.writeObject(list);;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (fos!=null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}