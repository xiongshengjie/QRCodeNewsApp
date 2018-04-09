package cn.xcloude.qrcodenewsapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.xyzlf.share.library.bean.ShareEntity;
import com.xyzlf.share.library.interfaces.ShareConstant;
import com.xyzlf.share.library.util.ShareUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.News;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;

import static cn.xcloude.qrcodenewsapp.constant.Constants.PREFIX;
import static cn.xcloude.qrcodenewsapp.constant.Constants.SYSTEM_USER;

public class NewsContentActivity extends AppCompatActivity {

    private News news;
    private boolean isSystem = false;

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
    @BindView(R.id.share_button)
    FloatingActionButton shareButton;

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
        if(SYSTEM_USER.equals(news.getNewsAuthor())){
            isSystem = true;
        }
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

        if(isSystem){
            newsTitle.setVisibility(View.GONE);
            newsAuthor.setVisibility(View.GONE);
        }else{
            newsTitle.setText(news.getNewsTitle());
            newsAuthor.setText(news.getNewsAuthor());
        }
        setSupportActionBar(contentToolBar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        contentWebView.loadUrl(news.getNewsUrl());
        final List<NewsCategory> category = DataSupport.where("categoryId = ?", news.getNewsCategory().toString()).find(NewsCategory.class);
        if (category.size() > 0) {
            collapsingBar.setTitle(category.get(0).getCategoryName());
        }
        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background);

        Glide.with(NewsContentActivity.this)
                .load(news.getNewsImg().split("\\|")[0])
                .apply(options)
                .into(headToolbar);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareEntity shareBean = new ShareEntity(news.getNewsTitle(), category.get(0).getCategoryName());
                shareBean.setUrl(news.getNewsUrl()); //分享链接
                String filePath = ShareUtil.saveBitmapToSDCard(NewsContentActivity.this, ZXingUtils.createQRImage(PREFIX + news.getNewsId()));
                shareBean.setImgUrl(filePath);
                ShareUtil.showShareDialog(NewsContentActivity.this, shareBean, ShareConstant.REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
