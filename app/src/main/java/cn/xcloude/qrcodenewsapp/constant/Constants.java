package cn.xcloude.qrcodenewsapp.constant;

/**
 * Created by Administrator on 2018/2/22.
 */

public final class Constants {

    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final int PAGESIZE = 10;

    public static final int ERROR = 4000;
    public static final int SUCCESS = 2000;
    public static final int SERVER_ERROR = 5000;

    public static final long SMS_OUTOFDATE= 120500L;

    public static final String PREFIX = "msxw://";

    //public static String baseUrl = "https://www.xcloude.cn/QRCodeNews";
    public static final String baseUrl = "http://192.168.0.102:8080/QRCodeNews";
//    public static final String baseUrl = "http://10.4.0.217:8080/QRCodeNews";

    public static final String upload = baseUrl + "/news/publish";
    public static final String getAllCategory = baseUrl + "/category/getAllCategory";
    public static final String login = baseUrl + "/user/login";
    public static final String getSmsCode = baseUrl + "/user/getSmsCode";
    public static final String checkSmsCode = baseUrl + "/user/checkSmsCode";
    public static final String userRegister = baseUrl + "/user/register";
    public static final String listNews = baseUrl + "/news/list";
    public static final String getNewsById = baseUrl + "/news/getNewsById";
    public static final String listNewsByUser = baseUrl + "/news/listNewsByUser";
}
