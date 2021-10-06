package com.huypham.drinkshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huypham.drinkshop.R;
import com.huypham.drinkshop.model.Banner;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private Context context;
    private List<Banner> bannerList = new ArrayList<>();

    public SliderAdapter(Context context, List<Banner> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    public void addItem(List<Banner> bannerList) {
        this.bannerList.clear();
        this.bannerList = bannerList;
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
        Banner sliderItem = bannerList.get(position);

        Glide.with(viewHolder.itemView)
                .load(sliderItem.getLink())
                .into(viewHolder.imageViewSlider);
    }

    @Override
    public int getCount() {
        return bannerList.size();
    }

    public class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        ImageView imageViewSlider;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewSlider = (ImageView) itemView.findViewById(R.id.iv_slider);
        }
    }
}
