package com.tomspter.translator.ui;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tomspter.translator.R;
import com.tomspter.translator.constant.Constants;
import com.tomspter.translator.model.YouDaoModel;
import com.tomspter.translator.util.MD5Util;
import com.youdao.sdk.app.YouDaoApplication;

import skin.support.SkinCompatManager;
import skin.support.app.SkinCardViewInflater;
import skin.support.constraint.app.SkinConstraintViewInflater;
import skin.support.design.app.SkinMaterialViewInflater;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private Toolbar toolbar;

    private NoteBookFragment noteBookFragment;
    private DailyOneFragment dailyOneFragment;
    private TranslateFragment translateFragment;

    private static final String ACTION_NOTEBOOK = "com.tomspter.translator.notebook";
    private static final String ACTION_DAILY_ONE = "com.tomspter.translator.dailyone";

    //    private BingModel model;
    private YouDaoModel model;
    private String result = null;
    private RequestQueue queue;

    private Boolean showNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        YouDaoApplication.init(this, Constants.YOUDAO_KEY);


        initViews();

        if (savedInstanceState != null) {
            FragmentManager manager = getSupportFragmentManager();
            noteBookFragment = (NoteBookFragment) manager.getFragment(savedInstanceState, "noteBookFragment");
            dailyOneFragment = (DailyOneFragment) manager.getFragment(savedInstanceState, "dailyOneFragment");
            translateFragment = (TranslateFragment) manager.getFragment(savedInstanceState, "translateFragment");
        } else {
            noteBookFragment = new NoteBookFragment();
            dailyOneFragment = new DailyOneFragment();
            translateFragment = new TranslateFragment();
        }

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .add(R.id.container_main, translateFragment, "translateFragment")
                .commit();

        manager.beginTransaction()
                .add(R.id.container_main, dailyOneFragment, "dailyOneFragment")
                .commit();

        manager.beginTransaction()
                .add(R.id.container_main, noteBookFragment, "noteBookFragment")
                .commit();

        Intent intent = getIntent();
        if (intent.getAction().equals(ACTION_NOTEBOOK)) {
            showHideFragment(2);
        } else if (intent.getAction().equals(ACTION_DAILY_ONE)) {
            showHideFragment(1);
        } else {
            showHideFragment(0);
        }

        //剪贴板粘贴功能
        clipboardCopy();
        Log.i("tttt", "onCreate: " + showNotification);

        // 基础控件换肤初始化
        SkinCompatManager.withoutActivity(getApplication())
                // material design 控件换肤初始化[可选]
                .addInflater(new SkinMaterialViewInflater())
                // ConstraintLayout 控件换肤初始化[可选]
                .addInflater(new SkinConstraintViewInflater())
                // CardView v7 控件换肤初始化[可选]
                .addInflater(new SkinCardViewInflater())
                // 关闭状态栏换肤，默认打开[可选]
                .setSkinStatusBarColorEnable(false)
                // 关闭windowBackground换肤，默认打开[可选]
                .setSkinWindowBackgroundEnable(false)
                .loadSkin();

    }

    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //TODO
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_translate) {

            showHideFragment(0);

        } else if (id == R.id.nav_daily) {

            showHideFragment(1);

        } else if (id == R.id.nav_notebook) {

            showHideFragment(2);

        } else if (id == R.id.nav_setting) {

            startActivity(new Intent(MainActivity.this, SettingsPreferenceActivity.class));

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (translateFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "translateFragment", translateFragment);
        }

        if (noteBookFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "noteBookFragment", noteBookFragment);
        }

        if (dailyOneFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, "dailyOneFragment", dailyOneFragment);
        }

    }

    /**
     * show or hide the fragment
     * and handle other operations like set toolbar's title
     * set the navigation's checked item
     *
     * @param position which fragment to show, only 3 values at this time
     *                 0 for translate fragment
     *                 1 for daily one fragment
     *                 2 for notebook fragment
     */
    private void showHideFragment(@IntRange(from = 0, to = 2) int position) {

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().hide(translateFragment).commit();
        manager.beginTransaction().hide(noteBookFragment).commit();
        manager.beginTransaction().hide(dailyOneFragment).commit();

        if (position == 0) {
            manager.beginTransaction().show(translateFragment).commit();
            toolbar.setTitle(R.string.app_name);
            navigationView.setCheckedItem(R.id.nav_translate);
        } else if (position == 1) {
            toolbar.setTitle(R.string.daily_one);
            manager.beginTransaction().show(dailyOneFragment).commit();
            navigationView.setCheckedItem(R.id.nav_daily);
        } else if (position == 2) {
            toolbar.setTitle(R.string.notebook);
            manager.beginTransaction().show(noteBookFragment).commit();
            navigationView.setCheckedItem(R.id.nav_notebook);
        }

    }

    /**
     * 监听剪贴板
     */
    private void clipboardCopy() {
            final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                    @Override
                    public void onPrimaryClipChanged() {
                        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip().getItemCount() > 0) {
                            CharSequence search = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                            if (search != null&&showNotification) {
                                Log.d("clipboard1", "copied text: " + search);
                                sendReq(search.toString());
                            }
                        }
                    }
                });
            }
    }

    /**
     * 发送请求数据
     *
     * @param in
     */
    private void sendReq(final String in) {

        queue = Volley.newRequestQueue(this);

        String sign = MD5Util.getMD5(Constants.YOUDAO_APPID + in + Constants.salt + Constants.YOUDAO_KEY);

        String url = Constants.YOUDAO_URL + in + Constants.TRAN_AUTO + Constants.YOUDAO_APPID + "&salt=" + Constants.salt + "&sign=" + sign;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String s) {
                        try {
                            Gson gson = new Gson();

                            model = gson.fromJson(s, YouDaoModel.class);

                            if (model != null) {
                                result = model.getQuery() + "\n";
                                if (model.getTranslation() != null) {
                                    for (String def : model.getTranslation()) {
                                        result = result + def;
                                    }
                                }
                                Log.i("clipboard1", "onResponse:-------" + result);
                                notifyCation(result);
                            }
                        } catch (JsonSyntaxException ex) {
                            Log.i("clipboard1", "onResponse: 翻译失败");
                        }
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("clipboard1", "onErrorResponse: volleyError");
            }
        });

        queue.add(request);

    }


    /**
     * 通知栏操作
     */

    //TODO 改进：增加数据库查询，存在->震动提醒，红灯提示
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notifyCation(String text) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //优先级默认为3
        NotificationChannel notificationChannel = new NotificationChannel("translate", "翻译通知", NotificationManager.IMPORTANCE_HIGH);
        //设置时会显示
        notificationChannel.setDescription("翻译通知");

        //通知显示绿灯
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);

        //通知震动提示
        notificationChannel.enableVibration(false);
        notificationChannel.setVibrationPattern(new long[]{0, 500, 1000, 1500});


        notificationManager.createNotificationChannel(notificationChannel);

        //跳转到TranslateFragment页面
        Intent intent = new Intent(this, TranslateFragment.class);
        //获取PendingIntent实例可以通过PendingIntent的getActivity(),getBroadCast(),getService()方法
        //第一个参数是Context,第二个参数一般为0，第三个是Intent对象，第四个是确定pendingIntent的行为，有FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCLE_CURRENT,FLAG_UPDATE_CURRENT四种值，一般传入0
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE);


        //TODO 增加跳转翻译 适配
        //安卓8.0以上必须带有SmallIcon
        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle("Translate")
//                .setContentText(text)
                .setChannelId("translate")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text).setSummaryText("Translation"))
                .build();

        //创建通知
        notificationManager.notify(1, notification);
        Log.i("Notify", "notifyCation: 通知创建");

//        //取消通知
//        notificationManager.cancel(1);
//        Log.i("Notify", "notifyCation: 通知消除");
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showNotification = sharedPreferences.getBoolean("showNotification", false);
        Log.i("tttt", "onResume: showNotification"+showNotification);
    }
}

//通常通知会在广播接收器(broadcastReciver)或者服务(Server)中创建，不在活动中创建，因为app给我们发送通知的时候并不是运行在前台。
