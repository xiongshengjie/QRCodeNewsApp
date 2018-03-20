package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import com.bumptech.glide.request.RequestOptions;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;
import cn.xcloude.qrcodenewsapp.fragment.ListNewsFragment;
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
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    private TextView tvPersonDes, tvPersonName;
    private CircleImageView navHeadImage;

    private SharedPreferences sharedPreferences;
    private boolean isLogin;
    private RequestOptions options = new RequestOptions();

    private List<Fragment> fragmentList;
    private List<NewsCategory> categoryList;
    private List<String> titles;

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
        options.centerCrop();
        fragmentList = new ArrayList<>();
        categoryList = DataSupport.findAll(NewsCategory.class);
        titles = new ArrayList<>();
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
                    startActivity(intent);
                }
            }
        });

        View headView = navView.getHeaderView(0);

        tvPersonDes = headView.findViewById(R.id.tv_person_description);
        tvPersonName = headView.findViewById(R.id.tv_person_name);
        navHeadImage = headView.findViewById(R.id.nav_head_image);


        if (TextUtils.isEmpty(sharedPreferences.getString("userId", null))) {
            Glide.with(MainActivity.this)
                    .load(R.drawable.person_center)
                    .apply(options)
                    .into(headImage);
            isLogin = false;
        } else {
            //登录过，设置为头像
            String head = sharedPreferences.getString("userHead", null);
            if (TextUtils.isEmpty(head)) {
                Glide.with(MainActivity.this)
                        .load(R.drawable.person_center)
                        .apply(options)
                        .into(headImage);
                Glide.with(MainActivity.this)
                        .load(R.drawable.person_center)
                        .apply(options)
                        .into(navHeadImage);
            } else {
                Glide.with(MainActivity.this)
                        .load(Constants.baseUrl + "/" + head)
                        .apply(options)
                        .into(headImage);
                Glide.with(MainActivity.this)
                        .load(Constants.baseUrl + "/" + head)
                        .apply(options)
                        .into(navHeadImage);
            }
            isLogin = true;
        }

        if (isLogin) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            tvPersonName.setText(sharedPreferences.getString("userNickname", null));
            tvPersonDes.setText(sharedPreferences.getString("userDescription", null));
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
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.person_publish:
                        //我的发布界面
                        break;
                    case R.id.person_information:
                        //修改个人信息
                        break;
                    case R.id.person_logout:
                        Intent intent = new Intent(MainActivity.this, LoginMainActivity.class);
                        startActivity(intent);
                        sharedPreferences.edit().clear().apply();
                        finish();
                }
                return false;
            }
        });

        for (NewsCategory category : categoryList) {
            ListNewsFragment fragment = ListNewsFragment.newInstance(category.getCategoryId(), category.getCategoryName());
            titles.add(category.getCategoryName());
            fragmentList.add(fragment);
        }

        viewPager.setAdapter(new ListNewsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

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

    class ListNewsAdapter extends FragmentStatePagerAdapter {

        private ListNewsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getCategoryName();
        }
    }
}
