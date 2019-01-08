package com.tomspter.translator.constant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Constants {

    /**
     * 有道词典应用ID
     */
    public static final String YOUDAO_APPID = "074fbb858c59be1f";


    /**
     * 有道词典应用密钥
     */
    public static final String YOUDAO_KEY="PCw9QIUdNgAdjOKdIeZXNjRj8ShVPGO9";

    /**
     * 随机数salt
     */
    public static final String salt="0813";

    /**
     * 有道词典BASE URL
     */
    public static final String YOUDAO_URL = "http://openapi.youdao.com/api?q=";

    /**
     * 翻译方式(自动)
     */
    public static final String TRAN_AUTO ="&from=auto&to=auto&appKey=";


    /**
     * 有道ASR URL
     */
    public static final String YOUDAO_ASRURL="http://openapi.youdao.com/speechtransapi";


    /**
     * 每日词图地址
     */
    public static final String DAILY_SENTENCE = "http://open.iciba.com/dsapi";

    /**
     * 必应词典base url
     * 拼接参数为：
     * 1. Word，必选，可以为单词或者词组，需要encode
     * 2. Sample，可选，例句功能，默认为true，utf-8编码格式
     */
    public static final String BING_BASE = "http://xtk.azurewebsites.net/BingDictService.aspx";

    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传码
    public final static int GALLERY_REQUEST_CODE = 1;
}
