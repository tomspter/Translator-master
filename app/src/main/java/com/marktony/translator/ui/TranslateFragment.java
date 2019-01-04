package com.marktony.translator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.marktony.translator.R;
import com.marktony.translator.adapter.SampleAdapter;
import com.marktony.translator.constant.Constants;
import com.marktony.translator.db.DBUtil;
import com.marktony.translator.db.NotebookDatabaseHelper;
import com.marktony.translator.model.BingModel;
import com.marktony.translator.model.YouDaoModel;
import com.marktony.translator.util.MD5Util;
import com.marktony.translator.util.NetworkUtil;
import com.wang.avi.AVLoadingIndicatorView;
import com.youdao.ocr.online.ImageOCRecognizer;
import com.youdao.ocr.online.OCRListener;
import com.youdao.ocr.online.OCRParameters;
import com.youdao.ocr.online.OCRResult;
import com.youdao.ocr.online.OcrErrorCode;
import com.youdao.ocr.online.RecognizeLanguage;
import com.youdao.sdk.app.YouDaoApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.marktony.translator.R.xml.settings_screen;


public class TranslateFragment extends Fragment {

    private EditText editText;
    private TextView textViewClear;
    private AVLoadingIndicatorView progressBar;
    private TextView textViewResult;
    private ImageView imageViewMark;
    private ImageView imageViewSound;
    private View viewResult;
    private AppCompatButton button;

    private AppCompatButton buttonOCR;
//    private ArrayList<BingModel.Sample> samples;

    private RecyclerView recyclerView;
//    private SampleAdapter adapter;

    private NotebookDatabaseHelper dbHelper;

    private String result = null;
    private String IU = null;
    private YouDaoModel model;
    private String soundPath;

    private RequestQueue queue;

    private boolean isMarked = false;
    private boolean showWebTran;
    private boolean isChinese;
    private boolean isKorea;


    private static Pattern ISCHINESE = Pattern.compile("[\u4e00-\u9fa5]");
    private static Pattern ISKOREA = Pattern.compile("[\u2E80-\u2FDF\u3040-\u318F\u31A0-\u31BF\u31F0-\u31FF\u3400-\u4DB5\u4E00-\u9FFF\uA960-\uA97F\uAC00-\uD7FF]");

    // empty constructor required
    public TranslateFragment() {

    }

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Volley创建请求队列
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        dbHelper = new NotebookDatabaseHelper(getActivity(), "MyStore.db", null, 1);
        Log.i("SQLite", "数据库创建成功");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initViews(view);

        //在这里进行网络连接的判断，如果没有连接，则进行snackbar的提示
        if (!NetworkUtil.isNetworkConnected(getActivity())) {
            showNoNetwork();
        }


