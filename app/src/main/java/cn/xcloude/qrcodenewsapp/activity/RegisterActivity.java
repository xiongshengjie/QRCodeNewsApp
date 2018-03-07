package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_toolbar)
    Toolbar registerToolbar;
    @BindView(R.id.tv_get_sms_code)
    TextView tvGetSmsCode;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.et_mobile)
    TextView etMobile;
    @BindView(R.id.et_sms_code)
    TextView etSmsCode;

    private TimeCount timeCount;
    private long remainTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(registerToolbar);
        registerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.this.finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        tvGetSmsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = etMobile.getText().toString();
                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(RegisterActivity.this, R.string.not_input_mobile, Toast.LENGTH_SHORT).show();
                    return;
                }
                OkHttpUtil.getSmsCode(mobile, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            Gson gson = new Gson();
                            ResponseResult<Object> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<Object>>() {
                            }.getType());

                            final String message = serverResponse.getMessage();
                            if (serverResponse.getStatus() == Constants.SUCCESS) {
                                //申请验证码成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        timeCount = new TimeCount(Constants.SMS_OUTOFDATE, 1000);
                                        timeCount.start();
                                    }
                                });
                            } else {
                                //申请验证码失败
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
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
                final String mobile = etMobile.getText().toString();
                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(RegisterActivity.this, R.string.not_input_mobile, Toast.LENGTH_SHORT).show();
                    return;
                }
                String smsCode = etSmsCode.getText().toString();
                if (TextUtils.isEmpty(smsCode)) {
                    Toast.makeText(RegisterActivity.this, R.string.not_input_sms_code, Toast.LENGTH_SHORT).show();
                    return;
                }

                OkHttpUtil.checkSmsCode(mobile, smsCode, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            Gson gson = new Gson();
                            ResponseResult<Object> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<Object>>() {
                            }.getType());

                            final String message = serverResponse.getMessage();
                            if (serverResponse.getStatus() == Constants.SUCCESS) {
                                //验证码正确
                                Intent intent = new Intent(RegisterActivity.this, ImproveInformationActivity.class);
                                intent.putExtra("userMobile", mobile);
                                startActivity(intent);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        SharedPreferences preferences = getSharedPreferences("RemainTime", Context.MODE_PRIVATE);
        remainTime = preferences.getLong("remainTime", 0);
        long lastCurrentTime = preferences.getLong("currentTime", 0);
        if (remainTime > 0 && lastCurrentTime > 0) {
            long currentTime = System.currentTimeMillis();
            if (remainTime > (currentTime - lastCurrentTime) / 1000) {
                remainTime -= (currentTime - lastCurrentTime) / 1000;
                timeCount = new TimeCount(remainTime * 1000, 1000);
                timeCount.start();
            } else {
                remainTime = 0;
            }
        }

    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //计时过程显示
        @Override
        public void onTick(long millisUntilFinished) {
            remainTime = millisUntilFinished / 1000;
            tvGetSmsCode.setText(remainTime + "");
            tvGetSmsCode.setClickable(false);
            tvGetSmsCode.setBackground(getDrawable(R.drawable.shape_sms_code_clicked));
        }

        //计时完成触发
        @Override
        public void onFinish() {
            tvGetSmsCode.setText(R.string.get_sms_code);
            tvGetSmsCode.setClickable(true);
            tvGetSmsCode.setBackground(getDrawable(R.drawable.shape_sms_code));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (remainTime > 0) {
            SharedPreferences preferences = getSharedPreferences("RemainTime", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("remainTime", remainTime);
            editor.putLong("currentTime", System.currentTimeMillis());
            editor.commit();
        }
        if (timeCount != null) {
            timeCount.cancel();
            timeCount = null;
        }
    }
}
