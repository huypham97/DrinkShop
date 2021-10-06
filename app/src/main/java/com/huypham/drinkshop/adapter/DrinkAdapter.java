package com.huypham.drinkshop.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.huypham.drinkshop.R;
import com.huypham.drinkshop.database.modeldb.Cart;
import com.huypham.drinkshop.database.modeldb.Favorite;
import com.huypham.drinkshop.model.Drink;
import com.huypham.drinkshop.utils.Common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {

    private Context context;
    private List<Drink> drinkList;

    public DrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
    }

    @NonNull
    @NotNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.drink_item_layout, null);
        return new DrinkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DrinkAdapter.DrinkViewHolder holder, int position) {
        String link = drinkList.get(position).getLink();
        String name = drinkList.get(position).getName();
        String price = drinkList.get(position).getPrice();
        holder.setData(link, name, price, position);
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public class DrinkViewHolder extends RecyclerView.ViewHolder {

        ImageView img_product;
        TextView txt_drink_name, txt_price;
        ImageView img_add_cart, img_favorite;

        public DrinkViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_product = (ImageView) itemView.findViewById(R.id.img_product);
            txt_drink_name = (TextView) itemView.findViewById(R.id.txt_drink_name);
            txt_price = (TextView) itemView.findViewById(R.id.txt_drink_price);
            img_add_cart = (ImageView) itemView.findViewById(R.id.img_add_cart);
            img_favorite = (ImageView) itemView.findViewById(R.id.img_favorite);
        }

        public void setData(String link, String name, String price, int position) {
            Glide.with(context)
                    .load(link)
                    .into(img_product);
            txt_drink_name.setText(name);
            txt_price.setText("$" + price);

            img_add_cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddToCartDialog(position);
                }
            });

            // Favorite System
            if (Common.favoriteRepository.isFavorite(Integer.parseInt(drinkList.get(position).getID())) == 1) {
                img_favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
            } else {
                img_favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            }

            img_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Common.favoriteRepository.isFavorite(Integer.parseInt(drinkList.get(position).getID())) != 1) {
                        addOrRemoveFavorite(drinkList.get(position), true);
                        img_favorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    } else {
                        addOrRemoveFavorite(drinkList.get(position), false);
                        img_favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                }
            });
        }

        private void addOrRemoveFavorite(Drink drink, boolean isAdd) {
            Favorite favorite = new Favorite();
            favorite.id = drink.getID();
            favorite.link = drink.getLink();
            favorite.name = drink.getName();
            favorite.price = drink.getPrice();
            favorite.menuId = drink.getMenuId();

            if (isAdd) {
                Common.favoriteRepository.insertFav(favorite);
            } else {
                Common.favoriteRepository.delete(favorite);
            }
        }

        private void showAddToCartDialog(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View itemView = LayoutInflater.from(context).inflate(R.layout.add_to_cart_layout, null);

            // View
            ImageView img_product_dialog = (ImageView) itemView.findViewById(R.id.img_cart_product);
            ElegantNumberButton txt_count = (ElegantNumberButton) itemView.findViewById(R.id.txt_count);
            TextView txt_product_dialog = (TextView) itemView.findViewById(R.id.txt_cart_product_name);
            EditText edt_commend = (EditText) itemView.findViewById(R.id.edt_comment);

            RadioButton rdi_sizeM = (RadioButton) itemView.findViewById(R.id.rdi_sizeM);
            RadioButton rdi_sizeL = (RadioButton) itemView.findViewById(R.id.rdi_sizeL);

            rdi_sizeM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sizeOfCup = 0;
                    }
                }
            });

            rdi_sizeL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sizeOfCup = 1;
                    }
                }
            });

            RadioButton rdi_sugar_100 = (RadioButton) itemView.findViewById(R.id.rdi_sugar_100);
            RadioButton rdi_sugar_70 = (RadioButton) itemView.findViewById(R.id.rdi_sugar_70);
            RadioButton rdi_sugar_50 = (RadioButton) itemView.findViewById(R.id.rdi_sugar_50);
            RadioButton rdi_sugar_30 = (RadioButton) itemView.findViewById(R.id.rdi_sugar_30);
            RadioButton rdi_sugar_free = (RadioButton) itemView.findViewById(R.id.rdi_sugar_free);

            rdi_sugar_30.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sugar = 30;
                    }
                }
            });

            rdi_sugar_50.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sugar = 50;
                    }
                }
            });

            rdi_sugar_70.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sugar = 70;
                    }
                }
            });

            rdi_sugar_100.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sugar = 100;
                    }
                }
            });

            rdi_sugar_free.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.sugar = 0;
                    }
                }
            });

            RadioButton rdi_ice_100 = (RadioButton) itemView.findViewById(R.id.rdi_ice_100);
            RadioButton rdi_ice_70 = (RadioButton) itemView.findViewById(R.id.rdi_ice_70);
            RadioButton rdi_ice_50 = (RadioButton) itemView.findViewById(R.id.rdi_ice_50);
            RadioButton rdi_ice_30 = (RadioButton) itemView.findViewById(R.id.rdi_ice_30);
            RadioButton rdi_ice_free = (RadioButton) itemView.findViewById(R.id.rdi_ice_free);

            rdi_ice_free.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.ice = 0;
                    }
                }
            });

            rdi_ice_30.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.ice = 30;
                    }
                }
            });

            rdi_ice_50.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.ice = 50;
                    }
                }
            });

            rdi_ice_70.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.ice = 70;
                    }
                }
            });

            rdi_ice_100.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Common.ice = 100;
                    }
                }
            });

            RecyclerView recycler_topping = (RecyclerView) itemView.findViewById(R.id.recycler_topping);
            recycler_topping.setLayoutManager(new LinearLayoutManager(context));
            recycler_topping.setHasFixedSize(true);

            MultiChoiceAdapter adapter = new MultiChoiceAdapter(context, Common.toppingList);
            recycler_topping.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            // Set data
            Glide.with(context)
                    .load(drinkList.get(position).getLink())
                    .into(img_product_dialog);
            txt_product_dialog.setText(drinkList.get(position).getName());

            builder.setView(itemView);
            builder.setNegativeButton("ADD TO CART", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Common.sizeOfCup == -1) {
                        Toast.makeText(context, "Please choose size of cup", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Common.sugar == -1) {
                        Toast.makeText(context, "Please choose sugar", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (Common.ice == -1) {
                        Toast.makeText(context, "Please choose ice", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showConfirmDialog(position, txt_count.getNumber());
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        private void showConfirmDialog(int position, String number) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View itemView = LayoutInflater.from(context).inflate(R.layout.confirm_add_to_cart_layout, null);

            // View
            ImageView img_product_dialog = (ImageView) itemView.findViewById(R.id.img_product);
            TextView txt_product_dialog = (TextView) itemView.findViewById(R.id.txt_cart_product_name);
            TextView txt_product_price = (TextView) itemView.findViewById(R.id.txt_cart_product_price);
            TextView txt_sugar = (TextView) itemView.findViewById(R.id.txt_cart_product_sugar);
            TextView txt_ice = (TextView) itemView.findViewById(R.id.txt_cart_product_ice);
            TextView txt_topping_extra = (TextView) itemView.findViewById(R.id.txt_topping_extra);

            // Set data
            Glide.with(context).load(drinkList.get(position).getLink()).into(img_product_dialog);
            txt_product_dialog.setText(new StringBuffer(drinkList.get(position).getName()).append(Common.sizeOfCup == 0 ? " Size M" : " Size L")
                    .append(" x")
                    .append(number).toString());
            txt_ice.setText(new StringBuffer("Ice: ").append(Common.ice).append("%").toString());
            txt_sugar.setText(new StringBuffer("Sugar: ").append(Common.sugar).append("%").toString());

            double price = (Double.parseDouble(drinkList.get(position).getPrice()) * Double.parseDouble(number)) + Common.toppingPrice;

            if (Common.sizeOfCup == 1)  // Size L
                price += Double.parseDouble(number) * 3.0;

            txt_product_price.setText(new StringBuffer("$").append(price));

            StringBuilder topping_final_comment = new StringBuilder("");
            for (String line : Common.toppingAdded) {
                topping_final_comment.append(line).append("\n");
            }
            topping_final_comment.deleteCharAt(topping_final_comment.lastIndexOf("\n"));

            txt_topping_extra.setText(topping_final_comment);

            double finalPrice = price;
            builder.setNegativeButton("CONFIRM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    try {
                        // Add to SQLite
                        // Create new Cart Item
                        Cart cartItem = new Cart();
                        cartItem.name = drinkList.get(position).getName();
                        cartItem.link = drinkList.get(position).getLink();
                        cartItem.amount = Integer.parseInt(number);
                        cartItem.ice = Common.ice;
                        cartItem.sugar = Common.sugar;
                        cartItem.price = finalPrice;
                        cartItem.size = Common.sizeOfCup;
                        cartItem.toppingExtras = txt_topping_extra.getText().toString();

                        // Add to DB
                        Common.cartRepository.insertToCart(cartItem);

                        Log.d("DEBUG", new Gson().toJson(cartItem));
                        Toast.makeText(context, "Save item to cart success", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setView(itemView);
            builder.show();
        }
    }
}
