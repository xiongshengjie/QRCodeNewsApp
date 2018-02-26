package cn.xcloude.qrcodenewsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_publish)
    Button publishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //分类初始化
        OkHttpUtil.getAllCategory(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "获取分类失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                Map<String, Object> result = gson.fromJson(response.body().string(), new TypeToken<Map<String, Object>>() {
                }.getType());
                if (Integer.parseInt(result.get(Constants.STATUS).toString().substring(0, 4)) == Constants.SUCCESS) {
                    String allCategories = result.get("categories").toString();
                    List<NewsCategory> categories = gson.fromJson(allCategories, new TypeToken<List<NewsCategory>>() {
                    }.getType());
                    if (categories != null && categories.size() > 0) {
                        DataSupport.deleteAll(NewsCategory.class);
                        for (NewsCategory category : categories) {
                            category.save();
                        }
                    }
                }
            }
        });
    }

    @OnClick(R.id.btn_publish)
    public void OnClickPublish() {
        Intent intent = new Intent(MainActivity.this, PublishNewsActivity.class);
        startActivity(intent);
    }
}
