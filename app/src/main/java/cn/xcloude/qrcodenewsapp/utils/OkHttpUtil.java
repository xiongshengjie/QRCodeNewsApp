package cn.xcloude.qrcodenewsapp.utils;

import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.xcloude.qrcodenewsapp.constant.Constants;
import cn.xcloude.qrcodenewsapp.entity.User;
import cn.xcloude.qrcodenewsapp.interfaces.ProgressListener;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2018/2/21.
 */

public class OkHttpUtil {

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS);

    //发布新闻接口
    public static void postFile(String url, final ProgressListener listener, Callback callback, Map<String, String> params, List<File> files) {

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
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("userNickname", user.getUserNickname())
                .addFormDataPart("userName", user.getUserName())
                .addFormDataPart("userMobile", user.getUserMobile())
                .addFormDataPart("userPassword", user.getUserPassword())
                .addFormDataPart("userSex", user.getUserSex().toString())
                .addFormDataPart("userDescription", user.getUserDescription());
        String path = user.getUserHead();
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            builder.addFormDataPart("headFile", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(Constants.userRegister).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    //修改
    public static void update(User user, boolean isModifyHead, Callback callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("userNickname", user.getUserNickname())
                .addFormDataPart("userName", user.getUserName())
                .addFormDataPart("userMobile", user.getUserMobile())
                .addFormDataPart("userPassword", user.getUserPassword())
                .addFormDataPart("userSex", user.getUserSex().toString())
                .addFormDataPart("userDescription", user.getUserDescription())
                .addFormDataPart("userId",user.getUserId())
                .addFormDataPart("userHead",user.getUserHead());
        if (isModifyHead) {
            File file = new File(user.getUserHead());
            builder.addFormDataPart("headFile", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(Constants.userUpdate).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    public static void listNews(int category, int pageNum, int pageCount, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("category", category + "").add("pageNum", pageNum + "").add("pageCount", pageCount + "").build();
        Request request = new Request.Builder().url(Constants.listNews).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    public static void getNewsById(String id, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("id", id).build();
        Request request = new Request.Builder().url(Constants.getNewsById).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    public static void listNewsByUser(String id, int pageNum, int pageCount, Callback callback) {
        RequestBody requestBody = new FormBody.Builder().add("userId", id).add("pageNum", pageNum + "").add("pageCount", pageCount + "").build();
        Request request = new Request.Builder().url(Constants.listNewsByUser).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }

    public static void delNews(String id , Callback callback){
        RequestBody requestBody = new FormBody.Builder().add("newsId",id).build();
        Request request = new Request.Builder().url(Constants.delNews).post(requestBody).build();
        okHttpClient.build().newCall(request).enqueue(callback);
    }
}
