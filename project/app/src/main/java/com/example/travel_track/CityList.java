package com.example.travel_track;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class CityList implements Serializable {
    //定义基本信息
    private String name;
    private List<String> cities;
    //无参构造方法
    public CityList() {
        super();
    }
    //有参构造方法，方便数据写入
    public CityList(String name, List<String> cities) {
        super();
        this.cities = cities;
    }
    public List<String> getList(){
        return this.cities;
    }
    //重写toString方法，方便显示
    @NonNull
    @Override
    public String toString() {
        return "People [name=" + name + "]";
    }
}
