package com.huypham.drinkshop.database.local;

import com.huypham.drinkshop.database.modeldb.Favorite;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface FavoriteDAO {
    @Query("SELECT * FROM Favorite")
    Flowable<List<Favorite>> getFavItems();

    @Query("SELECT EXISTS (SELECT 1 FROM Favorite WHERE id = :itemId)")
    int isFavorite(int itemId);

    @Insert
    void insertFav(Favorite... favorites);

    // delete single word
    @Delete
    void delete(Favorite favorite);
}
