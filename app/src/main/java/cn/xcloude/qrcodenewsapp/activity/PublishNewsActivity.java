package cn.xcloude.qrcodenewsapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.even.mricheditor.ActionType;
import com.even.mricheditor.RichEditorAction;
import com.even.mricheditor.RichEditorCallback;
import com.even.mricheditor.ui.ActionImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.client.android.utils.ZXingUtils;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.xyzlf.share.library.bean.ShareEntity;
import com.xyzlf.share.library.interfaces.ShareConstant;
import com.xyzlf.share.library.util.ShareUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.News;
import cn.xcloude.qrcodenewsapp.entity.NewsCategory;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.fragment.EditHyperlinkPopWindow;
import cn.xcloude.qrcodenewsapp.fragment.EditTablePopWindow;
import cn.xcloude.qrcodenewsapp.fragment.EditorMenuFragment;
import cn.xcloude.qrcodenewsapp.interfaces.KeyboardHeightObserver;
import cn.xcloude.qrcodenewsapp.interfaces.OnActionPerformListener;
import cn.xcloude.qrcodenewsapp.interfaces.ProgressListener;
import cn.xcloude.qrcodenewsapp.utils.FileIOUtil;
import cn.xcloude.qrcodenewsapp.utils.KeyboardHeightProvider;
import cn.xcloude.qrcodenewsapp.utils.KeyboardUtils;
import cn.xcloude.qrcodenewsapp.utils.LogUtil;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static cn.xcloude.qrcodenewsapp.constant.Constants.PERMISSION_REQUEST_CODE;
import static cn.xcloude.qrcodenewsapp.constant.Constants.PREFIX;

@SuppressLint("SetJavaScriptEnabled")
public class PublishNewsActivity extends AppCompatActivity implements KeyboardHeightObserver {

    private static String TAG = "PublishNewsActivity";

    @BindView(R.id.wv_container)
    WebView mWebView;
    @BindView(R.id.fl_action)
    FrameLayout flAction;
    @BindView(R.id.ll_action_bar_container)
    LinearLayout llActionBarContainer;
    @BindView(R.id.news_title)
    EditText newsTitle;
    @BindView(R.id.news_category)
    Spinner newsCategory;

    private ProgressDialog dialog, firstDialog;

    private List<File> files = new ArrayList<>();
    private List<NewsCategory> categories = new ArrayList<>();
    private News news;

    /**
     * The keyboard height provider
     */
    private KeyboardHeightProvider keyboardHeightProvider;
    private boolean isKeyboardShowing;
    private String htmlContent = "<p><br></p>";

    private RichEditorAction mRichEditorAction;
    private RichEditorCallback mRichEditorCallback;

    private EditorMenuFragment mEditorMenuFragment;

    private List<ActionType> mActionTypeList =
            Arrays.asList(ActionType.BOLD, ActionType.ITALIC, ActionType.UNDERLINE,
                    ActionType.STRIKETHROUGH, ActionType.SUBSCRIPT, ActionType.SUPERSCRIPT,
                    ActionType.NORMAL, ActionType.H1, ActionType.H2, ActionType.H3, ActionType.H4,
                    ActionType.H5, ActionType.H6, ActionType.INDENT, ActionType.OUTDENT,
                    ActionType.JUSTIFY_LEFT, ActionType.JUSTIFY_CENTER, ActionType.JUSTIFY_RIGHT,
                    ActionType.JUSTIFY_FULL, ActionType.ORDERED, ActionType.UNORDERED, ActionType.LINE,
                    ActionType.BLOCK_CODE, ActionType.BLOCK_QUOTE, ActionType.CODE_VIEW);

