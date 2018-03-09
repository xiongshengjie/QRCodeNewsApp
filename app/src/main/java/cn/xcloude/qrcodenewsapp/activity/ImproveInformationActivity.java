package cn.xcloude.qrcodenewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import cn.xcloude.qrcodenewsapp.entity.User;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ImproveInformationActivity extends AppCompatActivity {

    private String mobile;

    @BindView(R.id.improve_toolbar)
    Toolbar improveToolbar;
    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_username)
    EditText etUsername;
    @BindView(R.id.rg_sex)
    RadioGroup rgSex;
    @BindView(R.id.tv_register)
    TextView tvRegister;
    @BindView(R.id.et_nickname)
    TextView etNickname;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_re_password)
    EditText etRePassword;
    @BindView(R.id.et_description)
    EditText etDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_improve_information);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mobile = intent.getStringExtra("userMobile");
        initViews();
    }

    private void initViews() {
        setSupportActionBar(improveToolbar);
        improveToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImproveInformationActivity.this.finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        etMobile.setText(mobile);
        etUsername.setText(mobile);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString();
        String nickname = etNickname.getText().toString();
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();
        String description = etDescription.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, R.string.improve_information, Toast.LENGTH_SHORT).show();
        }
        if (!password.equals(rePassword)) {
            Toast.makeText(this, R.string.password_not_same, Toast.LENGTH_SHORT).show();
        }

        RadioButton radioButton = findViewById(rgSex.getCheckedRadioButtonId());
        int sex = rgSex.indexOfChild(radioButton);
        User user = new User(null, username, password, nickname, mobile, sex, description, "");

        OkHttpUtil.register(user, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ImproveInformationActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    ResponseResult<User> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<User>>() {
                    }.getType());

                    final String message = serverResponse.getMessage();
                    if (serverResponse.getStatus() == Constants.SUCCESS) {
                        //注册成功
                        Intent intent = new Intent(ImproveInformationActivity.this, MainActivity.class);
                        startActivity(intent);
                        ImproveInformationActivity.this.finish();
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
                                Toast.makeText(ImproveInformationActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImproveInformationActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
