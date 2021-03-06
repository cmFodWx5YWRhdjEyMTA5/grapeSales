package com.creatively.grapeSalesApp.grapeapplication.activity;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.creatively.grapeSalesApp.grapeapplication.R;
import com.creatively.grapeSalesApp.grapeapplication.adapter.OfferAdapter;
import com.creatively.grapeSalesApp.grapeapplication.callback.InstallCallback;
import com.creatively.grapeSalesApp.grapeapplication.callback.OnItemClickListener;
import com.creatively.grapeSalesApp.grapeapplication.manager.AppErrorsManager;
import com.creatively.grapeSalesApp.grapeapplication.manager.ConnectionManager;
import com.creatively.grapeSalesApp.grapeapplication.manager.FontManager;
import com.creatively.grapeSalesApp.grapeapplication.model.Offer;
import com.creatively.grapeSalesApp.grapeapplication.model.Shop;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Offer> offerList = new ArrayList<>();
    private Shop shop;
    private ProgressBar progressBar;
    private ConnectionManager connectionManager;
    private OfferAdapter shopAdapter;
    private ImageView progressbar;
    private TextView noTxt;
    private RecyclerView recyclerView;
    private EditText offerEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale locale = new Locale("ar");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_shop_details);
        connectionManager = new ConnectionManager(this);
        shop = (Shop) getIntent().getSerializableExtra("shop");
        init();
    }

    public void init() {
        RatingBar ratingStart = findViewById(R.id.rating);
        offerEditText = findViewById(R.id.offerEditText);
        offerEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    FontManager.hideKeyboard(ShopDetailsActivity.this);
                    getofferSearchList();
                    return true;
                }
                return false;
            }
        });
        progressbar = findViewById(R.id.waitProgress);
        noTxt = findViewById(R.id.noTxt);
        ObjectAnimator animation = ObjectAnimator.ofFloat(progressbar, "rotationY", 0.0f, 360f);
        animation.setDuration(1000);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();

        RoundedImageView shopImage = findViewById(R.id.shopImage);
        progressBar = findViewById(R.id.progressBar);
        TextView shopNameTxt = findViewById(R.id.shopNameTxt);
        TextView mobileTxt = findViewById(R.id.mobileTxt);
        shopNameTxt.setText(shop.getName());
        mobileTxt.setText(shop.getShop_phone());

        TextView shopAddressTxt = findViewById(R.id.shopAddressTxt);
        shopAddressTxt.setText(shop.getAddress());
        ratingStart.setRating(Float.parseFloat(shop.getRating()));
        progressBar.setVisibility(View.VISIBLE);
        Picasso.with(this).load(FontManager.IMAGE_URL + shop.getShopImage())
                .into(shopImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
        RelativeLayout addLayout = findViewById(R.id.addLayout);
        RelativeLayout backLayout = findViewById(R.id.backLayout);
        addLayout.setOnClickListener(this);
        backLayout.setOnClickListener(this);

        recyclerView = findViewById(R.id.recyclerView);
        shopAdapter = new OfferAdapter(this, offerList, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                int id = view.getId();
                if (id == R.id.layout) {
                    Intent intent = new Intent(ShopDetailsActivity.this, updateOfferActivity.class);
                    intent.putExtra("offer_id", offerList.get(position).getId());
                    startActivityForResult(intent, 12);
                } else if (id == R.id.deleteOffer) {
                    AppErrorsManager.showSuccessDialog(ShopDetailsActivity.this,
                            "عملية الحذف", " هل تريد حذف هذا العرض؟", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteOffer(offerList.get(position).getId());
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                }
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(shopAdapter);
        getShopList();
        ImageButton searchBtoon = findViewById(R.id.searchBtoon);
        searchBtoon.setOnClickListener(this);

        final SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(false);
                getShopList();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.addLayout) {
            Intent intent = new Intent(ShopDetailsActivity.this, AddOfferActivity.class);
            intent.putExtra("shop_id", shop.getId());
            startActivityForResult(intent, 12);
        } else if (id == R.id.backLayout) {
            finish();
        } else if (id == R.id.searchBtoon) {
            getofferSearchList();
        }
    }

    public void getShopList() {
        offerList.clear();
        recyclerView.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        noTxt.setVisibility(View.GONE);
        Offer offer = new Offer();
        offer.setShop_id(shop.getId());
        connectionManager.getOfferList(offer, new InstallCallback() {
            @Override
            public void onStatusDone(String status) {

                if (status.equals("[]")) {
                    recyclerView.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                    noTxt.setVisibility(View.VISIBLE);
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(status);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("id");
                            String offer_name = jsonObject.getString("offer_name");
                            String offer_bio = jsonObject.getString("offer_bio");
                            String befor_discount = jsonObject.getString("befor_discount");
                            String after_discount = jsonObject.getString("after_discount");
                            String offer_image = jsonObject.getString("offer_image");
                            String rating = jsonObject.getString("rating");

                            JSONArray jsonArray2 = new JSONArray(offer_image);
                            String image = jsonArray2.getString(0);


                            String shop_id = jsonObject.getString("shop_id");
                            Offer offer = new Offer(id, offer_name, offer_bio, befor_discount,
                                    after_discount, image, shop_id, rating, "");
                            offerList.add(offer);
                            progressbar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            noTxt.setVisibility(View.GONE);
                            shopAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressbar.setVisibility(View.GONE);
                        noTxt.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String error) {
                progressbar.setVisibility(View.GONE);
                noTxt.setVisibility(View.GONE);
                AppErrorsManager.showErrorDialog(ShopDetailsActivity.this, error);
            }
        });

    }

    public void getofferSearchList() {
        offerList.clear();
        recyclerView.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        noTxt.setVisibility(View.GONE);
        String offerName = offerEditText.getText().toString().trim();
        connectionManager.getOfferStoreSearch(shop.getId(), offerName, new InstallCallback() {
            @Override
            public void onStatusDone(String status) {

                if (status.equals("[]")) {
                    recyclerView.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                    noTxt.setVisibility(View.VISIBLE);
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(status);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("id");
                            String offer_name = jsonObject.getString("offer_name");
                            String offer_bio = jsonObject.getString("offer_bio");
                            String befor_discount = jsonObject.getString("befor_discount");
                            String after_discount = jsonObject.getString("after_discount");
                            String offer_image = jsonObject.getString("offer_image");
                            String rating = jsonObject.getString("rating");

                            JSONArray jsonArray2 = new JSONArray(offer_image);
                            String image = jsonArray2.getString(0);

                            String shop_id = jsonObject.getString("shop_id");
                            Offer offer = new Offer(id, offer_name, offer_bio, befor_discount,
                                    after_discount, image, shop_id, rating, "");
                            offerList.add(offer);
                            progressbar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            noTxt.setVisibility(View.GONE);
                            shopAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressbar.setVisibility(View.GONE);
                        noTxt.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String error) {
                progressbar.setVisibility(View.GONE);
                noTxt.setVisibility(View.GONE);
                AppErrorsManager.showErrorDialog(ShopDetailsActivity.this, error);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && data != null) {
            getShopList();
        }
    }

    public void deleteOffer(String id) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.deleteOffer(id, new InstallCallback() {
            @Override
            public void onStatusDone(String status) {
               progressDialog.dismiss();
                AppErrorsManager.showSuccessDialog(ShopDetailsActivity.this, status, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getShopList();
                    }
                });
            }

            @Override
            public void onError(String error) {
               progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(ShopDetailsActivity.this, error);
            }
        });

    }
}
