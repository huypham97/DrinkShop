package com.huypham.drinkshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import android.os.Bundle;
import android.widget.TextView;

import com.huypham.drinkshop.adapter.DrinkAdapter;
import com.huypham.drinkshop.model.Drink;
import com.huypham.drinkshop.retrofit.IDrinkShopAPI;
import com.huypham.drinkshop.utils.Common;

import java.util.List;

public class DrinkActivity extends AppCompatActivity {

    IDrinkShopAPI mService;

    RecyclerView listDrink;
    TextView txt_menu_name;
    SwipeRefreshLayout swipeRefreshLayout;

    DrinkAdapter drinkAdapter;

    // Rxjava
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        mService = Common.getAPI();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);

        // Get Drink
        listDrink = (RecyclerView) findViewById(R.id.recycler_drinks);
        listDrink.setLayoutManager(new GridLayoutManager(this, 2));
        listDrink.setHasFixedSize(true);

        txt_menu_name = (TextView) findViewById(R.id.txt_menu_name);
        txt_menu_name.setText(Common.currentCategory.getName());

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                loadListDrink(Common.currentCategory.getID());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                loadListDrink(Common.currentCategory.getID());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.dispose();
    }

    private void loadListDrink(String menuId) {
        compositeDisposable.add(mService.getDrink(menuId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<Drink>>() {
            @Override
            public void accept(List<Drink> drinks) throws Throwable {
                displayDrinkList(drinks);
            }
        }));
    }

    private void displayDrinkList(List<Drink> drinks) {
        swipeRefreshLayout.setRefreshing(false);

        drinkAdapter = new DrinkAdapter(this, drinks);
        listDrink.setAdapter(drinkAdapter);
        drinkAdapter.notifyDataSetChanged();
    }
}