package com.huypham.drinkshop.database.local;

import android.content.Context;

import com.huypham.drinkshop.database.modeldb.Cart;
import com.huypham.drinkshop.database.modeldb.Favorite;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Cart.class, Favorite.class}, version = 1)
public abstract class DrinkShopRoomDatabase extends RoomDatabase {

    public abstract CartDAO cartDAO();
    public abstract FavoriteDAO favoriteDAO();
    private static DrinkShopRoomDatabase instance;

    public static DrinkShopRoomDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, DrinkShopRoomDatabase.class, "DrinkShopDB")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
