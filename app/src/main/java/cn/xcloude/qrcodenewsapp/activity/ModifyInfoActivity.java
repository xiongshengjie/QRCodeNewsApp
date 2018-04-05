package cn.xcloude.qrcodenewsapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;
import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.ResponseResult;
import cn.xcloude.qrcodenewsapp.entity.User;
import cn.xcloude.qrcodenewsapp.utils.OkHttpUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ModifyInfoActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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
    @BindView(R.id.pick_head)
    CircleImageView pickHead;

    private String headPath;
    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_info);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initView();
    }

    private void initView(){
        setSupportActionBar(improveToolbar);
        improveToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModifyInfoActivity.this.finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        etMobile.setText(sharedPreferences.getString("userMobile",null));
        etUsername.setText(sharedPreferences.getString("userName",null));
        etPassword.setText(sharedPreferences.getString("userPassWord",null));
        etRePassword.setText(sharedPreferences.getString("userPassWord",null));
        etDescription.setText(sharedPreferences.getString("userDescription",null));
        etNickname.setText(sharedPreferences.getString("userNickname",null));
        RadioButton check = (RadioButton) rgSex.getChildAt(sharedPreferences.getInt("userSex",0));
        check.setChecked(true);
        RequestOptions options = new RequestOptions();
        options.centerCrop();
        Glide.with(this).load(Constants.baseUrl + "/" + sharedPreferences.getString("userHead",null)).apply(options).into(pickHead);

        pickHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(ModifyInfoActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .imageSpanCount(4)
                        .selectionMode(PictureConfig.SINGLE)
                        .isCamera(true)
                        .setOutputCameraPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath())
                        .enableCrop(true)// 是否裁剪 true or false
                        .compress(true)// 是否压缩 true or false
                        .freeStyleCropEnabled(true)
                        .circleDimmedLayer(true)
                        .scaleEnabled(true)
                        .rotateEnabled(false)
                        .showCropFrame(false)
                        .showCropGrid(false)
                        .cropWH(256, 256)
                        .minimumCompressSize(20)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && data != null
                && requestCode == PictureConfig.CHOOSE_REQUEST) {
            List<LocalMedia> images = PictureSelector.obtainMultipleResult(data);
            if (images != null && !images.isEmpty()) {
                LocalMedia imageItem = images.get(0);
                if (imageItem.isCompressed()) {
                    headPath = imageItem.getCompressPath();
                } else {
                    headPath = imageItem.getCutPath();
                }

                //2.Insert the ImageUrl
                if (TextUtils.isEmpty(headPath)) {
                    return;
                }

                RequestOptions options = new RequestOptions();
                options.centerCrop();
                Glide.with(this).load(headPath).apply(options).into(pickHead);
            }
        }
    }

    private void update() {
        String username = etUsername.getText().toString();
        String nickname = etNickname.getText().toString();
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();
        String description = etDescription.getText().toString();
        mobile = etMobile.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, R.string.improve_information, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(rePassword)) {
            Toast.makeText(this, R.string.password_not_same, Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioButton = findViewById(rgSex.getCheckedRadioButtonId());
        int sex = rgSex.indexOfChild(radioButton);
        Boolean isModifyHead = false;
        User user;
        if(headPath == null){
            user = new User(sharedPreferences.getString("userId",null), username, password, nickname, mobile, sex, description, sharedPreferences.getString("userHead",null));
        }else {
            isModifyHead = true;
            user = new User(sharedPreferences.getString("userId",null), username, password, nickname, mobile, sex, description, headPath);
        }

        if (dialog == null) {
            dialog = new ProgressDialog(ModifyInfoActivity.this, ProgressDialog.STYLE_SPINNER);
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("修改中");
        dialog.show();

        OkHttpUtil.update(user,isModifyHead, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ModifyInfoActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dialog.dismiss();
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    ResponseResult<User> serverResponse = gson.fromJson(response.body().string(), new TypeToken<ResponseResult<User>>() {
                    }.getType());

                    final String message = serverResponse.getMessage();
                    if (serverResponse.getStatus() == Constants.SUCCESS) {
                        //修改成功
                        Intent intent = new Intent(ModifyInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        ModifyInfoActivity.this.finish();
                        User user = serverResponse.getResult();
                        editor.putString("userId", user.getUserId());
                        editor.putString("userName", user.getUserName());
                        editor.putString("userPassWord", user.getUserPassword());
                        editor.putString("userNickname", user.getUserNickname());
                        editor.putString("userMobile", user.getUserMobile());
                        editor.putInt("userSex", user.getUserSex());
                        editor.putString("userDescription", user.getUserDescription());
                        editor.putString("userHead", user.getUserHead());
                        editor.apply();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifyInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ModifyInfoActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
