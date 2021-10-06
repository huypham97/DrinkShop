package com.huypham.drinkshop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.huypham.drinkshop.R;
import com.huypham.drinkshop.database.modeldb.Cart;
import com.huypham.drinkshop.database.modeldb.Favorite;
import com.huypham.drinkshop.utils.Common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Context context;
    List<Cart> cartList;

    public CartAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @NotNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CartAdapter.CartViewHolder holder, int position) {
        String link = cartList.get(position).link;
        String name = cartList.get(position).name;
        int sugar = cartList.get(position).sugar;
        int ice = cartList.get(position).ice;
        double price = cartList.get(position).price;

        holder.setData(link, name, sugar, ice, price, position);
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public void removeItem(int position) {
        cartList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Cart item, int position) {
        cartList.add(position, item);
        notifyItemInserted(position);
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView img_product;
        TextView txt_product_name, txt_sugar_ice, txt_price;
        ElegantNumberButton txt_amount;

        public RelativeLayout view_background;
        public LinearLayout view_foreground;

        public CartViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_product = (ImageView) itemView.findViewById(R.id.img_product);
            txt_product_name = (TextView) itemView.findViewById(R.id.txt_product_name);
            txt_sugar_ice = (TextView) itemView.findViewById(R.id.txt_sugar_ice);
            txt_price = (TextView) itemView.findViewById(R.id.txt_price);
            txt_amount = (ElegantNumberButton) itemView.findViewById(R.id.txt_amount);

            view_background = (RelativeLayout) itemView.findViewById(R.id.view_background);
            view_foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);
        }

        public void setData(String link, String name, int sugar, int ice, double price, int position) {
            Glide.with(context).load(link).into(img_product);

            txt_product_name.setText(new StringBuilder(name).append(cartList.get(position).size == 0 ? " Size M" : " Size L")
                    .append(" x")
                    .append(cartList.get(position).amount).toString());
            txt_sugar_ice.setText(new StringBuilder("Sugar: ").append(sugar).append("%").append("\n")
                    .append("Ice: ").append(ice).append("%").toString());
            txt_price.setText(new StringBuilder("$").append(price));
            txt_amount.setNumber(String.valueOf(cartList.get(position).amount));

            // Get price of one cup with all options
            double priceOneCup = cartList.get(position).price / cartList.get(position).amount;

            // Auto save item when user change amount
            txt_amount.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                    Cart cart = cartList.get(position);
                    cart.amount = newValue;
                    cart.price = priceOneCup * newValue;

                    Common.cartRepository.updateCart(cart);

                    txt_price.setText(new StringBuilder("$").append(cart.price));
                }
            });
        }
    }
}
