package cn.xcloude.qrcodenewsapp.constant;

/**
 * Created by Administrator on 2018/2/22.
 */

public final class Constants {

    public static final String STATUS = "status";
    public static final String MESSAGE = "message";

    public static final int ERROR = 4000;
    public static final int SUCCESS = 2000;
    public static final int SERVER_ERROR = 5000;

    //public static String baseUrl = "https://www.xcloude.cn/QRCodeNews";
    public static final String baseUrl = "http://192.168.1.100:8080/QRCodeNews";

    public static final String uploadUrl = baseUrl + "/news/publish";
}
