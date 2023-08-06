package com.cafefood.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafefood.R;
import com.cafefood.adepter.CouponAdp;
import com.cafefood.model.Coupon;
import com.cafefood.model.Couponlist;
import com.cafefood.model.RestResponse;
import com.cafefood.model.User;
import com.cafefood.retrofit.APIClient;
import com.cafefood.retrofit.GetResult;
import com.cafefood.utils.CustPrograssbar;
import com.cafefood.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

import static com.cafefood.utils.SessionManager.COUPON;
import static com.cafefood.utils.SessionManager.COUPONID;


public class CoupunActivity extends BaseActivity implements GetResult.MyListener, CouponAdp.RecyclerTouchListener {

    CustPrograssbar custPrograssbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    User user;
    SessionManager sessionManager;
    int amount = 0;
    String PAYMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupun);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Apply Coupons");
        amount = getIntent().getIntExtra("amount", 0);
        PAYMENT = getIntent().getStringExtra("payment");
        sessionManager = new SessionManager(CoupunActivity.this);
        user = sessionManager.getUserDetails("");
        custPrograssbar = new CustPrograssbar();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getCoupuns();

    }

    private void getCoupuns() {
        custPrograssbar.prograssCreate(CoupunActivity.this);
        JSONObject jsonObject = new JSONObject();
        JsonParser jsonParser = new JsonParser();
        Call<JsonObject> call = APIClient.getInterface().getCoupuns((JsonObject) jsonParser.parse(jsonObject.toString()));
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    private void chackCoupuns(String cid) {
        try {
            custPrograssbar.prograssCreate(CoupunActivity.this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", user.getId());
            jsonObject.put("cid", cid);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().CheckCoupun((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Coupon coupon = gson.fromJson(result.toString(), Coupon.class);
                if (coupon.getResult().equalsIgnoreCase("true")) {
                    CouponAdp couponAdp = new CouponAdp(this, coupon.getCouponlist(), this, amount);
                    recyclerView.setAdapter(couponAdp);
                }
            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                Toast.makeText(CoupunActivity.this, response.getResponseMsg(), Toast.LENGTH_LONG).show();
                if (response.getResult().equalsIgnoreCase("true")) {
                    finish();
                } else {
                    sessionManager.setIntData(COUPON, 0);

                }
            }

        } catch (Exception e) {
            sessionManager.setIntData(COUPON, 0);

        }

    }

    @Override
    public void onClickItem(View v, Couponlist coupon) {
        try {
            if (coupon.getMinAmt() < amount) {
                sessionManager.setIntData(COUPON, Integer.parseInt(coupon.getCValue()));
                sessionManager.setIntData(COUPONID, Integer.parseInt(coupon.getId()));

                chackCoupuns(coupon.getId());
            } else {
                Toast.makeText(CoupunActivity.this,"Sorry this coupon code is not applied",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }


    }

}