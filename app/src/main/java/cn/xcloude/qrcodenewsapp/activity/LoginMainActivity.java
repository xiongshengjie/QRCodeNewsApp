package cn.xcloude.qrcodenewsapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.entity.User;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginMainActivity extends AppCompatActivity {

    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.tv_login)
    TextView tvLogin;
    @BindView(R.id.iv_logo)
    ImageView ivLogo;
    @BindView(R.id.tv_none)
    TextView tvNone;
    @BindView(R.id.et_login_name)
    EditText loginName;
    @BindView(R.id.et_login_password)
    EditText loginPassword;

    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        ButterKnife.bind(this);
        initViews();
        initAnims();
    }

    private void initViews() {
        tvNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                String username = loginName.getText().toString();
                String password = loginPassword.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(LoginMainActivity.this, R.string.not_input_username, Toast.LENGTH_SHORT).show();
                    loginName.requestFocus();
                    imm.showSoftInput(loginName, 0);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginMainActivity.this, R.string.not_input_password, Toast.LENGTH_SHORT).show();
                    loginPassword.requestFocus();
                    imm.showSoftInput(loginPassword, 0);
                    return;
                }

                if(dialog == null){
                    dialog = new ProgressDialog(LoginMainActivity.this,ProgressDialog.STYLE_SPINNER);
                }

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();


                OkHttpUtil.login(username, password, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginMainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        dialog.dismiss();
                        if(response.code() == 200) {
                            Gson gson = new Gson();
                            ResponseResult<User> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<User>>() {
                            }.getType());
                            int statu = serverResponse.getStatus();
                            final String message = serverResponse.getMessage();
                            if (statu == Constants.SUCCESS) {
                                Intent intent = new Intent(LoginMainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                User user = serverResponse.getResult();
                                SharedPreferences userShared = getSharedPreferences("User", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = userShared.edit();
                                editor.putString("userId", user.getUserId());
                                editor.putString("userName", user.getUserName());
                                editor.putString("userPassWord", user.getUserPassword());
                                editor.putString("userNickname", user.getUserNickname());
                                editor.putString("userMobile", user.getUserMobile());
                                editor.putInt("userSex", user.getUserSex());
                                editor.putString("userDescription", user.getUserDescription());
                                editor.putString("userHead", user.getUserHead());
                                editor.commit();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginMainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginMainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginMainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAnims() {
        //初始化底部注册、登录的按钮动画
        //以控件自身所在的位置为原点，从下方距离原点200像素的位置移动到原点
        ObjectAnimator tranLogin = ObjectAnimator.ofFloat(tvLogin, "translationY", 200, 0);
        ObjectAnimator tranRegister = ObjectAnimator.ofFloat(tvRegister, "translationY", 200, 0);
        ObjectAnimator tranNone = ObjectAnimator.ofFloat(tvNone, "translationY", 200, 0);
        ObjectAnimator tranName = ObjectAnimator.ofFloat(loginName, "translationY", 200, 0);
        ObjectAnimator tranPassword = ObjectAnimator.ofFloat(loginPassword, "translationY", 200, 0);
        //将注册、登录的控件alpha属性从0变到1
        ObjectAnimator alphaLogin = ObjectAnimator.ofFloat(tvLogin, "alpha", 0, 1);
        ObjectAnimator alphaRegister = ObjectAnimator.ofFloat(tvRegister, "alpha", 0, 1);
        ObjectAnimator alphaNone = ObjectAnimator.ofFloat(tvNone, "alpha", 0, 1);
        ObjectAnimator alphaName = ObjectAnimator.ofFloat(loginName, "alpha", 0, 1);
        ObjectAnimator alphaPassword = ObjectAnimator.ofFloat(loginPassword, "alpha", 0, 1);
        final AnimatorSet bottomAnim = new AnimatorSet();
        bottomAnim.setDuration(1000);
        //同时执行控件平移和alpha渐变动画
        bottomAnim.play(tranLogin).with(tranRegister).with(alphaLogin).with(alphaRegister).with(tranNone).with(alphaNone).with(tranName).with(alphaName).with(tranPassword).with(alphaPassword);

        //获取屏幕高度
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        //通过测量，获取ivLogo的高度
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        ivLogo.measure(w, h);
        int logoHeight = ivLogo.getMeasuredHeight();

        //初始化ivLogo的移动和缩放动画
        float transY = (screenHeight - logoHeight) * 0.28f;
        //ivLogo向上移动 transY 的距离
        ObjectAnimator tranLogo = ObjectAnimator.ofFloat(ivLogo, "translationY", 0, -transY);
        //ivLogo在X轴和Y轴上都缩放0.75倍
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(ivLogo, "scaleX", 1f, 0.75f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(ivLogo, "scaleY", 1f, 0.75f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.setDuration(1000);
        logoAnim.play(tranLogo).with(scaleXLogo).with(scaleYLogo);
        logoAnim.start();
        logoAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //待ivLogo的动画结束后,开始播放底部注册、登录按钮的动画
                bottomAnim.start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //实现只在冷启动时显示启动页，即点击返回键与点击HOME键退出效果一致
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
