package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.main_coordinatorLayout)
    CoordinatorLayout mainCoordinatorLayout;
    @BindView(R.id.head_image)
    CircleImageView headImage;
    @BindView(R.id.tv_person_description)
    TextView tvPersonDes;
    @BindView(R.id.tv_person_name)
    TextView tvPersonName;
    @BindView(R.id.nav_head_image)
    CircleImageView navHeadImage;

    private SharedPreferences sharedPreferences;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
        initView();
    }

    private void init() {
        sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);
    }

    private void initView() {
        setSupportActionBar(mainToolbar);
        headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    //未登录，提示或跳转登录界面
                    Intent intent = new Intent(MainActivity.this, LoginMainActivity.class);
                    StartActivity(intent);
                }
            }
        });

        if (TextUtils.isEmpty(sharedPreferences.getString("userId", null))) {
            Glide.with(MainActivity.this)
                    .load(R.drawable.person_center)
                    .centerCrop()
                    .into(headImage);
            isLogin = false;
        } else {
            //登录过，设置为头像
            String head = sharedPreferences.getString("userHead", null);
            if (TextUtils.isEmpty(head)) {
                Glide.with(MainActivity.this)
                        .load(R.drawable.person_center)
                        .centerCrop()
                        .into(headImage);
                Glide.with(MainActivity.this)
                        .load(R.drawable.person_center)
                        .centerCrop()
                        .into(navHeadImage);
            } else {
                Glide.with(MainActivity.this)
                        .load(Constants.baseUrl + "/" + head)
                        .centerCrop()
                        .into(headImage);
                Glide.with(MainActivity.this)
                        .load(Constants.baseUrl + "/" + head)
                        .centerCrop()
                        .into(navHeadImage);
            }
            isLogin = true;
        }

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics dm = new DisplayMetrics();
                manager.getDefaultDisplay().getMetrics(dm);
                mainCoordinatorLayout.layout(navView.getRight(), 0,
                        navView.getRight() + dm.widthPixels, dm.heightPixels);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_news:
                Intent intent;
                if (isLogin) {
                    intent = new Intent(MainActivity.this, PublishNewsActivity.class);
                } else {
                    //未登录，提示或跳转登录界面
                    intent = new Intent(MainActivity.this, LoginMainActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.qr_scanner:
                //扫码
                break;
            default:
        }
        return true;
    }

    /**
     * 重写返回键
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawers();
        } else {
            if (!TextUtils.isEmpty(sharedPreferences.getString("userId", null))) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        }
    }
}
