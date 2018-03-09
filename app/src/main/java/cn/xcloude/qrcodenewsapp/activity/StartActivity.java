package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StartActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAllCategory();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(getSharedPreferences("User", Context.MODE_PRIVATE).getString("userId", null))) {
                    gotoMain(true);
                }else {
                    gotoMain(false);
                }

            }
        }, 2000);

    }

    private void getAllCategory() {
        //分类初始化
        OkHttpUtil.getAllCategory(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StartActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    Gson gson = new Gson();
                    ResponseResult<List<NewsCategory>> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<List<NewsCategory>>>() {
                    }.getType());
                    if (serverResponse.getStatus() == Constants.SUCCESS) {
                        List<NewsCategory> categories = serverResponse.getResult();
                        if (categories != null && categories.size() > 0) {
                            DataSupport.deleteAll(NewsCategory.class);
                            for (NewsCategory category : categories) {
                                category.save();
                            }
                        }
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StartActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 前往主页
     */
    private void gotoMain(boolean flag) {
        Intent intent;
        if (flag) {
            intent = new Intent(StartActivity.this, MainActivity.class);
        } else {
            intent = new Intent(StartActivity.this, LoginMainActivity.class);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
