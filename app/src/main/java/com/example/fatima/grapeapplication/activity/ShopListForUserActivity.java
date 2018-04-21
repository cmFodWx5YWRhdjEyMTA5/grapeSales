package com.example.fatima.grapeapplication.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fatima.grapeapplication.R;
import com.example.fatima.grapeapplication.adapter.OfferListAdapter;
import com.example.fatima.grapeapplication.adapter.ShopAdapter;
import com.example.fatima.grapeapplication.callback.InstallCallback;
import com.example.fatima.grapeapplication.callback.OnItemClickListener;
import com.example.fatima.grapeapplication.manager.AppErrorsManager;
import com.example.fatima.grapeapplication.manager.AppPreferences;
import com.example.fatima.grapeapplication.manager.ConnectionManager;
import com.example.fatima.grapeapplication.model.Offer;
import com.example.fatima.grapeapplication.model.Shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopListForUserActivity extends AppCompatActivity implements View.OnClickListener {
    private List<Shop> shopList = new ArrayList<>();
    private String category, city, user_id;
    private ConnectionManager connectionManager;
    private ShopAdapter shopAdapter;
    private ImageView progressbar;
    private TextView noTxt;
    private String is_active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops_list);
        category = getIntent().getStringExtra("category");
        city = getIntent().getStringExtra("city");
//        user_id = AppPreferences.getString(getActivity(), "user_id");
        connectionManager = new ConnectionManager(this);
        init();
    }

    public void init() {
        TextView title = findViewById(R.id.registerTitle);
        switch (city) {
            case "Gaza":
                title.setText(getString(R.string.gaza));
                break;
            case "Jabalia":
                title.setText(getString(R.string.jabalia));
                break;
            case "Beit Hanoun":
                title.setText(getString(R.string.bitHanon));
                break;
            case "Central":
                title.setText(getString(R.string.middle));
                break;
            case "Khan Younes":
                title.setText(getString(R.string.khan_younis));
                break;
            case "Rafah":
                title.setText(getString(R.string.rafah));
                break;
        }

        progressbar = findViewById(R.id.waitProgress);
        noTxt = findViewById(R.id.noTxt);

        ObjectAnimator animation = ObjectAnimator.ofFloat(progressbar, "rotationY", 0.0f, 360f);
        animation.setDuration(1000);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
        RelativeLayout backLayout = findViewById(R.id.backLayout);
        backLayout.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        shopAdapter = new ShopAdapter(this, shopList, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ShopListForUserActivity.this, ShopDetailsUserActivity.class);
                intent.putExtra("shop", shopList.get(position));
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(shopAdapter);
        getShopList();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backLayout) {
            finish();
        }
    }

    public void getShopList() {
        shopList.clear();
        progressbar.setVisibility(View.VISIBLE);
        noTxt.setVisibility(View.GONE);
        Shop shop = new Shop();
        shop.setCategory_name(category);
        shop.setCity_name(city);
        connectionManager.getShopListForUser(shop, new InstallCallback() {
            @Override
            public void onStatusDone(String status) {
                if (status.equals("[]")) {
                    noTxt.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(status);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = jsonObject.getString("id");
                            String shop_name = jsonObject.getString("shop_name");
                            String shop_address = jsonObject.getString("shop_address");
                            String shop_phone = jsonObject.getString("shop_phone");
                            String shop_bio = jsonObject.getString("shop_bio");
                            String user_id = jsonObject.getString("user_id");
                            String is_active = jsonObject.getString("is_active");
                            String image_url = jsonObject.getString("image_url");
                            String category_name = jsonObject.getString("category_name");
                            String city_name = jsonObject.getString("city_name");
                            String count = jsonObject.getString("count");
                            Shop shop1 = new Shop(id, shop_name, shop_address, count, image_url);
                            shopList.add(shop1);
                            progressbar.setVisibility(View.GONE);
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
                AppErrorsManager.showErrorDialog(ShopListForUserActivity.this, error);
            }
        });

    }
}
