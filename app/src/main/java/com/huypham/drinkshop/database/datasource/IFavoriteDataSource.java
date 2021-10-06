package com.huypham.drinkshop.database.datasource;

import com.huypham.drinkshop.database.modeldb.Favorite;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public interface IFavoriteDataSource {
    Flowable<List<Favorite>> getFavItems();
    int isFavorite(int itemId);
    void insertFav(Favorite... favorites);
    void delete(Favorite favorite);
}
