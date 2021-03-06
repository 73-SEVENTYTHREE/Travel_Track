package com.example.travel_track;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CityListActivity extends AppCompatActivity {
    private String nickname;
    private List<String> cityList = new ArrayList<String>();
    String path;
    FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + nickname + ".txt";
        read();
        Log.e("TAG", cityList.toString());
        showList();

        add = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????????????????
                AlertDialog.Builder builder = new AlertDialog.Builder(CityListActivity.this);
                builder.setTitle("????????????");//????????????
                View view = LayoutInflater.from(CityListActivity.this).inflate(R.layout.add_city,null);//??????????????????

                builder.setView(view);//????????????????????????
                builder.setPositiveButton("??????", (dialogInterface, i) -> {
                    //???????????????????????????
                    EditText input = (EditText) view.findViewById(R.id.addCityName);
                    String name = String.valueOf(input.getText());
                    Log.e("TAG", name);
                    if(name.equals("")){
                        Toast.makeText(CityListActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        GeocodeSearch geocodeSearch = new GeocodeSearch(CityListActivity.this);
                        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                            @Override
                            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

                            }

                            @Override
                            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                                if(i == 1000){
                                    if(cityList.contains(name)){
                                        Toast.makeText(CityListActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        if(!geocodeResult.getGeocodeAddressList().toString().equals("[]")){
                                            cityList.add(name);
                                            Log.e("TAG", cityList.toString() + geocodeResult.getGeocodeAddressList().toString());
                                            save();
                                            showList();
                                        }
                                        else{
                                            Toast.makeText(CityListActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                else{
                                    Toast.makeText(CityListActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        GeocodeQuery query = new GeocodeQuery(name, null);
                        geocodeSearch.getFromLocationNameAsyn(query);
                    }
                });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });
    }

    public void showList(){
        read();
        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.item, cityList);//listdata???str??????
        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                cityList.remove(position);
                save();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CityListActivity.this,R.layout.item, cityList);//listdata???str??????
                listView.setAdapter(arrayAdapter);
                return true;
            }
        });
    }

    //????????????
    public void read(){
        ObjectInputStream ois=null;
        try {
            Log.e("TAG", new File(path).getAbsolutePath()+"<---");
            //???????????????
            ois=new ObjectInputStream(new FileInputStream(new File(path)));
            //????????????????????????
            CityList record = (CityList) ois.readObject();
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

    //????????????
    public void save(){
        ObjectOutputStream fos=null;
        try {

            //????????????????????????????????????
            File file=new File(path);
            if(!file.exists()){
                file.createNewFile();
            }
            //???????????????
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