    private List<Integer> mActionTypeIconList =
            Arrays.asList(R.drawable.ic_format_bold, R.drawable.ic_format_italic,
                    R.drawable.ic_format_underlined, R.drawable.ic_format_strikethrough,
                    R.drawable.ic_format_subscript, R.drawable.ic_format_superscript,
                    R.drawable.ic_format_para, R.drawable.ic_format_h1, R.drawable.ic_format_h2,
                    R.drawable.ic_format_h3, R.drawable.ic_format_h4, R.drawable.ic_format_h5,
                    R.drawable.ic_format_h6, R.drawable.ic_format_indent_decrease,
                    R.drawable.ic_format_indent_increase, R.drawable.ic_format_align_left,
                    R.drawable.ic_format_align_center, R.drawable.ic_format_align_right,
                    R.drawable.ic_format_align_justify, R.drawable.ic_format_list_numbered,
                    R.drawable.ic_format_list_bulleted, R.drawable.ic_line, R.drawable.ic_code_block,
                    R.drawable.ic_format_quote, R.drawable.ic_code_review);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_news);
        ButterKnife.bind(this);

        firstDialog = new ProgressDialog(PublishNewsActivity.this, ProgressDialog.STYLE_SPINNER);
        firstDialog.setCancelable(false);
        firstDialog.setCanceledOnTouchOutside(false);
        firstDialog.setMessage("加载中...");
        firstDialog.show();

        initView();

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                getResources().getDisplayMetrics());
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9,
                getResources().getDisplayMetrics());
        for (int i = 0, size = mActionTypeList.size(); i < size; i++) {
            final ActionImageView actionImageView = new ActionImageView(this);
            actionImageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
            actionImageView.setPadding(padding, padding, padding, padding);
            actionImageView.setActionType(mActionTypeList.get(i));
            actionImageView.setTag(mActionTypeList.get(i));
            actionImageView.setActivatedColor(R.color.colorAccent);
            actionImageView.setDeactivatedColor(R.color.tintColor);
            actionImageView.setRichEditorAction(mRichEditorAction);
            actionImageView.setBackgroundResource(R.drawable.btn_colored_material);
            actionImageView.setImageResource(mActionTypeIconList.get(i));
            actionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionImageView.command();
                }
            });
            llActionBarContainer.addView(actionImageView);
        }

        mEditorMenuFragment = new EditorMenuFragment();
        mEditorMenuFragment.setActionClickListener(new MOnActionPerformListener(mRichEditorAction));
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fl_action, mEditorMenuFragment, EditorMenuFragment.class.getName())
                .commit();
    }

    private void initView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new CustomWebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mRichEditorCallback = new MRichEditorCallback();
        mWebView.addJavascriptInterface(mRichEditorCallback, "MRichEditor");
        mWebView.loadUrl("file:///android_asset/richEditor.html");
        mRichEditorAction = new RichEditorAction(mWebView);

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        findViewById(R.id.fl_container).post(new Runnable() {
            @Override
            public void run() {
                keyboardHeightProvider.start();
            }
        });

        categories = DataSupport.findAll(NewsCategory.class);
        ArrayAdapter<NewsCategory> categoryArrayAdapter = new ArrayAdapter<NewsCategory>(this, android.R.layout.simple_spinner_item, categories);
        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newsCategory.setAdapter(categoryArrayAdapter);
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                firstDialog.dismiss();
                if (!TextUtils.isEmpty(htmlContent)) {
                    mRichEditorAction.insertHtml(htmlContent);
                }
                KeyboardUtils.showSoftInput(PublishNewsActivity.this);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    @OnClick(R.id.iv_action)
    void onClickAction() {
        if (flAction.getVisibility() == View.VISIBLE) {
            flAction.setVisibility(View.GONE);
        } else {
            if (isKeyboardShowing) {
                KeyboardUtils.hideSoftInput(PublishNewsActivity.this);
            }
            flAction.setVisibility(View.VISIBLE);
        }
    }

    private RichEditorCallback.OnGetHtmlListener onGetHtmlListener =
            new RichEditorCallback.OnGetHtmlListener() {
                @Override
                public void getHtml(final String html) {
                    if (TextUtils.isEmpty(html)) {
                        Toast.makeText(PublishNewsActivity.this, "新闻还没写哦", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    final String title = newsTitle.getText().toString();
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(PublishNewsActivity.this, "标题还没写哦", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    final Integer category = ((NewsCategory) newsCategory.getSelectedItem()).getCategoryId();

                    final AlertDialog.Builder publishDialog = new AlertDialog.Builder(PublishNewsActivity.this);
                    publishDialog.setTitle(R.string.title);
                    publishDialog.setMessage(R.string.publish_message);
                    publishDialog.setPositiveButton(R.string.publish, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Map<String, String> params = new HashMap<>();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mWebView.getWindowToken(), 0);
                            params.put("title", title);
                            params.put("author", getSharedPreferences("User", Context.MODE_PRIVATE).getString("userId", null));
                            params.put("category", category.toString());
                            params.put("html", html);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    publishNews(params);
                                }
                            });
                        }
                    });
                    publishDialog.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });

                    publishDialog.show();
                }
            };

    private void publishNews(Map<String, String> params) {

        if (dialog == null) {
            dialog = new ProgressDialog(PublishNewsActivity.this);
        }
        //设置进度条风格，风格为圆形，旋转的
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置ProgressDialog 标题
        dialog.setTitle("发布中");
        //设置ProgressDialog 提示信息
        dialog.setMessage("发布进度");
        //设置ProgressDialog的最大进度
        dialog.setMax(100);
        dialog.setProgress(0);
        dialog.setCancelable(false);
        if (!dialog.isShowing()) {
            dialog.show();
        }

        OkHttpUtil.postFile(Constants.upload, new ProgressListener() {
            @Override
            public void onProgress(long currentBytes, long contentLength, boolean done) {

                LogUtil.d("Publish", currentBytes + "/" + contentLength);
                int progress = (int) (currentBytes * 100 / contentLength);

                dialog.setProgress(progress);
            }
        }, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //发布失败
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage(getString(R.string.network_error));
                    }
                });
                dialog.setCancelable(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ResponseResult<News> result = new Gson().fromJson(response.body().string(), new TypeToken<ResponseResult<News>>() {
                    }.getType());
                    int status = result.getStatus();
                    final String message = result.getMessage();
                    //成功之后的操作
                    news = result.getResult();

                    //发布成功
                    if (status == Constants.SUCCESS) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(message);
                                dialog.dismiss();

                                View shareView = getLayoutInflater().inflate(R.layout.share_dialog, (ViewGroup) findViewById(R.id.ll_qrcode));
                                ImageView shareImage = shareView.findViewById(R.id.QRCode);
                                final Bitmap share = ZXingUtils.createQRImage(PREFIX + news.getNewsId());
                                shareImage.setImageBitmap(share);
                                new AlertDialog.Builder(PublishNewsActivity.this)
                                        .setView(shareView)
                                        .setTitle("将新闻分享给你的好友")
                                        .setPositiveButton("分享", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (ContextCompat.checkSelfPermission(PublishNewsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                                        || ContextCompat.checkSelfPermission(PublishNewsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(PublishNewsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                                } else {
                                                    share();
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(PublishNewsActivity.this, NewsContentActivity.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("news", news);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                                PublishNewsActivity.this.finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            }
                        });

                    } else {
                        //发布失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage(getString(R.string.server_error));
                            }
                        });
                        dialog.setCancelable(true);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage(getString(R.string.server_error));
                        }
                    });
                    dialog.setCancelable(true);
                }
            }
        }, params, files);
    }

    private void share() {
        ShareEntity shareBean = new ShareEntity(news.getNewsTitle(), ((NewsCategory) newsCategory.getSelectedItem()).getCategoryName());
        shareBean.setUrl(news.getNewsUrl()); //分享链接
        String filePath = ShareUtil.saveBitmapToSDCard(PublishNewsActivity.this, ZXingUtils.createQRImage(PREFIX + news.getNewsId()));
        shareBean.setImgUrl(filePath);
        ShareUtil.showShareDialog(PublishNewsActivity.this, shareBean, ShareConstant.REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    share();
                }else {
                    Toast.makeText(PublishNewsActivity.this,"您尚未授予读写储存",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @OnClick(R.id.iv_action_undo)
    void onClickUndo() {
        mRichEditorAction.undo();
    }

    @OnClick(R.id.iv_action_redo)
    void onClickRedo() {
        mRichEditorAction.redo();
    }

    @OnClick(R.id.iv_get_html)
    void onClickGetHtml() {
        mRichEditorAction.refreshHtml(mRichEditorCallback, onGetHtmlListener);
    }

    @OnClick(R.id.iv_action_txt_color)
    void onClickTextColor() {
        mRichEditorAction.foreColor("blue");
    }

    @OnClick(R.id.iv_action_txt_bg_color)
    void onClickHighlight() {
        mRichEditorAction.backColor("red");
    }

    @OnClick(R.id.iv_action_line_height)
    void onClickLineHeight() {
        mRichEditorAction.lineHeight(20);
    }

    @OnClick(R.id.iv_action_insert_image)
    void onClickInsertImage() {
        PictureSelector.create(PublishNewsActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .imageSpanCount(4)
                .selectionMode(PictureConfig.SINGLE)
                .isCamera(true)
                .setOutputCameraPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())
                .enableCrop(true)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
                .freeStyleCropEnabled(true)
                .circleDimmedLayer(false)
                .scaleEnabled(true)
                .rotateEnabled(true)
                .showCropFrame(true)
                .showCropGrid(true)
                .cropWH(640, 512)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && data != null
                && requestCode == PictureConfig.CHOOSE_REQUEST) {
            List<LocalMedia> images = PictureSelector.obtainMultipleResult(data);
            if (images != null && !images.isEmpty()) {

                //1.Insert the Base64 String (Base64.NO_WRAP)
                LocalMedia imageItem = images.get(0);
//                mRichEditorAction.insertImageData(imageItem.name,
//                        encodeFileToBase64Binary(imageItem.path));

                //2.Insert the ImageUrl
                if (files == null) {
                    files = new ArrayList<>();
                }

                String path;
                if (imageItem.isCompressed()) {
                    path = imageItem.getCompressPath();
                } else {
                    path = imageItem.getCutPath();
                }

                File file = new File(path);
                if (!files.contains(file)) {
                    files.add(file);
                }
                mRichEditorAction.insertImageUrl(path);
            }
        } else if (requestCode == ShareConstant.REQUEST_CODE) {
            Intent intent = new Intent(PublishNewsActivity.this, NewsContentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("news", news);
            intent.putExtras(bundle);
            startActivity(intent);
            PublishNewsActivity.this.finish();
        }
    }

    private static String encodeFileToBase64Binary(String filePath) {
        byte[] bytes = FileIOUtil.readFile2BytesByStream(filePath);
        byte[] encoded = Base64.encode(bytes, Base64.NO_WRAP);
        return new String(encoded);
    }

    @OnClick(R.id.iv_action_insert_link)
    void onClickInsertLink() {
        KeyboardUtils.hideSoftInput(PublishNewsActivity.this);
        final EditHyperlinkPopWindow mPop = new EditHyperlinkPopWindow(this);
        mPop.setOnOkClickListener(new EditHyperlinkPopWindow.OnOkClickListener() {
            @Override
            public void onHyperlinkOK(String address, String text) {
                if (TextUtils.isEmpty(address) || TextUtils.isEmpty(text)) {
                    Toast.makeText(PublishNewsActivity.this, "你还没有输入呢", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mRichEditorAction.createLink(text, address);
                    mPop.dismiss();
                }
            }
        });
        mPop.showAtLocation(findViewById(R.id.fl_container), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @OnClick(R.id.iv_action_table)
    void onClickInsertTable() {
        KeyboardUtils.hideSoftInput(PublishNewsActivity.this);
        final EditTablePopWindow mPop = new EditTablePopWindow(this);
        mPop.setOnOkClickListener(new EditTablePopWindow.OnOkClickListener() {
            @Override
            public void onTableOK(int rows, int cols) {
                if (rows == 0 || cols == 0) {
                    Toast.makeText(PublishNewsActivity.this, "请正确输入行列数", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mRichEditorAction.insertTable(rows, cols);
                    mPop.dismiss();
                }
            }
        });
        mPop.showAtLocation(findViewById(R.id.fl_container), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
        if (flAction.getVisibility() == View.INVISIBLE) {
            flAction.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
        dialog = null;
        files.clear();
    }


    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        isKeyboardShowing = height > 0;
        if (height != 0) {
            flAction.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = flAction.getLayoutParams();
            params.height = height;
            flAction.setLayoutParams(params);
            performInputSpaceAndDel();
        } else if (flAction.getVisibility() != View.VISIBLE) {
            flAction.setVisibility(View.GONE);
        }
    }

    //TODO not a good solution
    private void performInputSpaceAndDel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                } catch (InterruptedException e) {
                    LogUtil.e(TAG, "错误" + e);
                }
            }
        }).start();
    }

    class MRichEditorCallback extends RichEditorCallback {

        @Override
        public void notifyFontStyleChange(ActionType type, final String value) {
            ActionImageView actionImageView =
                    (ActionImageView) llActionBarContainer.findViewWithTag(type);
            if (actionImageView != null) {
                actionImageView.notifyFontStyleChange(type, value);
            }

            if (mEditorMenuFragment != null) {
                mEditorMenuFragment.updateActionStates(type, value);
            }
        }
    }

    public class MOnActionPerformListener implements OnActionPerformListener {
        private RichEditorAction mRichEditorAction;

        public MOnActionPerformListener(RichEditorAction mRichEditorAction) {
            this.mRichEditorAction = mRichEditorAction;
        }

        @Override
        public void onActionPerform(ActionType type, Object... values) {
            if (mRichEditorAction == null) {
                return;
            }

            String value = null;
            if (values != null && values.length > 0) {
                value = (String) values[0];
            }

            switch (type) {
                case SIZE:
                    mRichEditorAction.fontSize(Double.valueOf(value));
                    break;
                case LINE_HEIGHT:
                    mRichEditorAction.lineHeight(Double.valueOf(value));
                    break;
                case FORE_COLOR:
                    mRichEditorAction.foreColor(value);
                    break;
                case BACK_COLOR:
                    mRichEditorAction.backColor(value);
                    break;
                case FAMILY:
                    mRichEditorAction.fontName(value);
                    break;
                case IMAGE:
                    onClickInsertImage();
                    break;
                case LINK:
                    onClickInsertLink();
                    break;
                case TABLE:
                    onClickInsertTable();
                    break;
                case BOLD:
                case ITALIC:
                case UNDERLINE:
                case SUBSCRIPT:
                case SUPERSCRIPT:
                case STRIKETHROUGH:
                case JUSTIFY_LEFT:
                case JUSTIFY_CENTER:
                case JUSTIFY_RIGHT:
                case JUSTIFY_FULL:
                case CODE_VIEW:
                case ORDERED:
                case UNORDERED:
                case INDENT:
                case OUTDENT:
                case BLOCK_QUOTE:
                case BLOCK_CODE:
                case NORMAL:
                case H1:
                case H2:
                case H3:
                case H4:
                case H5:
                case H6:
                case LINE:
                    ActionImageView actionImageView =
                            (ActionImageView) llActionBarContainer.findViewWithTag(type);
                    if (actionImageView != null) {
                        actionImageView.performClick();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
