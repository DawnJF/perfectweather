package com.dawnjf.fei.perfectweather.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dawnjf.fei.perfectweather.R;
import com.dawnjf.fei.perfectweather.db.MyCity;

import java.util.Collections;
import java.util.List;

/**
 * Created by fei on 2017/3/9.
 */

public class MyCityAdapter extends RecyclerView.Adapter<MyCityAdapter.ViewHolder>
        implements ItemTouchHelperAdapter{

    private List<MyCity> mCityList;

    @Override
    public void onItemMove(int formPosition, int toPosition) {
        Collections.swap(mCityList, formPosition, toPosition);
        notifyItemMoved(formPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mCityList.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mCityName;
        CardView mCardView;

        public ViewHolder(View itemView) {
            super(itemView);
            mCityName = (TextView) itemView.findViewById(R.id.my_city_name);
            mCardView = (CardView) itemView;
            mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
//                    if (Build.VERSION.SDK_INT >= 21 )
//                        view.setElevation(4);
                    return false;
                }
            });
        }
    }

    public MyCityAdapter(List<MyCity> cityList) {
        mCityList = cityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_city_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MyCity city = mCityList.get(position);
        holder.mCityName.setText(city.getCityName());
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }


}
