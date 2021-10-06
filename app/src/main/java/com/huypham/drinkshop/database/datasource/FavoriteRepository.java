package com.huypham.drinkshop.database.datasource;

import com.huypham.drinkshop.database.modeldb.Favorite;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class FavoriteRepository implements IFavoriteDataSource {

    private IFavoriteDataSource iFavoriteDataSource;
    private static FavoriteRepository instance;

    public FavoriteRepository(IFavoriteDataSource iFavoriteDataSource) {
        this.iFavoriteDataSource = iFavoriteDataSource;
    }

    public static FavoriteRepository getInstance(IFavoriteDataSource iFavoriteDataSource) {
        if (instance == null) {
            instance = new FavoriteRepository(iFavoriteDataSource);
        }
        return instance;
    }

    @Override
    public Flowable<List<Favorite>> getFavItems() {
        return iFavoriteDataSource.getFavItems();
    }

    @Override
    public int isFavorite(int itemId) {
        return iFavoriteDataSource.isFavorite(itemId);
    }

    @Override
    public void insertFav(Favorite... favorites) {
        iFavoriteDataSource.insertFav(favorites);
    }

    @Override
    public void delete(Favorite favorite) {
        iFavoriteDataSource.delete(favorite);
    }
}
