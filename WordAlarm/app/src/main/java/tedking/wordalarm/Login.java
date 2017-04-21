package tedking.wordalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {
    private Button login;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences("sharedpreference",MODE_PRIVATE);
        login = (Button)findViewById(R.id.login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int result = login();
                            //login()为向php服务器提交请求的函数，返回数据类型为int
                            if (result == 1) {
                                Looper.prepare();
                                Toast.makeText(Login.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                editor = preferences.edit();
                                editor.putString("username",username.getText().toString()).commit();
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                Login.this.finish();
                                Looper.loop();
                            } else if (result == -2) {
                                Looper.prepare();
                                Toast.makeText(Login.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -1) {
                                Looper.prepare();
                                Toast.makeText(Login.this, "不存在该用户！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }
    /*
*用户登录提交post请求
* 向服务器提交数据1.user_id用户名，2.input_pwd密码
* 返回JSON数据{"status":"1","info":"login success","sex":"0","nicename":""}
*/
    private int login() throws IOException {
        int returnResult=0;
        /*获取用户名和密码*/
        String user_id=username.getText().toString();
        String input_pwd=password.getText().toString();
        if(user_id==null||user_id.length()<=0){
            Looper.prepare();
            Toast.makeText(Login.this,"请输入账号", Toast.LENGTH_LONG).show();
            Looper.loop();
            return 0;

        }
        if(input_pwd==null||input_pwd.length()<=0){
            Looper.prepare();
            Toast.makeText(Login.this,"请输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return 0;
        }
        String urlstr="http://www.wordalarm.online/android.php";
        //建立网络连接
        URL url = new URL(urlstr);
        HttpURLConnection http= (HttpURLConnection) url.openConnection();
        //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
        String params="uid="+user_id+'&'+"pwd="+input_pwd;
        http.setDoOutput(true);
        http.setRequestMethod("POST");
        OutputStream out=http.getOutputStream();
        out.write(params.getBytes());//post提交参数
        out.flush();
        out.close();

        //读取网页返回的数据
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
        String line="";
        StringBuilder sb=new StringBuilder();//建立输入缓冲区
        while (null!=(line=bufferedReader.readLine())){//结束会读入一个null值
            sb.append(line);//写缓冲区
        }
        String result= sb.toString();//返回结果

        try {
            /*获取服务器返回的JSON数据*/
            JSONObject jsonObject= new JSONObject(result);
            returnResult=jsonObject.getInt("status");
        } catch (Exception e) {
            // TODO: handle exception
        }
        return returnResult;
    }
}
