package com.dawnjf.fei.perfectweather;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dawnjf.fei.perfectweather.adapter.ItemTouchHelperCallback;
import com.dawnjf.fei.perfectweather.adapter.MyCityAdapter;
import com.dawnjf.fei.perfectweather.db.MyCity;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class EditCityActivity extends AppCompatActivity {

    private static final String TAG = "wwwww";


    private Toolbar mToolbar;

    private RecyclerView mCityList;

    private List<MyCity> mMyCities = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_city);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_edit);
        mCityList = (RecyclerView) findViewById(R.id.list_edit_city);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        mMyCities = DataSupport.order("showId").find(MyCity.class);
        Log.i(TAG, "onCreate: " + mMyCities.size());
        MyCityAdapter adapter = new MyCityAdapter(mMyCities);
        mCityList.setLayoutManager(new LinearLayoutManager(this));
        mCityList.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mCityList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_done:
                updateDB();
                Log.i(TAG, "onOptionsItemSelected: " + DataSupport.count(MyCity.class));
                setResult(RESULT_OK);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }

    private void updateDB() {
        DataSupport.deleteAll(MyCity.class);
        for (int i = 0; i < mMyCities.size(); i++) {
            MyCity oldCity = mMyCities.get(i);
            MyCity newCity = new MyCity();
            newCity.setShowId(i);
            newCity.setCityName(oldCity.getCityName());
            newCity.setWeatherId(oldCity.getWeatherId());
            newCity.save();
        }
    }
}
