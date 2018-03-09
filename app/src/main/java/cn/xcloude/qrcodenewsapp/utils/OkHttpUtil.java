package cn.xcloude.qrcodenewsapp.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.User;
import cn.xcloude.qrcodenewsapp.interfaces.ProgressListener;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/2/21.
 */

public class OkHttpUtil {

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS);

    //发布新闻接口
    public static void postFile(String url, final ProgressListener listener, Callback callback, Map<String, String> params, List<File> files) {

        okHttpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .method(original.method(), new ProgressRequestBody(original.body(), listener))
                        .build();
                return chain.proceed(request);
            }
        });

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (File file : files) {
            builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }

        for (String key : params.keySet()) {
            builder.addFormDataPart(key, params.get(key));
        }

        MultipartBody multipartBody = builder.build();

        Request request = new Request.Builder().url(url).post(new ProgressRequestBody(multipartBody, listener)).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //获取分类接口
    public static void getAllCategory(Callback callback) {
        Request request = new Request.Builder().url(Constants.getAllCategory).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //登录
    public static void login(String userName, String passWord, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("userName", userName).add("passWord", passWord).build();
        Request request = new Request.Builder().url(Constants.login).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //获取验证码
    public static void getSmsCode(String mobile, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("userMobile", mobile).build();
        Request request = new Request.Builder().url(Constants.getSmsCode).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //验证验证码
    public static void checkSmsCode(String mobile, String smsCode, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("userMobile", mobile).add("smsCode", smsCode).build();
        Request request = new Request.Builder().url(Constants.checkSmsCode).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //注册
    public static void register(User user, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("userNickname", user.getUserNickname()).add("userName", user.getUserName()).add("userMobile", user.getUserMobile()).add("userPassword", user
                .getUserPassword()).add("userSex", user.getUserSex().toString()).add("userDescription", user.getUserDescription()).add("userHead", user.getUserHead()).build();
        Request request = new Request.Builder().url(Constants.userRegister).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }
}