        button.findViewById(R.id.buttonTranslate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!NetworkUtil.isNetworkConnected(getActivity())) {
                    showNoNetwork();
                } else if (editText.getText() == null || editText.getText().length() == 0) {
                    //snackbar提示输入为空
                    Snackbar.make(button, getString(R.string.no_input), Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    //发送输入文字
                    sendReq(editText.getText().toString());

                }
            }
        });

        buttonOCR.findViewById(R.id.buttonOCR).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OCR", "onClick: -----OCR-----start");

                OCRParameters tps = new OCRParameters
                        .Builder()
                        .source("youdaoocr")
                        .timeout(100000)
                        .type(OCRParameters.TYPE_LINE)
                        .lanType(RecognizeLanguage.LINE_CHINESE_ENGLISH.getCode())
                        .build();

                ImageOCRecognizer.getInstance(tps).recognize(base64,
                        new OCRListener() {

                            @Override
                            public void onResult(OCRResult result,
                                                 String input) {
                                //识别成功
                            }

                            @Override
                            public void onError(OcrErrorCode error) {
                                //识别失败
                            }
                        });
            }

        });


        //清除输入内容
        textViewClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //判断输入完成后显示清除已输入内容
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editText.getEditableText().toString().length() != 0) {
                    textViewClear.setVisibility(View.VISIBLE);
                } else {
                    textViewClear.setVisibility(View.INVISIBLE);
                }

                //TODO
                // 监听回车事件(TextWatcher)
                String str = s.toString().substring(start, start + count);
                if ("\n".equals(str)) {
                    editText.setText(s.toString().replaceFirst("\n", ""));
                    sendReq(editText.getEditableText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imageViewMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 在没有被收藏的情况下
                if (!isMarked) {
                    imageViewMark.setImageResource(R.drawable.ic_grade_white_24dp);
                    Snackbar.make(button, R.string.add_to_notebook, Snackbar.LENGTH_SHORT)
                            .show();
                    isMarked = true;

                    ContentValues values = new ContentValues();
                    values.put("input", model.getQuery());
                    values.put("output", result);
                    //插入收藏词条
                    DBUtil.insertValue(dbHelper, values);

                    values.clear();

                } else {
                    imageViewMark.setImageResource(R.drawable.ic_star_border_white_24dp);
                    Snackbar.make(button, R.string.remove_from_notebook, Snackbar.LENGTH_SHORT)
                            .show();
                    isMarked = false;

                    //删除收藏词条
                    DBUtil.deleteValue(dbHelper, model.getQuery());
                }

            }
        });

        /**
         * 分享词条
         */
        viewResult.findViewById(R.id.image_view_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND).setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, result);
                startActivity(Intent.createChooser(intent, getString(R.string.choose_app_to_share)));
            }
        });

        /**
         * 复制词条到粘贴板
         */
        viewResult.findViewById(R.id.image_view_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", result);
                manager.setPrimaryClip(clipData);
                if (manager.hasPrimaryClip()) {
                    Snackbar.make(button, R.string.copy_done, Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Snackbar.make(button, R.string.copy_notdone, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        });

        /**
         * 单词发音
         */
        viewResult.findViewById(R.id.image_view_sound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i("sound", "onClick:success!!!");
                final MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(soundPath);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            Log.i("sound", "onPrepared: 播放成功");
                        }
                    });
                } catch (IOException e) {
                    Log.i("sound", "onClick: 播放失败");
                    e.printStackTrace();
                } finally {
                    mediaPlayer.stop();
                }
            }
        });

        return view;
    }

    private void initViews(View view) {

        editText = view.findViewById(R.id.et_main_input);
        textViewClear = view.findViewById(R.id.tv_clear);
        // 初始化清除按钮，当没有输入时是不可见的
        textViewClear.setVisibility(View.INVISIBLE);

        progressBar = view.findViewById(R.id.progress_bar);

        viewResult = view.findViewById(R.id.include);
        textViewResult = view.findViewById(R.id.text_view_output);
        imageViewMark = view.findViewById(R.id.image_view_mark_star);
        imageViewMark.setImageResource(R.drawable.ic_star_border_white_24dp);

        imageViewSound = view.findViewById(R.id.image_view_sound);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);

        button = view.findViewById(R.id.buttonTranslate);
        buttonOCR = view.findViewById(R.id.buttonOCR);
    }

    /**
     * 发送请求数据
     *
     * @param in
     */
    public void sendReq(String in) {

        progressBar.setVisibility(View.VISIBLE);
        viewResult.setVisibility(View.INVISIBLE);

        // 监听输入面板的情况，如果激活则隐藏
        //调取android虚拟键盘
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        //点击按钮，隐藏android虚拟键盘
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(button.getWindowToken(), 0);
        }

        in = inputFormat(in);

        //判断是否含有中文
        isChinese = isChinese(in);
        isKorea = isKorea(in);

//      String url = Constants.BING_BASE + "?Word=" + in + "&Samples=";

        String sign = MD5Util.getMD5(Constants.YOUDAO_APPID + in + Constants.salt + Constants.YOUDAO_KEY);


        // http://openapi.youdao.com/api?q= good &from=EN&to=zh_CHS&appKey =ff889495-4b45-46d9-8f48-946554334f2a &salt=2 &sign=1995882C5064805BC30A39829B779D7B


        String url = Constants.YOUDAO_URL + in + Constants.TRAN_AUTO + Constants.YOUDAO_APPID + "&salt=" + Constants.salt + "&sign=" + sign;

        Log.i("url", "sendReq: ---url---" + url);

