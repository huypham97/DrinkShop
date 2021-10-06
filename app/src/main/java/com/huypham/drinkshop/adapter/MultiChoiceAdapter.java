package com.huypham.drinkshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.huypham.drinkshop.R;
import com.huypham.drinkshop.model.Drink;
import com.huypham.drinkshop.utils.Common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MultiChoiceAdapter extends RecyclerView.Adapter<MultiChoiceAdapter.MultiChoiceViewHolder> {

    Context context;
    List<Drink> optionList;

    public MultiChoiceAdapter(Context context, List<Drink> optionList) {
        this.context = context;
        this.optionList = optionList;
    }

    @NonNull
    @NotNull
    @Override
    public MultiChoiceViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.multi_check_layout, null);
        return new MultiChoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MultiChoiceAdapter.MultiChoiceViewHolder holder, int position) {
        String name = optionList.get(position).getName();
        holder.setData(name, position);
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    public class MultiChoiceViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public MultiChoiceViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.ckb_topping);
        }

        public void setData(String name, int position) {
            checkBox.setText(name);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.toppingAdded.add(buttonView.getText().toString());
                        Common.toppingPrice += Double.parseDouble(optionList.get(position).getPrice());
                    } else {
                        Common.toppingAdded.remove(buttonView.getText().toString());
                        Common.toppingPrice -= Double.parseDouble(optionList.get(position).getPrice());
                    }
                }
            });
        }
    }
}
