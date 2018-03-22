package cn.xcloude.qrcodenewsapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.News;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;

public class NewsContentActivity extends AppCompatActivity {

    private News news;

    @BindView(R.id.content_toolbar)
    Toolbar contentToolBar;
    @BindView(R.id.collapsing_bar)
    CollapsingToolbarLayout collapsingBar;
    @BindView(R.id.content_web_view)
    WebView contentWebView;
    @BindView(R.id.head_toolbar)
    ImageView headToolbar;
    @BindView(R.id.news_title)
    TextView newsTitle;
    @BindView(R.id.news_author)
    TextView newsAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);
        ButterKnife.bind(this);

        init();
        initView();
    }

    private void init() {
        news = (News) getIntent().getSerializableExtra("news");
    }

    private void initView() {
        contentWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                WebView.HitTestResult hit = view.getHitTestResult();
                if (hit != null) {
                    int hitType = hit.getType();
                    if (hitType == WebView.HitTestResult.SRC_ANCHOR_TYPE
                            || hitType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {// 点击超链接
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else {
                        view.loadUrl(url);
                    }
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });

        newsTitle.setText(news.getNewsTitle());
        newsAuthor.setText(news.getNewsAuthor());
        setSupportActionBar(contentToolBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        contentWebView.loadUrl(Constants.baseUrl + "/" + news.getNewsUrl());
        List<NewsCategory> category = DataSupport.where("categoryId = ?", news.getNewsCategory().toString()).find(NewsCategory.class);
        if (category.size() > 0) {
            collapsingBar.setTitle(category.get(0).getCategoryName());
        }
        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background);

        Glide.with(NewsContentActivity.this)
                .load(Constants.baseUrl + "/" + news.getNewsImg().split("\\|")[0])
                .apply(options)
                .into(headToolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentWebView.stopLoading();
        contentWebView.removeAllViews();
        contentWebView.destroy();
        contentWebView = null;
    }
}
