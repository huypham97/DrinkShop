package com.huypham.drinkshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.huypham.drinkshop.DrinkActivity;
import com.huypham.drinkshop.R;
import com.huypham.drinkshop.model.Category;
import com.huypham.drinkshop.utils.Common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    Context context;
    List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @NotNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_layout, null);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CategoryAdapter.CategoryViewHolder holder, int position) {
        String link = categoryList.get(position).getLink();
        String name = categoryList.get(position).getName();
        holder.setData(link, name, position);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView img_product;
        TextView txt_menu_name;

        public CategoryViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_product = (ImageView) itemView.findViewById(R.id.img_product);
            txt_menu_name = (TextView) itemView.findViewById(R.id.txt_menu_name);
        }

        public void setData(String link, String name, int position) {
            Glide.with(context)
                    .load(link)
                    .into(img_product);
            txt_menu_name.setText(name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Common.currentCategory = categoryList.get(position);

                    // Start new Activity
                    context.startActivity(new Intent(context, DrinkActivity.class));
                }
            });
        }
    }
}
