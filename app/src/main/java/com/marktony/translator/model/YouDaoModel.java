package com.marktony.translator.model;

import android.support.annotation.Nullable;

import java.util.List;

public class YouDaoModel {

    private String errorCode;

    private String query;

    private String[] translation;

    @Nullable
    private Basic basic;

    private List<Web> web;

    private String tSpeakUrl;

    private String speakUrl;

    public String getSpeakUrl() {
        return speakUrl;
    }

    public void setSpeakUrl(String speakUrl) {
        this.speakUrl = speakUrl;
    }

    public String gettSpeakUrl() {
        return tSpeakUrl;
    }

    public void settSpeakUrl(String tSpeakUrl) {
        this.tSpeakUrl = tSpeakUrl;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] getTranslation() {
        return translation;
    }

    public void setTranslation(String[] translation) {
        this.translation = translation;
    }

    @Nullable
    public Basic getBasic() {
        return basic;
    }

    public void setBasic(@Nullable Basic basic) {
        this.basic = basic;
    }

    public List<Web> getWeb() {
        return web;
    }

    public void setWeb(List<Web> web) {
        this.web = web;
    }

    public class Basic{
        @Nullable
        private String phonetic;

        @Nullable
        private String[] explains;

        @Nullable
        public String getPhonetic() {
            return phonetic;
        }

        public void setPhonetic(@Nullable String phonetic) {
            this.phonetic = phonetic;
        }

        public String[] getExplains() {
            return explains;
        }

        public void setExplains(@Nullable String[] explains) {
            this.explains = explains;
        }
    }

    public class Web{

        private String[] value;

        private String key;

        public String[] getValue() {
            return value;
        }

        public void setValue(String[] value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
