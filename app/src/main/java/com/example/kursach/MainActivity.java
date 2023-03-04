package com.example.kursach;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button main_button;
    private TextView result_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_field = findViewById(R.id.user_field);//Передаем id нужного объекта
        main_button = findViewById(R.id.main_button);
        result_info = findViewById(R.id.result_info);

        main_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user_field.getText().toString().trim().equals(""))
                    Toast.makeText(MainActivity.this, R.string.no_user_input,
                                                                         Toast.LENGTH_SHORT).show();
                else {
                    String crypto = user_field.getText().toString();
                    String urlUSD = "https://api.coingecko.com/api/v3/simple/price?ids="+crypto
                                                                              +"&vs_currencies=usd";
                    String urlRUB = "https://www.cbr-xml-daily.ru/daily_json.js";
                    new GetURLData().execute(urlRUB, urlUSD);
                }
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private class GetURLData extends AsyncTask<String, String, String> {

        // Будет выполнено до отправки данных по URL
        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Ожидайте...");
        }

        // Будет выполняться во время подключения по URL
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder buffer = new StringBuilder();
            String line = "";
            int i = 0;
            while (i!=strings.length){
//                System.out.println("!!!!!Сптрока "+i);


                try {
                    // Создаем URL подключение, а также HTTP подключение
                    URL url = new URL(strings[i]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // Создаем объекты для считывания данных из файла
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    // Генерируемая строка
                    StringBuilder temp = new StringBuilder();

                    // Считываем файл и записываем все в строку
                    while((line = reader.readLine()) != null)
                        temp.append(line).append("\n");



                    // Возвращаем строку
                    temp.deleteCharAt(temp.length()-2);
                    temp.deleteCharAt(0);
                    i++;
                    if (i!=strings.length){buffer.append(temp+",");}
                    else {buffer.append(temp);}

                    if (i==strings.length){
                        temp= new StringBuilder();
                        temp.append("{"+"\n"+buffer+"}");
                        buffer = new StringBuilder();
                        buffer.append(temp.append("\n"));
                        return buffer.toString();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Закрываем соединения
                    if(connection != null)
                        connection.disconnect();

                    try {
                        if (reader != null)
                            reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            return null;
        }

        // Выполняется после завершения получения данных
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                double priceUSD = jsonObject.getJSONObject(user_field.getText().toString().toLowerCase()).getDouble("usd");
                double priceRUB = jsonObject.getJSONObject("Valute").getJSONObject("USD").getDouble("Value") * priceUSD;
                result_info.setText("Цена в долларах: " + priceUSD +"$\n"+"Цена в рублях: "+ priceRUB+"₽");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}