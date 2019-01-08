package com.tomspter.translator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.tomspter.translator.R;
import com.tomspter.translator.adapter.NotebookMarkItemAdapter;
import com.tomspter.translator.constant.Constants;
import com.tomspter.translator.db.DBUtil;
import com.tomspter.translator.db.NotebookDatabaseHelper;
import com.tomspter.translator.interfaze.OnRecyclerViewOnClickListener;
import com.tomspter.translator.model.NotebookMarkItem;
import com.tomspter.translator.model.YouDaoModel;
import com.tomspter.translator.util.MD5Util;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.tomspter.translator.ui.TranslateFragment.isChinese;
import static com.tomspter.translator.ui.TranslateFragment.isKorea;


public class NoteBookFragment extends Fragment {

    private RecyclerView recyclerViewNotebook;
    private FloatingActionButton fab;
    private ArrayList<NotebookMarkItem> list = new ArrayList<>();
    private NotebookMarkItemAdapter adapter;
    private TextView tvNoNote;

    private NotebookDatabaseHelper dbHelper;

    private RefreshLayout refreshLayout;

    private YouDaoModel model;

    private RequestQueue queue;
    private String soundPath;
    private boolean isChinese;
    private boolean isKorea;

    private static Pattern ISCHINESE = Pattern.compile("[\u4e00-\u9fa5]");
    private static Pattern ISKOREA = Pattern.compile("[\u2E80-\u2FDF\u3040-\u318F\u31A0-\u31BF\u31F0-\u31FF\u3400-\u4DB5\u4E00-\u9FFF\uA960-\uA97F\uAC00-\uD7FF]");

    public NoteBookFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new NotebookDatabaseHelper(getActivity(), "MyStore.db", null, 1);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notebook, container, false);

        initViews(view);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setTitle(R.string.add_to_notebook);
                LayoutInflater li = getActivity().getLayoutInflater();
                final View v = li.inflate(R.layout.add_note, null);
                dialog.setView(v);

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        TextInputEditText etInput = (TextInputEditText) v.findViewById(R.id.et_input);
                        TextInputEditText etOutput = (TextInputEditText) v.findViewById(R.id.et_output);

                        String in = etInput.getText().toString();
                        String out = etOutput.getText().toString();

                        if (in.isEmpty() || out.isEmpty()) {

                            Snackbar.make(fab, R.string.no_input, Snackbar.LENGTH_SHORT).show();

                        } else {

                            if (tvNoNote.getVisibility() == View.VISIBLE) {
                                tvNoNote.setVisibility(View.GONE);
                            }

                            NotebookMarkItem item = new NotebookMarkItem(in, out);

                            ContentValues values = new ContentValues();
                            values.put("input", in);
                            values.put("output", out);

                            DBUtil.insertValue(dbHelper, values);

                            values.clear();

                            list.add(0, item);
                            adapter.notifyItemInserted(0);
                            recyclerViewNotebook.smoothScrollToPosition(0);
                        }


                    }
                });

                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                dialog.show();
            }
        });

        getDataFromDB();

        if (list.isEmpty()) {
            tvNoNote.setVisibility(View.VISIBLE);
        }

        Collections.reverse(list);
        adapter = new NotebookMarkItemAdapter(getActivity(), list);
        recyclerViewNotebook.setAdapter(adapter);
        adapter.setItemClickListener(new OnRecyclerViewOnClickListener() {

            @Override
            public void OnItemClick(View view, int position) {

            }

            @Override
            public void OnSubViewClick(View view, final int position) {

                switch (view.getId()) {

                    case R.id.image_view_share:

                        NotebookMarkItem item1 = list.get(position);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND).setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(item1.getInput() + "\n" + item1.getOutput()));
                        startActivity(Intent.createChooser(intent, getString(R.string.choose_app_to_share)));

                        break;

                    case R.id.image_view_copy:

                        NotebookMarkItem item2 = list.get(position);

                        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("text", String.valueOf(item2.getInput() + "\n" + item2.getOutput()));
                        manager.setPrimaryClip(clipData);

                        Snackbar.make(fab, R.string.copy_done, Snackbar.LENGTH_SHORT).show();

                        break;

                    case R.id.image_view_mark_star:

                        final NotebookMarkItem item3 = list.get(position);

                        DBUtil.deleteValue(dbHelper, item3.getInput());

                        list.remove(position);

                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, list.size());

                        Snackbar.make(fab, R.string.remove_from_notebook, Snackbar.LENGTH_SHORT)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ContentValues values = new ContentValues();
                                        values.put("input", item3.getInput());
                                        values.put("output", item3.getOutput());

                                        DBUtil.insertValue(dbHelper, values);

                                        values.clear();

                                        list.add(position, item3);
                                        adapter.notifyItemInserted(position);
                                        recyclerViewNotebook.smoothScrollToPosition(position);
                                    }
                                }).show();
                        break;
                    case R.id.image_view_sound:
                        NotebookMarkItem item4 = list.get(position);
                        getSoundPath(item4.getInput());
                        Log.i("sound", "OnSubViewClick: " + soundPath);
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

                        break;
                    default:
                        break;
                }
            }
        });

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.i("refresh1", "onRefresh: 开始刷新");
                getDataFromDB();
                if (!list.isEmpty()) {
                    if (tvNoNote.getVisibility() == View.VISIBLE) {
                        tvNoNote.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }
                refreshLayout.finishRefresh();
            }
        });
        return view;
    }

    private void initViews(View view) {

        recyclerViewNotebook = view.findViewById(R.id.recycler_view_notebook);
        recyclerViewNotebook.setLayoutManager(new LinearLayoutManager(getActivity()));

        fab = view.findViewById(R.id.fab);

        tvNoNote = view.findViewById(R.id.tv_no_note);

        refreshLayout = view.findViewById(R.id.refreshLayout);

    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromDB();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        getDataFromDB();
    }

    private void getDataFromDB() {
        if (list != null) {
            list.clear();
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("notebook", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String in = cursor.getString(cursor.getColumnIndex("input"));
                Log.i("SQLite2", "getDataFromDB: " + in);
                String out = cursor.getString(cursor.getColumnIndex("output"));
                Log.i("SQLite2", "getDataFromDB: " + out);
                NotebookMarkItem item = new NotebookMarkItem(in, out);
                list.add(item);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void getSoundPath(String in) {
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String sign = MD5Util.getMD5(Constants.YOUDAO_APPID + in + Constants.salt + Constants.YOUDAO_KEY);
        String url = Constants.YOUDAO_URL + in + Constants.TRAN_AUTO + Constants.YOUDAO_APPID + "&salt=" + Constants.salt + "&sign=" + sign;
        isChinese = isChinese(in);
        isKorea = isKorea(in);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                model = gson.fromJson(s, YouDaoModel.class);
                if (model.getSpeakUrl() != null && model.gettSpeakUrl() != null) {
                    if (!isChinese) {
                        soundPath = model.getSpeakUrl();
                        Log.i("url", "onResponse:---speakUrlT---" + soundPath);
                    } else {
                        soundPath = model.gettSpeakUrl();
                        Log.i("url", "onResponse:---speakUrl---" + soundPath);
                        //soundPath = pronunciation.getAmEmp3();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("NOTEBOOK", "onErrorResponse: error!!!");
            }
        });
        queue.add(stringRequest);
    }

}
