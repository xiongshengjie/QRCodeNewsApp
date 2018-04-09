package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.luck.picture.lib.decoration.RecycleViewDivider;
import com.xyzlf.share.library.bean.ShareEntity;
import com.xyzlf.share.library.interfaces.ShareConstant;
import com.xyzlf.share.library.util.ShareUtil;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.News;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.fragment.ListNewsFragment;
import cn.xcloude.qrcodenewsapp.interfaces.OnItemClickListener;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static cn.xcloude.qrcodenewsapp.constant.Constants.PAGESIZE;
import static cn.xcloude.qrcodenewsapp.constant.Constants.PREFIX;

public class MyPublishActivity extends AppCompatActivity {

    @BindView(R.id.publish_swipe)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.news_list)
    SwipeMenuRecyclerView newsListRecyclerView;
    @BindView(R.id.publish_toolbar)
    Toolbar publishToolbar;

    private int pageNum = 1;
    private boolean isMore = true;
    private int lastVisibleItem;
    private List<News> newsList = new ArrayList<>();
    private NewsAdapter newsAdapter;

    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_publish);

        ButterKnife.bind(this);
        initViews();
        getNews();
    }

    private void initViews() {

        setSupportActionBar(publishToolbar);
        publishToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPublishActivity.this.finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        linearLayoutManager = new LinearLayoutManager(MyPublishActivity.this);
        newsListRecyclerView.setLayoutManager(linearLayoutManager);
        newsListRecyclerView.addItemDecoration(new RecycleViewDivider(getApplicationContext(), LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(MyPublishActivity.this, R.color.gray)));

        if (newsAdapter == null) {
            newsAdapter = new NewsAdapter();
            newsAdapter.setmOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    News news = newsList.get(position);
                    Intent intent = new Intent(MyPublishActivity.this, NewsContentActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("news",news);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        newsListRecyclerView.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {

                if(viewType == 2){
                    return;
                }

                int width = getResources().getDimensionPixelSize(R.dimen.news_list_three_height);

                // MATCH_PARENT 自适应高度，保持和内容一样高；也可以指定菜单具体高度，也可以用WRAP_CONTENT。
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                SwipeMenuItem deleteItem = new SwipeMenuItem(MyPublishActivity.this)
                        .setBackgroundColor(getResources().getColor(R.color.red))
                        .setText("删除")
                        .setTextSize(20)
                        .setHeight(height)
                        .setWidth(width);
                SwipeMenuItem topItem = new SwipeMenuItem(MyPublishActivity.this)
                        .setBackgroundColor(getResources().getColor(R.color.gray))
                        .setText("分享")
                        .setTextSize(20)
                        .setHeight(height)
                        .setWidth(width);
                swipeRightMenu.addMenuItem(topItem);
                swipeRightMenu.addMenuItem(deleteItem);
            }
        });

        newsListRecyclerView.setSwipeMenuItemClickListener(new SwipeMenuItemClickListener(){
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                final int position = menuBridge.getAdapterPosition();
                final News news = newsList.get(position);
                if(menuBridge.getPosition() == 0){
                    Bitmap share = ZXingUtils.createQRImage(PREFIX + news.getNewsId());
                    ShareEntity testBean = new ShareEntity(news.getNewsTitle(), "震惊，又发生了一件神奇的事情，快来看看我发布的新闻");
                    testBean.setUrl(Constants.baseUrl + "/" + news.getNewsUrl()); //分享链接
                    String filePath = ShareUtil.saveBitmapToSDCard(MyPublishActivity.this, share);
                    testBean.setImgUrl(filePath);
                    ShareUtil.showShareDialog(MyPublishActivity.this, testBean, ShareConstant.REQUEST_CODE);
                }else {
                    //删除
                    AlertDialog dialog = new AlertDialog.Builder(MyPublishActivity.this)
                            .setTitle(R.string.title)
                            .setMessage(R.string.del_message)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    OkHttpUtil.delNews(news.getNewsId(), new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MyPublishActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                                                    swipeRefreshLayout.setRefreshing(false);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            if (response.code() == 200) {
                                                Gson gson = new Gson();
                                                final ResponseResult<Object> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<Object>>() {
                                                }.getType());
                                                final String message = serverResponse.getMessage();
                                                if (serverResponse.getStatus() == Constants.SUCCESS) {
                                                    //删除新闻成功
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            newsList.remove(position);
                                                            newsAdapter.notifyItemRemoved(position);
                                                            Toast.makeText(MyPublishActivity.this,R.string.del_success,Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    //删除新闻失败
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(MyPublishActivity.this, message, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(MyPublishActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    
                                }
                            })
                            .show();
                }
                menuBridge.closeMenu();
            }
        });

        newsListRecyclerView.setAdapter(newsAdapter);

        //设置上拉加载更多
        newsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == newsAdapter.getItemCount() && isMore) {
                    getNews();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getNews();
            }
        });

    }

    private void getNews() {
        OkHttpUtil.listNewsByUser(getSharedPreferences("User", Context.MODE_PRIVATE).getString("userId", null), pageNum, PAGESIZE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyPublishActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    final ResponseResult<List<News>> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<List<News>>>() {
                    }.getType());
                    final String message = serverResponse.getMessage();
                    if (serverResponse.getStatus() == Constants.SUCCESS) {
                        //获取新闻成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (serverResponse.getResult().size() == PAGESIZE) {
                                    newsList.addAll(serverResponse.getResult());
                                    isMore = true;
                                    pageNum++;
                                    newsAdapter.notifyItemRangeChanged(newsList.size() - PAGESIZE, PAGESIZE);
                                } else if (serverResponse.getResult().size() > 0) {
                                    newsList.addAll(serverResponse.getResult());
                                    pageNum++;
                                    isMore = false;
                                    newsAdapter.notifyItemRangeChanged(newsList.size() - serverResponse.getResult().size(), serverResponse.getResult().size());
                                } else {
                                    isMore = false;
                                    newsAdapter.notifyItemChanged(newsList.size());
                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        //获取新闻失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyPublishActivity.this, message, Toast.LENGTH_SHORT).show();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyPublishActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ONE_PICTURE = 0;
        private static final int TYPE_FOOT_VIEW = 2;

        private OnItemClickListener mOnItemClickListener;

        public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
            this.mOnItemClickListener = mOnItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_ONE_PICTURE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_one, parent, false);
                ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                itemViewHolder.vOnItemClickListener = mOnItemClickListener;
                return itemViewHolder;
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foot, parent, false);
                return new NewsAdapter.FootViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NewsAdapter.ItemViewHolder) {
                News news = newsList.get(position);
                String imagePath = news.getNewsImg();
                ((NewsAdapter.ItemViewHolder) holder).newsTitle.setText(news.getNewsTitle());
                ((NewsAdapter.ItemViewHolder) holder).newsAnother.setText(news.getNewsAuthor());
                if (!TextUtils.isEmpty(imagePath)) {
                    imagePath = imagePath.split("\\|")[0];
                    RequestOptions options = new RequestOptions();
                    options.centerCrop();
                    ((NewsAdapter.ItemViewHolder) holder).newsPic.setVisibility(View.VISIBLE);
                    Glide.with(MyPublishActivity.this)
                            .load(Constants.baseUrl + "/" + imagePath)
                            .apply(options)
                            .into(((NewsAdapter.ItemViewHolder) holder).newsPic);

                }else {
                    ((NewsAdapter.ItemViewHolder) holder).newsPic.setVisibility(View.GONE);
                }
            }else if(holder instanceof NewsAdapter.FootViewHolder){
                if(!isMore) {
                    ((NewsAdapter.FootViewHolder) holder).loading.setText(R.string.no_more);
                }
            }
        }

        @Override
        public int getItemCount() {
            return newsList.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == newsList.size()) {
                return TYPE_FOOT_VIEW;
            } else {
                return TYPE_ONE_PICTURE;
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            OnItemClickListener vOnItemClickListener;
            @BindView(R.id.news_title)
            TextView newsTitle;
            @BindView(R.id.news_another)
            TextView newsAnother;
            @BindView(R.id.news_pic)
            ImageView newsPic;

            public ItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (vOnItemClickListener != null) {
                    vOnItemClickListener.onItemClick(v, getAdapterPosition());
                }
            }
        }

        class FootViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.loading)
            TextView loading;

            public FootViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
