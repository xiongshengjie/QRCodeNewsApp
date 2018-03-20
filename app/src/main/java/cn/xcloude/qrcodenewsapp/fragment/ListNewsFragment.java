package cn.xcloude.qrcodenewsapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
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
import com.luck.picture.lib.decoration.RecycleViewDivider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.News;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static cn.xcloude.qrcodenewsapp.constant.Constants.PAGESIZE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListNewsFragment extends Fragment {

    private static final String categoryId = "categoryId";
    private static final String categoryName = "categoryName";

    private int id, pageNum = 1;
    private String name;
    private List<News> newsList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;

    public ListNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id   分类id.
     * @param name 分类名称.
     * @return A new instance of fragment ListNewsFragment.
     */
    public static ListNewsFragment newInstance(int id, String name) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putInt(categoryId, id);
        args.putString(categoryName, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt(categoryId);
            name = getArguments().getString(categoryName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_news, container, false);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        recyclerView = rootView.findViewById(R.id.news_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL,2, ContextCompat.getColor(getActivity(),R.color.gray)));
        getNews();
        if (newsAdapter == null) {
            newsAdapter = new NewsAdapter();
            recyclerView.setAdapter(newsAdapter);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getNews();
            }
        });
        return rootView;
    }

    private void getNews() {
        OkHttpUtil.listNews(id, pageNum, PAGESIZE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (serverResponse.getResult().size() > 0) {
                                    for (News news : serverResponse.getResult()) {
                                        newsList.add(news);
                                    }
                                    pageNum++;
                                    newsAdapter.notifyDataSetChanged();
                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        //获取新闻失败
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ONE_PICTURE = 0;
        private static final int TYPE_THREE_PICTURE = 1;
        private static final int TYPE_FOOT_VIEW = 2;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == TYPE_ONE_PICTURE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_one, parent, false);
                return new OnePictureViewHolder(view);
            } else if (viewType == TYPE_THREE_PICTURE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_three, parent, false);
                return new ThreePictureViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foot, parent, false);
                return new FootViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == newsList.size() + 1) {
                return;
            }
            if (holder instanceof OnePictureViewHolder) {
                News news = newsList.get(position);
                String imagePath = news.getNewsImg();
                ((OnePictureViewHolder) holder).newsTitle.setText(news.getNewsTitle());
                ((OnePictureViewHolder) holder).newsAnother.setText(news.getNewsAuthor());
                if (!TextUtils.isEmpty(imagePath)) {
                    imagePath = imagePath.split("\\|")[0];
                    RequestOptions options = new RequestOptions();
                    options.centerCrop();
                    Glide.with(getActivity())
                            .load(Constants.baseUrl + "/" + imagePath)
                            .apply(options)
                            .into(((OnePictureViewHolder) holder).newsPic);

                }
            } else if (holder instanceof ThreePictureViewHolder) {
                News news = newsList.get(position);
                String imagePath = news.getNewsImg();
                ((ThreePictureViewHolder) holder).newsTitle.setText(news.getNewsTitle());
                ((ThreePictureViewHolder) holder).newsAnother.setText(news.getNewsAuthor());
                String path[] = imagePath.split("\\|");
                RequestOptions options = new RequestOptions();
                options.centerCrop();
                Glide.with(getActivity())
                        .load(Constants.baseUrl + "/" + path[0])
                        .apply(options)
                        .into(((ThreePictureViewHolder) holder).imageViewOne);

                Glide.with(getActivity())
                        .load(Constants.baseUrl + "/" + path[1])
                        .apply(options)
                        .into(((ThreePictureViewHolder) holder).imageViewTwo);

                Glide.with(getActivity())
                        .load(Constants.baseUrl + "/" + path[2])
                        .apply(options)
                        .into(((ThreePictureViewHolder) holder).imageViewThree);
            }
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (newsList.size() == 0) {
                return TYPE_FOOT_VIEW;
            }
            if (position == newsList.size() + 1) {
                return TYPE_FOOT_VIEW;
            }
            if (newsList.get(position).getNewsImg().split("\\|").length < 3) {
                return TYPE_ONE_PICTURE;
            } else {
                return TYPE_THREE_PICTURE;
            }
        }

        class OnePictureViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.news_title)
            TextView newsTitle;
            @BindView(R.id.news_another)
            TextView newsAnother;
            @BindView(R.id.news_pic)
            ImageView newsPic;

            public OnePictureViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ThreePictureViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.news_title)
            TextView newsTitle;
            @BindView(R.id.news_another)
            TextView newsAnother;
            @BindView(R.id.image_url_one)
            ImageView imageViewOne;
            @BindView(R.id.image_url_two)
            ImageView imageViewTwo;
            @BindView(R.id.image_url_three)
            ImageView imageViewThree;

            public ThreePictureViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
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
