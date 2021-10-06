package com.huypham.drinkshop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.huypham.drinkshop.adapter.CartAdapter;
import com.huypham.drinkshop.adapter.FavoriteAdapter;
import com.huypham.drinkshop.database.modeldb.Cart;
import com.huypham.drinkshop.database.modeldb.Favorite;
import com.huypham.drinkshop.retrofit.IDrinkShopAPI;
import com.huypham.drinkshop.utils.Common;
import com.huypham.drinkshop.utils.RecyclerItemTouchHelper;
import com.huypham.drinkshop.utils.RecyclerItemTouchHelperListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    private static final int PAYMENT_REQUEST_CODE = 1234;

    private RecyclerView recycler_cart;
    private Button btn_place_order;
    private RelativeLayout rootLayout;

    private CompositeDisposable compositeDisposable;
    private CartAdapter cartAdapter;

    private List<Cart> localCarts = new ArrayList<>();

    IDrinkShopAPI mService;
    IDrinkShopAPI mServiceScalars;

    // Global Strings
    String token, amount, orderAddress, orderComment;
    HashMap<String, String> paramsHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        compositeDisposable = new CompositeDisposable();

        mService = Common.getAPI();
        mServiceScalars = Common.getScalarsAPI();

        recycler_cart = (RecyclerView) findViewById(R.id.recycler_cart);
        recycler_cart.setLayoutManager(new LinearLayoutManager(this));
        recycler_cart.setHasFixedSize(true);

        btn_place_order = (Button) findViewById(R.id.btn_place_order);
        btn_place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recycler_cart);

        loadCartItems();

        loadToken();
    }

    private void loadToken() {
        new getToken().execute();
    }

    private class getToken extends AsyncTask {
        android.app.AlertDialog mDialog;

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(Common.API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(final String responseBody) {
                    mDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token = responseBody;
                            if (Common.cartRepository.sumPrice() > 0) {
                                btn_place_order.setEnabled(true);
                            } else {
                                btn_place_order.setEnabled(false);
                            }
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {
                    mDialog.dismiss();
                    Log.d("Err3", exception.toString());
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new SpotsDialog(CartActivity.this);
            mDialog.show();
            mDialog.setCancelable(false);
            mDialog.setMessage("Please wait...");
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

    private void placeOrder() {
        if (Common.currentUser != null) {
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Submit Order");

            View submit_order_layout = LayoutInflater.from(this).inflate(R.layout.submit_order_layout, null);

            EditText edt_comment = (EditText) submit_order_layout.findViewById(R.id.edt_comment);
            EditText edt_other_address = (EditText) submit_order_layout.findViewById(R.id.edt_other_address);

            RadioButton rdi_user_address = (RadioButton) submit_order_layout.findViewById(R.id.rdi_user_address);
            RadioButton rdi_other_address = (RadioButton) submit_order_layout.findViewById(R.id.rdi_other_address);

            RadioButton rdi_credit_card = (RadioButton) submit_order_layout.findViewById(R.id.rdi_credit_card);
            RadioButton rdi_cod = (RadioButton) submit_order_layout.findViewById(R.id.rdi_cod);

            // Event
            rdi_user_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        edt_other_address.setEnabled(false);
                }
            });

            rdi_other_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        edt_other_address.setEnabled(true);
                }
            });

            builder.setView(submit_order_layout)
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (rdi_credit_card.isChecked()) {

                                orderComment = edt_comment.getText().toString();
                                if (rdi_user_address.isChecked()) {
                                    orderAddress = Common.currentUser.getAddress();
                                } else if (rdi_other_address.isChecked()) {
                                    orderAddress = edt_other_address.getText().toString();
                                } else {
                                    orderAddress = "";
                                }

                                // Payment
                                DropInRequest dropInRequest = new DropInRequest().clientToken(token);
                                startActivityForResult(dropInRequest.getIntent(CartActivity.this), PAYMENT_REQUEST_CODE);
                            } else if (rdi_cod.isChecked()) {
                                orderComment = edt_comment.getText().toString();
                                if (rdi_user_address.isChecked()) {
                                    orderAddress = Common.currentUser.getAddress();
                                } else if (rdi_other_address.isChecked()) {
                                    orderAddress = edt_other_address.getText().toString();
                                } else {
                                    orderAddress = "";
                                }

                                // Submit Order
                                compositeDisposable.add(
                                        Common.cartRepository.getCartItems()
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(new Consumer<List<Cart>>() {
                                                    @Override
                                                    public void accept(List<Cart> carts) throws Throwable {
                                                        if (!TextUtils.isEmpty(orderAddress)) {
                                                            sendOrderToServer(Common.cartRepository.sumPrice(),
                                                                    carts,
                                                                    orderComment,
                                                                    orderAddress,
                                                                    "COD");
                                                        } else {
                                                            Toast.makeText(CartActivity.this, "Order Address can't null", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                );
                            }
                        }
                    })
                    .show();
        } else {
            // Required Login
            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
            builder.setTitle("NOT LOGIN?");
            builder.setMessage("Please login or register account to submit order!");
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(CartActivity.this, MainActivity.class));
                    finish();
                }
            }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNounce = nonce.getNonce();
                Log.d("pay", result + " " + strNounce);

                if (Common.cartRepository.sumPrice() > 0) {
                    amount = String.valueOf(Common.cartRepository.sumPrice());
                    paramsHash = new HashMap<>();

                    paramsHash.put("amount", amount);
                    paramsHash.put("nonce", strNounce);

                    sendPayments();
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("Err1", error.toString());
            }
        }
    }

    private void sendPayments() {
        mServiceScalars.payment(paramsHash.get("nonce"), paramsHash.get("amount"))
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.body().toString().contains("Successful")) {
                            Toast.makeText(CartActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();

                            // Submit Order
                            compositeDisposable.add(
                                    Common.cartRepository.getCartItems()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new Consumer<List<Cart>>() {
                                                @Override
                                                public void accept(List<Cart> carts) throws Throwable {
                                                    if (!TextUtils.isEmpty(orderAddress)) {
                                                        sendOrderToServer(Common.cartRepository.sumPrice(),
                                                                carts,
                                                                orderComment,
                                                                orderAddress,
                                                                "Braintree");
                                                    } else {
                                                        Toast.makeText(CartActivity.this, "Order Address can't null", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                            );
                        } else {
                            Toast.makeText(CartActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                            Log.d("Err", "Payment Failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("Err2", t.getMessage());
                    }
                });
    }

    private void sendOrderToServer(float sumPrice, List<Cart> carts, String orderComment, String orderAddress, String paymentMethod) {
        if (carts.size() > 0) {
            String orderDetail = new Gson().toJson(carts);

            mService.submitOrder(sumPrice, orderDetail, orderComment, orderAddress, Common.currentUser.getPhone(), paymentMethod)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Toast.makeText(CartActivity.this, "Order submit", Toast.LENGTH_SHORT).show();

                            // Clear Cart
                            Common.cartRepository.emptyCart();
                            finish();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
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

    private void loadCartItems() {
        compositeDisposable.add(
                Common.cartRepository.getCartItems()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<List<Cart>>() {
                            @Override
                            public void accept(List<Cart> carts) throws Throwable {
                                localCarts = carts;
                                displayCartItems(carts);
                            }
                        })
        );
    }

    private void displayCartItems(List<Cart> carts) {
        cartAdapter = new CartAdapter(this, carts);
        recycler_cart.setAdapter(cartAdapter);
//        cartAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartAdapter.CartViewHolder) {
            String name = localCarts.get(viewHolder.getAdapterPosition()).name;

            Cart deletedItem = localCarts.get(viewHolder.getAdapterPosition());
            int deletedIndex = viewHolder.getAdapterPosition();

            // Delete item from adapter
            cartAdapter.removeItem(deletedIndex);
            // Delete item from Room database
            Common.cartRepository.deleteCartItem(deletedItem);

            Snackbar snackbar = Snackbar.make(rootLayout
                    , new StringBuilder(name).append(" removed from Favorites List").toString()
                    , Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartAdapter.restoreItem(deletedItem, deletedIndex);
                    Common.cartRepository.insertToCart(deletedItem);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}