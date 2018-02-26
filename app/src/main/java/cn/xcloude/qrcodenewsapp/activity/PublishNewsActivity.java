package cn.xcloude.qrcodenewsapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.even.mricheditor.ActionType;
import com.even.mricheditor.RichEditorAction;
import com.even.mricheditor.RichEditorCallback;
import com.even.mricheditor.ui.ActionImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

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
import cn.xcloude.qrcodenewsapp.fragment.EditHyperlinkPopWindow;
import cn.xcloude.qrcodenewsapp.fragment.EditTablePopWindow;
import cn.xcloude.qrcodenewsapp.fragment.EditorMenuFragment;
import cn.xcloude.qrcodenewsapp.interfaces.OnActionPerformListener;
import cn.xcloude.qrcodenewsapp.interfaces.ProgressListener;
import cn.xcloude.qrcodenewsapp.utils.FileIOUtil;
import cn.xcloude.qrcodenewsapp.utils.GlideImageLoader;
import cn.xcloude.qrcodenewsapp.interfaces.KeyboardHeightObserver;
import cn.xcloude.qrcodenewsapp.utils.KeyboardHeightProvider;
import cn.xcloude.qrcodenewsapp.utils.KeyboardUtils;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressLint("SetJavaScriptEnabled")
public class PublishNewsActivity extends AppCompatActivity implements KeyboardHeightObserver {

    @BindView(R.id.wv_container)
    WebView mWebView;
    @BindView(R.id.fl_action)
    FrameLayout flAction;
    @BindView(R.id.ll_action_bar_container)
    LinearLayout llActionBarContainer;
    @BindView(R.id.news_title)
    EditText newsTitle;

    private ProgressDialog dialog = null;

    private List<File> files = new ArrayList<>();

    /**
     * The keyboard height provider
     */
    private KeyboardHeightProvider keyboardHeightProvider;
    private boolean isKeyboardShowing;
    private String htmlContent = "<p>新闻正文</p>";

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

    private static final int REQUEST_CODE_CHOOSE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_news);
        ButterKnife.bind(this);

        initImageLoader();
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

    /**
     * ImageLoader for insert Image
     */
    private void initImageLoader() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.setShowCamera(true);
        imagePicker.setCrop(false);
        imagePicker.setMultiMode(false);
        imagePicker.setSaveRectangle(true);
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);
        imagePicker.setFocusWidth(800);
        imagePicker.setFocusHeight(800);
        imagePicker.setOutPutX(256);
        imagePicker.setOutPutY(256);
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
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
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
                    if(TextUtils.isEmpty(title)){
                        Toast.makeText(PublishNewsActivity.this, "标题还没写哦", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    final AlertDialog.Builder publishDialog = new AlertDialog.Builder(PublishNewsActivity.this);
                    publishDialog.setTitle(R.string.title);
                    publishDialog.setMessage(R.string.publish_message);
                    publishDialog.setPositiveButton(R.string.publish, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Map<String, String> params = new HashMap<>();
                            params.put("title", title);
                            params.put("author", "admin");
                            params.put("category", "1");
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

        OkHttpUtil.postFile(Constants.uploadUrl, new ProgressListener() {
            @Override
            public void onProgress(long currentBytes, long contentLength, boolean done) {

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
                        dialog.setMessage(getString(R.string.server_error));
                    }
                });
                dialog.setCancelable(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Map<String, Object> result = new Gson().fromJson(response.body().string(), new TypeToken<Map<String, Object>>() {
                }.getType());
                int statu = Integer.parseInt(result.get(Constants.STATUS).toString().substring(0,4));
                final String message = result.get(Constants.MESSAGE).toString();

                //发布成功
                if (statu == Constants.SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage(message);
                        }
                    });
                    dialog.dismiss();
                    //成功之后的操作


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
            }
        }, params, files);
    }

    @OnClick(R.id.iv_get_html)
    void onClickGetHtml() {
        mRichEditorAction.refreshHtml(mRichEditorCallback, onGetHtmlListener);
    }

    @OnClick(R.id.iv_action_undo)
    void onClickUndo() {
        mRichEditorAction.undo();
    }

    @OnClick(R.id.iv_action_redo)
    void onClickRedo() {
        mRichEditorAction.redo();
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
        Intent intent = new Intent(this, ImageGridActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS
                && data != null
                && requestCode == REQUEST_CODE_CHOOSE) {
            ArrayList<ImageItem> images =
                    (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images != null && !images.isEmpty()) {

                //1.Insert the Base64 String (Base64.NO_WRAP)
                ImageItem imageItem = images.get(0);
//                mRichEditorAction.insertImageData(imageItem.name,
//                        encodeFileToBase64Binary(imageItem.path));

                //2.Insert the ImageUrl
                if (files == null) {
                    files = new ArrayList<>();
                }
                File file = new File(imageItem.path);
                if (!files.contains(file)) {
                    files.add(file);
                }
                mRichEditorAction.insertImageUrl(imageItem.path);
            }
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
                    e.printStackTrace();
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
