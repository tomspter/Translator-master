package com.tomspter.translator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.tomspter.translator.R;
import com.tomspter.translator.constant.Constants;
import com.tomspter.translator.db.DBUtil;
import com.tomspter.translator.db.NotebookDatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.content.Context.CLIPBOARD_SERVICE;


public class DailyOneFragment extends Fragment {

    private RequestQueue queue;

    private TextView textViewEng;
    private TextView textViewChi;
    private ImageView imageViewMain;
    private ImageView ivStar;
    private ImageView ivCopy;
    private ImageView ivShare;
    private ImageView ivSound;


    private Boolean isMarked = false;

    private NotebookDatabaseHelper dbHelper;

//    private String imageUrl = null;


    private String soundPath=null;


    public DailyOneFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        dbHelper = new NotebookDatabaseHelper(getActivity(), "MyStore.db", null, 1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_one, container, false);

        initViews(view);

        requestData();

        ivStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 在没有被收藏的情况下
                if (!isMarked) {
                    ivStar.setImageResource(R.drawable.ic_grade_white_24dp);
                    Snackbar.make(ivStar, R.string.add_to_notebook, Snackbar.LENGTH_SHORT).show();
                    isMarked = true;

                    ContentValues values = new ContentValues();
                    values.put("input", textViewEng.getText().toString());
                    values.put("output", textViewChi.getText().toString());
                    DBUtil.insertValue(dbHelper, values);

                    values.clear();

                } else {
                    ivStar.setImageResource(R.drawable.ic_star_border_white_24dp);
                    Snackbar.make(ivStar, R.string.remove_from_notebook, Snackbar.LENGTH_SHORT).show();
                    isMarked = false;

                    DBUtil.deleteValue(dbHelper, textViewEng.getText().toString());

                }
            }
        });

        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", String.valueOf(textViewEng.getText() + "\n" + textViewChi.getText()));
                manager.setPrimaryClip(clipData);

                Snackbar.make(ivCopy, R.string.copy_done, Snackbar.LENGTH_SHORT).show();
            }
        });

        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND).setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(textViewEng.getText()) + "\n" + textViewChi.getText());
                startActivity(Intent.createChooser(intent, getString(R.string.choose_app_to_share)));
            }
        });

        ivSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mediaPlayer=new MediaPlayer();
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
                    e.printStackTrace();
                    Log.i("sound", "onClick: 播放失败");
                }finally {
                    mediaPlayer.stop();
                }
            }
        });

        imageViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData();
                Log.i("picture", "onClick: 刷新图片");
            }
        });

        return view;
    }

    private void initViews(View view) {

        textViewEng =view.findViewById(R.id.text_view_eng);
        textViewChi =view.findViewById(R.id.text_view_chi);
        imageViewMain =view.findViewById(R.id.image_view_daily);

        ivStar =view.findViewById(R.id.image_view_mark_star);
        ivCopy =view.findViewById(R.id.image_view_copy);
        ivShare =view.findViewById(R.id.image_view_share);
        ivSound=view.findViewById(R.id.image_view_sound);
    }

    /**
     * 请求图片和文本
     */
    private void requestData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.DAILY_SENTENCE, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {

//                    imageUrl = jsonObject.getString("picture2");

                    //设置时间戳
                    String updateTime = String.valueOf(System.currentTimeMillis());

                    soundPath=jsonObject.getString("tts");

                    //Glide图片加载
                    Glide.with(getActivity())
                            .load(Constants.UNSPLASH_URL)
//                            .crossFade()
                            .asBitmap()
                            .fitCenter()
//                            .centerCrop()
//                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .signature(new StringSignature(updateTime))
                            .error(R.drawable.notfound_picture)
                            .into(imageViewMain);

                    //填充文字
                    textViewEng.setText(jsonObject.getString("content"));
                    textViewChi.setText(jsonObject.getString("note"));

                    if (DBUtil.queryIfItemExist(dbHelper, textViewEng.getText().toString())) {
                        ivStar.setImageResource(R.drawable.ic_grade_white_24dp);
                        isMarked = true;
                    } else {
                        ivStar.setImageResource(R.drawable.ic_star_border_white_24dp);
                        isMarked = false;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("网络请求失败", "onErrorResponse: netWork Failed");
            }
        });

        queue.add(request);
    }

    /**
     * 屏幕状态监听
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imageViewMain.setScaleType(ImageView.ScaleType.CENTER_CROP);
        requestData();

    }


    /**
     * Fragment隐藏
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        imageViewMain.setScaleType(ImageView.ScaleType.CENTER_CROP);
        requestData();
    }
}
