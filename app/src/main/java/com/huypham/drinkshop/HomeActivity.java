package com.huypham.drinkshop;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import edmt.dev.afilechooser.utils.FileUtils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.huypham.drinkshop.adapter.CategoryAdapter;
import com.huypham.drinkshop.adapter.SliderAdapter;
import com.huypham.drinkshop.database.datasource.CartRepository;
import com.huypham.drinkshop.database.datasource.FavoriteRepository;
import com.huypham.drinkshop.database.local.CartDataSource;
import com.huypham.drinkshop.database.local.DrinkShopRoomDatabase;
import com.huypham.drinkshop.database.local.FavoriteDataSource;
import com.huypham.drinkshop.model.Banner;
import com.huypham.drinkshop.model.Category;
import com.huypham.drinkshop.model.CheckUserResponse;
import com.huypham.drinkshop.model.Drink;
import com.huypham.drinkshop.model.User;
import com.huypham.drinkshop.retrofit.IDrinkShopAPI;
import com.huypham.drinkshop.utils.Common;
import com.huypham.drinkshop.utils.ProgressRequestBody;
import com.huypham.drinkshop.utils.UploadCallback;
import com.nex3z.notificationbadge.NotificationBadge;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UploadCallback {

    private static final int REQUEST_CHOOSER = 1234;

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private TextView txt_name, txt_phone;
    private SliderView sliderView;
    private RecyclerView listMenu;
    private NotificationBadge badge;
    private ImageView cartIcon;
    private CircleImageView img_avatar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Banner> bannerList;

    private SliderAdapter sliderAdapter;
    private CategoryAdapter categoryAdapter;

    private IDrinkShopAPI mService;
    private Uri selectFileUri;

    private FirebaseAuth firebaseAuth;

    // Rxjava
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getAPI();

        firebaseAuth = FirebaseAuth.getInstance();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_to_refresh);

        listMenu = (RecyclerView) findViewById(R.id.list_menu);
        listMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listMenu.setHasFixedSize(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        txt_name = (TextView) headerView.findViewById(R.id.txt_name);
        txt_phone = (TextView) headerView.findViewById(R.id.txt_phone);
        img_avatar = (CircleImageView) headerView.findViewById(R.id.img_avatar);

        checkUserLogin();   // If user already logged, just login again

        // Get banner
        sliderView = (SliderView) findViewById(R.id.imageSlider);
        bannerList = new ArrayList<>();
        sliderAdapter = new SliderAdapter(this, bannerList);
        sliderView.setSliderAdapter(sliderAdapter);

        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.startAutoCycle();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // Get banner
                getBannerImage();

                // Get Menu
                getMenu();

                // Save newest Topping List
                getToppingList();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                // Get banner
                getBannerImage();

                // Get Menu
                getMenu();

                // Save newest Topping List
                getToppingList();
            }
        });

        // Init database
        initDB();
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void checkUserLogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User already auth
            android.app.AlertDialog alertDialog = new SpotsDialog(HomeActivity.this);
            alertDialog.show();
            alertDialog.setMessage("Please waiting...");

            mService.checkUserExists(user.getPhoneNumber())
                    .enqueue(new Callback<CheckUserResponse>() {
                        @Override
                        public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                            CheckUserResponse userResponse = response.body();
                            if (userResponse.isExists()) {
                                // Request Information of current User
                                mService.getUserInformation(user.getPhoneNumber()).enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {
                                        Common.currentUser = response.body();
                                        if (Common.currentUser != null) {
                                            alertDialog.dismiss();

                                            // Event
                                            img_avatar.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (Common.currentUser != null) {
                                                        chooseImage();
                                                    }
                                                }
                                            });

                                            if (Common.currentUser != null) {
                                                // Set Info
                                                txt_name.setText(Common.currentUser.getName());
                                                txt_phone.setText(Common.currentUser.getPhone());

                                                // Set avatar
                                                if (!TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
                                                    Glide.with(HomeActivity.this)
                                                            .load(new StringBuilder(Common.BASE_URL)
                                                                    .append("user_avatar/")
                                                                    .append(Common.currentUser.getAvatarUrl()).toString())
                                                            .into(img_avatar);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {
                                        alertDialog.dismiss();
                                        Log.d("ERROR", t.getMessage());
                                    }
                                });
                            } else {
                                // If user not exists on database, just make login
                                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<CheckUserResponse> call, Throwable t) {
                            alertDialog.dismiss();
                            Log.d("ERROR", t.getMessage());
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
        startActivityForResult(intent, REQUEST_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        selectFileUri = data.getData();
                        if (selectFileUri != null && !selectFileUri.getPath().isEmpty()) {
                            img_avatar.setImageURI(selectFileUri);
                            uploadFile();
                        }
                    } else {
                        Toast.makeText(this, "Can't upload file to server", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void uploadFile() {
        if (selectFileUri != null) {
            File file = FileUtils.getFile(this, selectFileUri);

            String fileName = new StringBuilder(Common.currentUser.getPhone())
                    .append(FileUtils.getExtension(file.toString()))
                    .toString();

            ProgressRequestBody requestBody = new ProgressRequestBody(file, this);

            MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody);
            MultipartBody.Part userPhone = MultipartBody.Part.createFormData("phone", Common.currentUser.getPhone());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadFile(userPhone, body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Toast.makeText(HomeActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void initDB() {
        Common.drinkShopRoomDatabase = DrinkShopRoomDatabase.getInstance(this);
        Common.cartRepository = CartRepository.getInstance(CartDataSource.getInstance(Common.drinkShopRoomDatabase.cartDAO()));
        Common.favoriteRepository = FavoriteRepository.getInstance(FavoriteDataSource.getInstance(Common.drinkShopRoomDatabase.favoriteDAO()));
    }

    private void getToppingList() {
        compositeDisposable.add(mService.getDrink(Common.TOPPING_MENU_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Drink>>() {
                    @Override
                    public void accept(List<Drink> drinks) throws Throwable {
                        Common.toppingList = drinks;
                    }
                }));
    }

    private void getMenu() {
        compositeDisposable.add(mService.getMenu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Category>>() {
                    @Override
                    public void accept(List<Category> categories) throws Throwable {
                        displayMenu(categories);
                    }
                }));
    }

    private void displayMenu(List<Category> categories) {
        categoryAdapter = new CategoryAdapter(this, categories);
        listMenu.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();

        swipeRefreshLayout.setRefreshing(false);
    }

    private void getBannerImage() {
        compositeDisposable.add(mService.getBanners()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Banner>>() {
                    @Override
                    public void accept(List<Banner> banners) throws Throwable {
                        sliderAdapter.addItem(banners);
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCartCount();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_bar, menu);
        View view = menu.findItem(R.id.cart_menu).getActionView();
        badge = (NotificationBadge) view.findViewById(R.id.badge);
        cartIcon = (ImageView) view.findViewById(R.id.cart_icon);
        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            }
        });

        updateCartCount();

        return true;
    }

    private void updateCartCount() {
        if (badge == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Common.cartRepository.countCartItems() == 0) {
                    badge.setVisibility(View.INVISIBLE);
                } else {
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(Common.cartRepository.countCartItems()));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.cart_menu) {
            return true;
        } else if (id == R.id.search_menu) {
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_sign_out:
                // Create confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Exit Application")
                        .setMessage("Do you want to exit DrinkShop?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();

                                // Clear all activity
                                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .create();
                builder.show();
                break;
            case R.id.nav_favorite:
                if (Common.currentUser != null) {
                    startActivity(new Intent(HomeActivity.this, FavoriteListActivity.class));
                } else {
                    Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_orders:
                if (Common.currentUser != null) {
                    startActivity(new Intent(HomeActivity.this, ShowOrderActivity.class));
                } else {
                    Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
                }
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}