package com.calamus.easykorean.models;

public class CategoryModel {
    String cate;
    String pic;
    String code;
    String eCode;

    public CategoryModel(String cate,String code, String pic,String eCode) {
        this.cate = cate;
        this.pic = pic;
        this.code=code;
        this.eCode=eCode;
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String geteCode() {
        return eCode;
    }

    public void seteCode(String eCode) {
        this.eCode = eCode;
    }
}
