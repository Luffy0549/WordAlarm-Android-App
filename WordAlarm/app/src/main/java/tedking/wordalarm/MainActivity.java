package tedking.wordalarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String url = "http://fanyi.youdao.com/openapi.do?keyfrom="这里填你的keyfrom"&key=“这里填你的key,两个都不用双引号，可以向有道申请”&type=data&doctype=xml&version=1.1&q=";
    private String string = "",f, notFoundindb = "not found in database",networkerror = "请打开数据连接",searcherror = "查询失败，请检查输入是否正确";
    private String first_start = "first_start",song = "song",worddatabase = "worddatabase",questionnumber = "questionnumber";
    private String song1 = "梦中的婚礼",song2 = "天空之城", song3 = "故乡的原风景", database1 = "四六级词库", database2 = "托福词库";
    private RadioButton marriage,air,original,cet ,toef1,five,ten,fifth;
    public String[] finaltranslation = {"","",""};
    private static final int UPDATE_CONTENT = 0;
    private List<View> viewList;
    private Button addalarm,search,alarmselect, wordselect,questionselect, aboutus,settinglogin;
    private ViewPager viewPager;
    private EditText searchword;
    private ListView alarm_list;
    private View searchview, alarmview, settingview, alarmselectview,wordselectview, questionselectview;
    private TextView worditself, pronunciation,explain,nouse;
    private RadioGroup rgforalarm, rgforword,rgfornumber;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    private SimpleAdapter simpleAdapter;
    private int count = 0;
    private boolean first = false;
    public void tostartService(){
        Intent intent = new Intent(MainActivity.this,MyService.class);
        intent.setAction("来自主界面");
        startService(intent);
    }
    public String[] searchfromdatabase(){
        String []translation = {"","",""};
        File file = new File(getFilesDir()+"/databases/newdata.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = database.rawQuery("select * from cetcomplete where english = ?",new String[]{searchword.getText().toString()});
        int column = cursor.getColumnCount();
        while (cursor.moveToNext()){
            for (int i = 0; i < column; i ++) {
                String columnname = cursor.getColumnName(i);
                String columnvalue = cursor.getString(cursor.getColumnIndex(columnname));
                translation[i] = columnvalue;
            }
        }
        if (translation[0].equals("")){
            cursor = database.rawQuery("select * from toeflcomplete where english = ?",new String[]{searchword.getText().toString()});
            column = cursor.getColumnCount();
            while (cursor.moveToNext()){
                for (int i = 0; i < column; i ++){
                    String columnname = cursor.getColumnName(i);
                    String columnvalue = cursor.getString(cursor.getColumnIndex(columnname));
                    translation[i] = columnvalue;
                }
            }
        }
        if (database != null)
            database.close();
        if (translation[0].equals(""))
        {
            translation[0] = notFoundindb;
            return translation;
        }
        return translation;
    }
    public boolean Network_available(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        else {
            NetworkInfo networkInfos = connectivityManager.getActiveNetworkInfo();
            if (networkInfos != null && networkInfos.isConnected()){
                return true;
            }
        }
        return false;
    }
    public void aftersearchfromdatabase(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                string = "";
                finaltranslation[0] = "";
                finaltranslation[1] = "";
                finaltranslation[2] = "";
                //here is codes new added
                finaltranslation = searchfromdatabase();
                if (finaltranslation[0].equals(notFoundindb) && !Network_available()){
                    finaltranslation[0] = networkerror;
                }
                else if (finaltranslation[0].equals(notFoundindb) && Network_available()){
                    //add finish
                    HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) ((new URL(url + URLEncoder.encode(searchword.getText().toString(), "utf-8")).openConnection()));
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    int response_code = connection.getResponseCode();
                    if (response_code == connection.HTTP_OK) {
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        string = response.toString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(new StringReader(string));
                    int eventType = parser.getEventType();
                    string = "";
                    boolean explains = false;
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if ("phonetic".equals(parser.getName())) {
                                    try {
                                        finaltranslation[1] = "/" + parser.nextText() + "/";
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else if ("explains".equals(parser.getName())) {
                                    explains = true;
                                } else if ("ex".equals(parser.getName())) {
                                    if (explains) {
                                        try {
                                            finaltranslation[2] += parser.nextText();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                if ("explains".equals(parser.getName()))
                                    explains = false;
                                break;
                            default:
                                break;
                        }
                        try {
                            eventType = parser.next();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
                if (!finaltranslation[2].equals("")){
                    finaltranslation[0] = searchword.getText().toString();
                }
                Message message = new Message();
                message.what = UPDATE_CONTENT;
                message.obj = finaltranslation;
                handler.sendMessage(message);
        }
        }).start();
    }
    public void findview(){
        viewPager = (ViewPager) findViewById(R.id.vp);
        LayoutInflater inflater = getLayoutInflater();
        searchview = inflater.inflate(R.layout.search,null);
        alarmview = inflater.inflate(R.layout.alarm,null);
        settingview = inflater.inflate(R.layout.setting,null);
        viewList = new ArrayList<View>();
        viewList.add(searchview);
        viewList.add(alarmview);
        viewList.add(settingview);
        final PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
            @Override
            public void destroyItem(ViewGroup container, int position, Object object){
                container.removeView(viewList.get(position));
            }
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        searchword = (EditText) searchview.findViewById(R.id.searchword);
        search = (Button) searchview.findViewById(R.id.searchbutton);
        worditself = (TextView) searchview.findViewById(R.id.worditself);
        pronunciation = (TextView) searchview.findViewById(R.id.pronunciation);
        explain = (TextView) searchview.findViewById(R.id.explain);
        addalarm = (Button) alarmview.findViewById(R.id.addAlarm);
        alarm_list = (ListView) alarmview.findViewById(R.id.alarm_list);
        alarmselect = (Button)settingview.findViewById(R.id.alarmselect);
        wordselect = (Button) settingview.findViewById(R.id.wordselect);
        questionselect = (Button) settingview.findViewById(R.id.questionselect);
        aboutus = (Button) settingview.findViewById(R.id.aboutus);
        nouse = (TextView) settingview.findViewById(R.id.nouse);
        settinglogin = (Button) settingview.findViewById(R.id.settinglogin);
        nouse.setVisibility(View.INVISIBLE);
    }
    public void importDatabase(){
        f = getFilesDir() + "/databases";
        File dir = new File(f);
        if (!dir.exists()){
            dir.mkdir();
        }
        f += "/newdata.db";
        File file = new File(f);
        if (!file.exists()) {
            FileOutputStream fileOutputStream = null;
            InputStream inputStream = null;
            inputStream = getResources().openRawResource(R.raw.newdata);
            try {
                fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[128];
                int len = 0;
                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    buffer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void addAlarmEvent(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,Alarmsetting.class);
        startActivity(intent);
    }
    public void whether_first_and_dosomething(){
        preferences = getSharedPreferences("sharedpreference",MODE_PRIVATE);
        first = preferences.getBoolean(first_start,true);
        if (first){
            importDatabase();
            editor = preferences.edit();
            editor.putBoolean(first_start,false).commit();
        }
    }
    public void alarmselectclick(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        alarmselectview = inflater.inflate(R.layout.alarmselectdialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(alarmselectview);
        final AlertDialog dialog = builder.create();
        marriage = (RadioButton) alarmselectview.findViewById(R.id.marriage);
        air = (RadioButton) alarmselectview.findViewById(R.id.air);
        original =(RadioButton) alarmselectview.findViewById(R.id.original);
        preferences = getSharedPreferences("sharedpreference",MODE_PRIVATE);
        String selectsong = preferences.getString(song,song1);
        if (selectsong.equals(song1)){
            marriage.setChecked(true);
        }
        else if (selectsong.equals(song2)){
            air.setChecked(true);
        }
        else {
            original.setChecked(true);
        }
        dialog.show();
        rgforalarm = (RadioGroup) alarmselectview.findViewById(R.id.rgforalarm);
        rgforalarm.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                editor = preferences.edit();
                int songid = rgforalarm.getCheckedRadioButtonId();
                if (songid == R.id.marriage){
                    editor.putString(song,song1).commit();
                }
                else if (songid == R.id.air){
                    editor.putString(song,song2).commit();
                }
                else {
                    editor.putString(song,song3).commit();
                }
                //if语句改变歌曲
                dialog.dismiss();
            }
        });
    }
    public void wordselectclick(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        wordselectview = inflater.inflate(R.layout.wordselectdialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(wordselectview);
        final AlertDialog dialog = builder.create();
        cet = (RadioButton) wordselectview.findViewById(R.id.cet);
        toef1 = (RadioButton) wordselectview.findViewById(R.id.toefl);
        preferences = getSharedPreferences("sharedpreference",MODE_PRIVATE);
        String selectword = preferences.getString(worddatabase,database1);
        if (selectword.equals(database1)){
            cet.setChecked(true);
        }
        else {
            toef1.setChecked(true);
        }
        dialog.show();
        rgforword = (RadioGroup) wordselectview.findViewById(R.id.rgforword);
        rgforword.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int worddatabaseid = rgforword.getCheckedRadioButtonId();
                editor = preferences.edit();
                if (worddatabaseid == R.id.cet){
                    editor.putString(worddatabase,database1).commit();
                }
                else {
                    editor.putString(worddatabase,database2).commit();
                }
                //if语句改变词库
                dialog.dismiss();
            }
        });
    }
    public void questionselectclick(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        questionselectview = inflater.inflate(R.layout.questionselectdialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(questionselectview);
        final AlertDialog dialog = builder.create();
        five = (RadioButton) questionselectview.findViewById(R.id.five);
        ten = (RadioButton) questionselectview.findViewById(R.id.ten);
        fifth = (RadioButton) questionselectview.findViewById(R.id.fifth);
        preferences = getSharedPreferences("sharedpreference",MODE_PRIVATE);
        int selectnumber = preferences.getInt(questionnumber,10);
        if (selectnumber == 5){
            five.setChecked(true);
        }
        else if (selectnumber == 10){
            ten.setChecked(true);
        }
        else {
            fifth.setChecked(true);
        }
        dialog.show();
        rgfornumber = (RadioGroup) questionselectview.findViewById(R.id.rgfornumber);
        rgfornumber.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int numberdatabaseid = rgfornumber.getCheckedRadioButtonId();
                editor = preferences.edit();
                if (numberdatabaseid == R.id.five){
                    editor.putInt(questionnumber,5).commit();
                }
                else if (numberdatabaseid == R.id.ten){
                    editor.putInt(questionnumber,10).commit();
                }
                else {
                    editor.putInt(questionnumber,15).commit();
                }
                dialog.dismiss();
            }
        });
    }
    public void aboutusclick(){
        count += 1;
        if (count % 2 == 1){
            nouse.setVisibility(View.VISIBLE);
        }
        else
            nouse.setVisibility(View.INVISIBLE);
    }
    public void updateUI(){
        String time,repeate;
        boolean hasvalue = false;
        File file = new File(getFilesDir()+"/databases/newdata.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        Cursor cursor = database.rawQuery("select * from alarm", null);
        List<Map<String,Object>> data = new ArrayList<>();
        while (cursor.moveToNext()){
            hasvalue = true;
            time = cursor.getString(0);
            repeate = cursor.getString(1);
            Map<String,Object> temp = new LinkedHashMap<>();
            temp.put("time",time);
            temp.put("repeate",repeate);
            data.add(temp);
        }
        cursor.close();
        database.close();
        if (hasvalue) {
            simpleAdapter = new SimpleAdapter(this, data, R.layout.alarm_item, new String[]{"time",  "repeate"}, new int[]{R.id.alarm_time, R.id.alarm_repeate});
            alarm_list.setAdapter(simpleAdapter);
        }
        else {
            alarm_list.setAdapter(null);
        }
    }
    public void deletedata(String name){
        File file = new File(getFilesDir()+"/databases/newdata.db");
        SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getPath(),null,SQLiteDatabase.OPEN_READWRITE);
        database.delete("alarm","time=?",new String[]{name});
        database.close();
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            String [] result = (String[]) message.obj;
            switch (message.what){
                case UPDATE_CONTENT:
                    if (result[0].equals(notFoundindb)){
                        Toast.makeText(MainActivity.this,searcherror,Toast.LENGTH_SHORT).show();
                        worditself.setText("");
                        pronunciation.setText("");
                        explain.setText("");
                    }
                    else {
                        worditself.setText(result[0]);
                        pronunciation.setText(result[1]);
                        explain.setText(result[2]);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        whether_first_and_dosomething();
        tostartService();
        findview();
        updateUI();
        settinglogin.setText(preferences.getString("username","登录"));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.searchbutton:
                        aftersearchfromdatabase();
                        break;
                    case R.id.addAlarm:
                        addAlarmEvent();
                        break;
                    case R.id.alarmselect:
                        alarmselectclick();
                        break;
                    case R.id.wordselect:
                        wordselectclick();
                        break;
                    case R.id.questionselect:
                        questionselectclick();
                        break;
                    case R.id.aboutus:
                        aboutusclick();
                        break;
                    case R.id.settinglogin:
                        editor = preferences.edit();
                        editor.putString("username","登录").commit();
                        Intent loginintent = new Intent(MainActivity.this,Login.class);
                        startActivity(loginintent);
                        MainActivity.this.finish();
                        break;
                }
            }
        };
        search.setOnClickListener(onClickListener);
        addalarm.setOnClickListener(onClickListener);
        alarmselect.setOnClickListener(onClickListener);
        wordselect.setOnClickListener(onClickListener);
        aboutus.setOnClickListener(onClickListener);
        questionselect.setOnClickListener(onClickListener);
        settinglogin.setOnClickListener(onClickListener);
        alarm_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, String> map = (Map< String, String>)simpleAdapter.getItem(position);
                new AlertDialog.Builder(MainActivity.this).setMessage("是否删除？").setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String delete_name = map.get("time");
                        deletedata(delete_name);
                        updateUI();
                        /*Intent intent = new Intent(MainActivity.this,DealReceiver.class);
                        intent.setAction("来自主界面");
                        sendBroadcast(intent);*/
                        tostartService();
                    }
                }).show();
                return true;
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }
}
