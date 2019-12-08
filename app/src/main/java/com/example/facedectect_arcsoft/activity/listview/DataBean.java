package com.example.facedectect_arcsoft.activity.listview;

public class DataBean {


    private String name;

    public boolean isCheck;  //该属性主要标志CheckBox是否选中

    public DataBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
