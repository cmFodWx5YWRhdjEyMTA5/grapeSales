package com.creatively.grapeSalesApp.grapeapplication.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.creatively.grapeSalesApp.grapeapplication.R;
import com.creatively.grapeSalesApp.grapeapplication.callback.InstallCallback;
import com.creatively.grapeSalesApp.grapeapplication.callback.OfferCallback;
import com.creatively.grapeSalesApp.grapeapplication.manager.AppErrorsManager;
import com.creatively.grapeSalesApp.grapeapplication.manager.ConnectionManager;
import com.creatively.grapeSalesApp.grapeapplication.manager.FontManager;
import com.creatively.grapeSalesApp.grapeapplication.model.Offer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ConnectionManager connectionManager;
    private String order_id;
    private TextView offerNameTxt, shopNameTxt, userNameTxt, mobileTxt, locationTxt, noteTxt,
            offerPreviousPrice, offerNextPrice, statusTxt, typeTxt;
    private SliderLayout sliderLayout;
    private ArrayList<String> imageArrayList = new ArrayList<>();
    private String imageArray;
    private Handler handler;
    private ScrollView scrollView;
    private Button agreeBtn, rejectBtn, processBtn, completedBtn;
    private RelativeLayout typeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale locale = new Locale("ar");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_order_details);
        handler = new Handler(Looper.getMainLooper());
        connectionManager = new ConnectionManager(this);
        order_id = getIntent().getStringExtra("order_id");
        init();
    }

    public void init() {
        scrollView = findViewById(R.id.scrollView);
        scrollView.setVisibility(View.GONE);
        RelativeLayout backLayout = findViewById(R.id.backLayout);
        backLayout.setOnClickListener(this);
        agreeBtn = findViewById(R.id.agreeBtn);
        rejectBtn = findViewById(R.id.rejectBtn);
        processBtn = findViewById(R.id.doneBtn);
        completedBtn = findViewById(R.id.completedBtn);
        agreeBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
        completedBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        statusTxt = findViewById(R.id.statusTxt);
        typeTxt = findViewById(R.id.typeTxt);
        typeLayout = findViewById(R.id.typeLayout);
        offerNameTxt = findViewById(R.id.offerNameTxt);
        shopNameTxt = findViewById(R.id.shopNameTxt);
        userNameTxt = findViewById(R.id.userNameTxt);
        mobileTxt = findViewById(R.id.mobileTxt);
        locationTxt = findViewById(R.id.locationTxt);
        noteTxt = findViewById(R.id.noteTxt);
        offerPreviousPrice = findViewById(R.id.offerPreviousPrice);
        offerNextPrice = findViewById(R.id.offerNextPrice);
        sliderLayout = findViewById(R.id.slider);

        getOrderDetails(order_id);
    }

    public void getImageListSlider() {
//        imageArrayList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < imageArrayList.size(); i++) {
                    imageArray = imageArrayList.get(i);

                    final TextSliderView textSliderView = new TextSliderView(OrderDetailsActivity.this);
                    textSliderView
                            .image(FontManager.IMAGE_URL + imageArray)
                            .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent i = new Intent(OrderDetailsActivity.this, ImageViewerPagerActivity.class);
                                    i.putStringArrayListExtra("IMAGES", imageArrayList);
                                    i.putExtra("SELECTED", slider.getBundle().getInt("position"));
                                    startActivity(i);
                                }
                            });
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle()
                            .putSerializable("extra", imageArrayList.get(i));
                    textSliderView.getBundle()
                            .putInt("position", i);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sliderLayout.addSlider(textSliderView);
                        }
                    });
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sliderLayout.setCurrentPosition(0, true);
                        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
                        sliderLayout.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));
                        sliderLayout.setCustomAnimation(new DescriptionAnimation());
                        sliderLayout.setDuration(4000);
                    }
                });

            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backLayout) {
            finish();
        } else if (id == R.id.agreeBtn) {
            AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this,
                    "الموافقة على الطلب", "هل أنت متأكد على قبول هذا الطلب؟", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            acceptOrder(order_id);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        } else if (id == R.id.rejectBtn) {
            AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this,
                    "رفض الطلب", "هل أنت متأكد من رفض هذا الطلب؟", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rejectOrder(order_id);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        } else if (id == R.id.doneBtn) {
            AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this,
                    "تجهيز الطلب", "هل أنت متأكد من أن الطلب أصبح جاهزاً لتسليم؟", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            processDoneOrder(order_id);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        }else if (id == R.id.completedBtn) {
            AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this,
                    "اكتمال الطلب", "هل أنت متأكد من تسليم الطلب؟", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            completeOrder(order_id);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        }
    }

    public void getOrderDetails(String order_id) {
        imageArrayList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.orderDetails(order_id, new InstallCallback() {
            @Override
            public void onStatusDone(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String id = jsonObject.getString("id");
                    String username = jsonObject.getString("username");
                    String mobile = jsonObject.getString("mobile");
                    String address = jsonObject.getString("address");
                    String note = jsonObject.getString("note");
                    String status = jsonObject.getString("status");
                    String delivery_type = jsonObject.getString("delivery_type");
                    if (TextUtils.equals(delivery_type, "null")) {
                        typeLayout.setVisibility(View.GONE);
                    } else {
                        typeLayout.setVisibility(View.VISIBLE);
                        typeTxt.setText(delivery_type);
                    }
                    if (TextUtils.equals(status, "0")) {
                        agreeBtn.setVisibility(View.VISIBLE);
                        rejectBtn.setVisibility(View.VISIBLE);
                        processBtn.setVisibility(View.GONE);
                        completedBtn.setVisibility(View.GONE);
                    } else if (TextUtils.equals(status, "1")) {
                        agreeBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        completedBtn.setVisibility(View.GONE);
                        processBtn.setVisibility(View.VISIBLE);
                    } else if (TextUtils.equals(status, "2") || TextUtils.equals(status, "3")) {
                        agreeBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        processBtn.setVisibility(View.GONE);
                        completedBtn.setVisibility(View.GONE);
                    } else if (TextUtils.equals(status, "4")) {
                        agreeBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        processBtn.setVisibility(View.GONE);
                        completedBtn.setVisibility(View.VISIBLE);
                    } else if (TextUtils.equals(status, "5")) {
                        agreeBtn.setVisibility(View.GONE);
                        rejectBtn.setVisibility(View.GONE);
                        processBtn.setVisibility(View.GONE);
                        completedBtn.setVisibility(View.GONE);
                    }

                    if (TextUtils.equals(status, "0")) {
                        statusTxt.setText("قيد الانتظار");
                    } else if (TextUtils.equals(status, "1")) {
                        statusTxt.setText("قيد التجهيز");
                    } else if (TextUtils.equals(status, "2")) {
                        statusTxt.setText("تم الغاء الطلب");
                    } else if (TextUtils.equals(status, "3")) {
                        statusTxt.setText("تم رفض الطلب");
                    } else if (TextUtils.equals(status, "4")) {
                        statusTxt.setText("قيد التوصيل");
                    } else if (TextUtils.equals(status, "5")) {
                        statusTxt.setText("اكتمال الطلب");
                    }
                    String shop = jsonObject.getString("shop");
                    JSONObject shopObject = new JSONObject(shop);
                    String shop_name = shopObject.getString("shop_name");
                    String offer = jsonObject.getString("offer");
                    JSONObject offerObject = new JSONObject(offer);
                    String offer_name = offerObject.getString("offer_name");
                    String befor_discount = offerObject.getString("befor_discount");
                    String after_discount = offerObject.getString("after_discount");
                    String offer_image = offerObject.getString("offer_image");
                    JSONArray jsonArray2 = new JSONArray(offer_image);
                    for (int j = 0; j < jsonArray2.length(); j++) {
                        String aa = jsonArray2.getString(j);
                        imageArrayList.add(aa);
                    }
                    offerNameTxt.setText(offer_name);
                    userNameTxt.setText(username);
                    mobileTxt.setText(mobile);
                    locationTxt.setText(address);
                    if (TextUtils.equals(note, "null"))
                        noteTxt.setText("لا يوجد ملاحظات");
                    else
                        noteTxt.setText(note);
                    shopNameTxt.setText(shop_name);
                    offerPreviousPrice.setText(befor_discount);
                    offerPreviousPrice.setPaintFlags(offerPreviousPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    offerNextPrice.setText(after_discount);
                    progressDialog.dismiss();
                    scrollView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getImageListSlider();

            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(OrderDetailsActivity.this, error);
            }
        });

    }

    public void acceptOrder(final String order_id) {
        imageArrayList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.acceptOrder(order_id, new InstallCallback() {
            @Override
            public void onStatusDone(String result) {
                progressDialog.dismiss();
                AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this, result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOrderDetails(order_id);
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(OrderDetailsActivity.this, error);
            }
        });
    }

    public void rejectOrder(final String order_id) {
        imageArrayList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.rejectOrder(order_id, new InstallCallback() {
            @Override
            public void onStatusDone(String result) {
                progressDialog.dismiss();
                AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this, result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOrderDetails(order_id);
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(OrderDetailsActivity.this, error);
            }
        });
    }

    public void processDoneOrder(final String order_id) {
        imageArrayList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.processDoneOrder(order_id, new InstallCallback() {
            @Override
            public void onStatusDone(String result) {
                progressDialog.dismiss();
                AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this, result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOrderDetails(order_id);
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(OrderDetailsActivity.this, error);
            }
        });
    }

    public void completeOrder(final String order_id) {
        imageArrayList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        connectionManager.completedOrder(order_id, new InstallCallback() {
            @Override
            public void onStatusDone(String result) {
                progressDialog.dismiss();
                AppErrorsManager.showSuccessDialog(OrderDetailsActivity.this, result, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getOrderDetails(order_id);
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                AppErrorsManager.showErrorDialog(OrderDetailsActivity.this, error);
            }
        });
    }

}
