package cn.xcloude.qrcodenewsapp.entity;

import java.io.Serializable;
import java.util.Date;

public class News implements Serializable{
    private String newsId;

    private String newsTitle;

    private String newsUrl;

    private String newsAuthor;

    private String newsImg;

    private Integer newsCategory;

    public News(String newsId, String newsTitle, String newsUrl, String newsAuthor, String newsImg, Integer newsCategory) {
        this.newsId = newsId;
        this.newsTitle = newsTitle;
        this.newsUrl = newsUrl;
        this.newsAuthor = newsAuthor;
        this.newsImg = newsImg;
        this.newsCategory = newsCategory;
    }

    public News() {
        super();
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId == null ? null : newsId.trim();
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle == null ? null : newsTitle.trim();
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl == null ? null : newsUrl.trim();
    }

    public String getNewsAuthor() {
        return newsAuthor;
    }

    public void setNewsAuthor(String newsAuthor) {
        this.newsAuthor = newsAuthor == null ? null : newsAuthor.trim();
    }

    public String getNewsImg() {
        return newsImg;
    }

    public void setNewsImg(String newsImg) {
        this.newsImg = newsImg == null ? null : newsImg.trim();
    }

    public Integer getNewsCategory() {
        return newsCategory;
    }

    public void setNewsCategory(Integer newsCategory) {
        this.newsCategory = newsCategory;
    }
}