//        if (showSamples) {
//            url += "true";
//        } else {
//            url += "false";
//        }

        Log.i("webTran", "onResponse: " + showWebTran);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            Gson gson = new Gson();
                            //解析
                            model = gson.fromJson(s, YouDaoModel.class);

                            if (model != null) {
                                result = model.getQuery() + "\n";
//                                Log.i("youdao", "onResponse:---in---"+model.getQuery());

                                //单词本功能
                                if (DBUtil.queryIfItemExist(dbHelper, model.getQuery())) {
                                    imageViewMark.setImageResource(R.drawable.ic_grade_white_24dp);
                                    isMarked = true;
                                } else {
                                    imageViewMark.setImageResource(R.drawable.ic_star_border_white_24dp);
                                    isMarked = false;
                                }

                                //音标显示
                                if ((model.getBasic() != null ? model.getBasic().getPhonetic() : null) != null) {
                                    String p = model.getBasic().getPhonetic();
//                                    result = result + "\nAmE:" + p.getAmE() + "\nBrE:" + p.getBrE() + "\n";
                                    result = result + "\n音标： [ " + p + " ]\n\n";
                                } else {
                                    result = result + "\n";
                                }

                                //获取声音路径
                                if (model.getSpeakUrl() != null && model.gettSpeakUrl() != null) {
                                    if (!isChinese) {
                                        //无中文
                                        soundPath = model.getSpeakUrl();
                                        Log.i("url", "onResponse:---speakUrlT---" + soundPath);
                                    } else {
                                        //有中文
                                        soundPath = model.gettSpeakUrl();
                                        Log.i("url", "onResponse:---speakUrl---" + soundPath);
                                        //soundPath = pronunciation.getAmEmp3();
                                    }
                                }
                                //TODO  需要判空
//                                for (BingModel.Definition def : model.getDefs()) {
//                                    result = result + def.getPos() + "\n" + def.getDef() + "\n";
//                                }

                                //TODO translation[]
                                if ((model.getBasic() != null ? model.getBasic().getExplains() : null) != null) {
                                    for (String def : model.getBasic().getExplains()) {
                                        result = result + def + "\n";
                                    }
                                }
                                result = result.substring(0, result.length() - 1);

                                //有中文时开启translation[]
                                if (isChinese) {
                                    for (String def : model.getTranslation()) {
                                        result = result + "\n" + def;
                                    }
                                }

                                //有韩文时开启translation[]
                                if (isKorea) {
                                    for (String def : model.getTranslation()) {
                                        result = result + "\n" + def;
                                    }
                                }


                                //显示网络释义
                                if (showWebTran) {
                                    Log.i("webTran", "onResponse: " + showWebTran);

                                    if (model.getWeb().size() != 0 && model.getWeb() != null) {
                                        result = result + "\n网络释义:\n";
                                        for (YouDaoModel.Web web : model.getWeb()) {
//                                            for (String webDef : web.getValue()) {
//                                                Map resultMap=new HashMap();
//                                                resultMap.put(web,webDef);
//                                                Iterator iterator=resultMap.entrySet().iterator();
//                                                while (iterator.hasNext()){
//                                                    Map.Entry entry= (Map.Entry) iterator.next();
//                                                    result=result+entry.getKey()+entry.getValue();
//                                                }
                                            result = result + web.getKey() + "\n" + Arrays.toString(web.getValue()) + "\n";
                                            Log.i("webTran", "onResponse: ");
                                        }
                                    }
                                }

                                //例句显示
//                                if (model.getSams() != null && model.getSams().size() != 0) {
//
//                                    if (samples == null) {
//                                        samples = new ArrayList<>();
//                                    }
//
//                                    samples.clear();
//
//                                    samples.addAll(model.getSams());
//
//                                    if (adapter == null) {
//                                        adapter = new SampleAdapter(getActivity(), samples);
//                                        recyclerView.setAdapter(adapter);
//                                    } else {
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                }

                                progressBar.setVisibility(View.INVISIBLE);
                                viewResult.setVisibility(View.VISIBLE);

                                textViewResult.setText(result);
                            } else {
                                result = "没有查询结果";
                                progressBar.setVisibility(View.INVISIBLE);
                                viewResult.setVisibility(View.VISIBLE);
                                textViewResult.setText(result);
                            }
                        } catch (
                                JsonSyntaxException ex)

                        {
                            showTransError();
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progressBar.setVisibility(View.GONE);
                showTransError();
            }
        });

        queue.add(request);

    }

    /**
     * 去除输入的回车
     *
     * @param in
     * @return
     */
    private String inputFormat(String in) {
        in = in.replace("\n", "");
        return in;
    }

    private void showNoNetwork() {
        Snackbar.make(button, R.string.no_network_connection, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //打开网络设置
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    /**
     * 显示网络错误Snackbar
     */
    private void showTransError() {
        Snackbar.make(button, R.string.trans_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    /**
     * 判断输入是否含有中文
     *
     * @param in
     * @return
     */
    public static boolean isChinese(String in) {
        Pattern pattern = ISCHINESE;
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断输入是否含有韩文
     *
     * @param in
     * @return
     */
    public static boolean isKorea(String in) {
        Pattern pattern = ISKOREA;
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 活动准备好后与用户交互时调用
     */
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        showWebTran = sp.getBoolean("showWebTran", false);
//        if (samples != null) {
//            samples.clear();
//        }
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
    }
}


/*
NOTE：
一个Activity启动另一个Activity: onPause()->onStop(),再返回：onRestart()->onStart()->onResume()
程序按back 退出： onPause()->onStop()->onDestory(),再进入：onCreate()->onStart()->onResume();
程序按home 退出： onPause()->onStop(),再进入：onRestart()->onStart()->onResume();
 */