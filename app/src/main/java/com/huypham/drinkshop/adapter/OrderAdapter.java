package com.huypham.drinkshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huypham.drinkshop.OrderDetailActivity;
import com.huypham.drinkshop.R;
import com.huypham.drinkshop.model.Order;
import com.huypham.drinkshop.utils.Common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    Context context;
    List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @NotNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View viewItem = LayoutInflater.from(context).inflate(R.layout.order_layout, parent, false);
        return new OrderViewHolder(viewItem);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OrderAdapter.OrderViewHolder holder, int position) {
        holder.txt_order_id.setText(new StringBuilder("#").append(orderList.get(position).getOrderId()));
        holder.txt_order_price.setText(new StringBuilder("$").append(orderList.get(position).getOrderPrice()));
        holder.txt_order_address.setText(orderList.get(position).getOrderAddress());
        holder.txt_order_comment.setText(orderList.get(position).getOrderComment());
        holder.txt_order_status.setText(new StringBuilder("Order Status : ").append(Common.convertCodeToStatus(orderList.get(position).getOrderStatus())));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.currentOrder = orderList.get(position);
                context.startActivity(new Intent(context, OrderDetailActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        public TextView txt_order_id, txt_order_price, txt_order_address, txt_order_comment, txt_order_status;

        public OrderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            txt_order_id = (TextView) itemView.findViewById(R.id.txt_order_id);
            txt_order_price = (TextView) itemView.findViewById(R.id.txt_order_price);
            txt_order_address = (TextView) itemView.findViewById(R.id.txt_order_address);
            txt_order_comment = (TextView) itemView.findViewById(R.id.txt_order_comment);
            txt_order_status = (TextView) itemView.findViewById(R.id.txt_order_status);
        }
    }
}
