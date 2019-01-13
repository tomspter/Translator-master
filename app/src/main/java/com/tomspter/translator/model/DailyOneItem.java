package com.tomspter.translator.model;


public class DailyOneItem {

    /**
     * 英文内容
     */
    private String content = null;
    /**
     * 中文内容
     */
    private String note = null;
    /**
     * 大图地址
     */
    private String imgUrl = null;

    private int id;
    /**
     * 声音地址
     */
    private String sound=null;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
