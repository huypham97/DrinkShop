package com.huypham.drinkshop.utils;

import com.huypham.drinkshop.database.datasource.CartRepository;
import com.huypham.drinkshop.database.datasource.FavoriteRepository;
import com.huypham.drinkshop.database.local.DrinkShopRoomDatabase;
import com.huypham.drinkshop.model.Category;
import com.huypham.drinkshop.model.Drink;
import com.huypham.drinkshop.model.Order;
import com.huypham.drinkshop.model.User;
import com.huypham.drinkshop.retrofit.IDrinkShopAPI;
import com.huypham.drinkshop.retrofit.RetrofitClient;
import com.huypham.drinkshop.retrofit.RetrofitScalarsClient;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static final String BASE_URL = "http://192.168.0.4/drinkshop/";
    public static final String API_GET_TOKEN = "http://192.168.0.4/drinkshop/braintree/main.php";
    public static final String TOPPING_MENU_ID = "7";

    public static User currentUser = null;
    public static Category currentCategory = null;
    public static Order currentOrder = null;

    public static List<Drink> toppingList = new ArrayList<>();
    public static List<String> toppingAdded = new ArrayList<>();

    public static double toppingPrice = 0;

    //Hold field
    public static int sizeOfCup = -1;   // -1: no cho0se (error) , 0 : M , 1 : L
    public static int sugar = -1;   // -1 : no choose (error)
    public static int ice = -1;

    // Database
    public static DrinkShopRoomDatabase drinkShopRoomDatabase;
    public static CartRepository cartRepository;
    public static FavoriteRepository favoriteRepository;

    public static IDrinkShopAPI getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopAPI.class);
    }

    public static IDrinkShopAPI getScalarsAPI() {
        return RetrofitScalarsClient.getScalarsClient(BASE_URL).create(IDrinkShopAPI.class);
    }

    public static String convertCodeToStatus(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Processing";
            case 2:
                return "Shipping";
            case 3:
                return "Shipped";
            case -1:
                return "Canceled";
            default:
                return "Order Error";
        }
    }
}
