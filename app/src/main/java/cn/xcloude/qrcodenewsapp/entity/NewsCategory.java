package cn.xcloude.qrcodenewsapp.entity;

import org.litepal.crud.DataSupport;

import java.util.Date;

public class NewsCategory extends DataSupport{
    private Integer categoryId;

    private String categoryName;

    public NewsCategory(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public NewsCategory() {
        super();
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName == null ? null : categoryName.trim();
    }

    @Override
    public String toString() {
        return categoryName;
    }
}