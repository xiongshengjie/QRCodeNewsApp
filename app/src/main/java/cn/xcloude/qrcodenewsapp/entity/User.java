package cn.xcloude.qrcodenewsapp.entity;

import java.util.Date;

public class User {
    private String userId;

    private String userName;

    private String userPassword;

    private String userNickname;

    private String userMobile;

    private Integer userSex;

    private String userDescription;

    private String userHead;

    private Date createDatetime;

    private Date updateDatetime;

    public User(String userId, String userName, String userPassword, String userNickname, String userMobile, Integer userSex, String userDescription, String userHead, Date createDatetime, Date updateDatetime) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userNickname = userNickname;
        this.userMobile = userMobile;
        this.userSex = userSex;
        this.userDescription = userDescription;
        this.userHead = userHead;
        this.createDatetime = createDatetime;
        this.updateDatetime = updateDatetime;
    }

    public User() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword == null ? null : userPassword.trim();
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname == null ? null : userNickname.trim();
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile == null ? null : userMobile.trim();
    }

    public Integer getUserSex() {
        return userSex;
    }

    public void setUserSex(Integer userSex) {
        this.userSex = userSex;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription == null ? null : userDescription.trim();
    }

    public String getUserHead() {
        return userHead;
    }

    public void setUserHead(String userHead) {
        this.userHead = userHead == null ? null : userHead.trim();
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Date getUpdateDatetime() {
        return updateDatetime;
    }

    public void setUpdateDatetime(Date updateDatetime) {
        this.updateDatetime = updateDatetime;
    }
}