package com.huypham.drinkshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.huypham.drinkshop.adapter.FavoriteAdapter;
import com.huypham.drinkshop.database.modeldb.Favorite;
import com.huypham.drinkshop.utils.Common;
import com.huypham.drinkshop.utils.RecyclerItemTouchHelper;
import com.huypham.drinkshop.utils.RecyclerItemTouchHelperListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private RecyclerView recycler_fav;
    private RelativeLayout rootLayout;

    private CompositeDisposable compositeDisposable;
    private FavoriteAdapter favoriteAdapter;

    private List<Favorite> localFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        compositeDisposable = new CompositeDisposable();

        recycler_fav = (RecyclerView) findViewById(R.id.recycler_fav);
        recycler_fav.setLayoutManager(new LinearLayoutManager(this));
        recycler_fav.setHasFixedSize(true);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recycler_fav);

        loadFavItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();

        compositeDisposable.clear();
    }

    private void loadFavItems() {
        compositeDisposable.add(
                Common.favoriteRepository.getFavItems()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<List<Favorite>>() {
                            @Override
                            public void accept(List<Favorite> favorites) throws Throwable {
                                displayFavItems(favorites);
                            }
                        })
        );
    }

    private void displayFavItems(List<Favorite> favorites) {
        localFavorites = favorites;
        favoriteAdapter = new FavoriteAdapter(this, favorites);
        recycler_fav.setAdapter(favoriteAdapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoriteAdapter.FavoriteViewHolder) {
            String name = localFavorites.get(viewHolder.getAdapterPosition()).name;

            Favorite deletedItem = localFavorites.get(viewHolder.getAdapterPosition());
            int deletedIndex = viewHolder.getAdapterPosition();

            // Delete item from adapter
            favoriteAdapter.removeItem(deletedIndex);
            // Delete item from Room database
            Common.favoriteRepository.delete(deletedItem);

            Snackbar snackbar = Snackbar.make(rootLayout
                    , new StringBuilder(name).append(" removed from Favorites List").toString()
                    , Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favoriteAdapter.restoreItem(deletedItem, deletedIndex);
                    Common.favoriteRepository.insertFav(deletedItem);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}