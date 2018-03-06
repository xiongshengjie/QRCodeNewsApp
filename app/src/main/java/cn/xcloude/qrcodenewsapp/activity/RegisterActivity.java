package cn.xcloude.qrcodenewsapp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.IOException;

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
                    Toast.makeText(RegisterActivity.this, R.string.input_mobile, Toast.LENGTH_SHORT).show();
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
                        Gson gson = new Gson();
                        ResponseResult<Object> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<Object>>() {
                        }.getType());

                        final String message = serverResponse.getMessage();
                        if (serverResponse.getStatus() == Constants.SUCCESS) {
                            //申请验证码成功

                        }else {
                            //申请验证码失败
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}
