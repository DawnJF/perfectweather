package com.dawnjf.fei.perfectweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.dawnjf.fei.perfectweather.db.MyCity;

import java.util.ArrayList;
import java.util.List;

public class SetCityActivity extends AppCompatActivity {

    private Button mDoneButton;

    private ImageButton mAddButton;

    private ListView mCityList;

    private List<MyCity> mMyCities = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_city);

        mDoneButton = (Button) findViewById(R.id.done);
        mAddButton = (ImageButton) findViewById(R.id.city_add);
        mCityList = (ListView) findViewById(R.id.list_edit_city);


    }
}
