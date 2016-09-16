package com.example.rox.combiningkeyboardtranslate;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rox on 9/14/2016.
 */
public class RequestTranslation extends AppCompatActivity {

    String translatedText;
    private static String key;

    public RequestTranslation(String apiKey) {
        key = apiKey;
    }

    public String requestTranslation(String text, String from, String to) {

        String encodedText = null;
        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String urlStr = "https://www.googleapis.com/language/translate/v2?key=" +
                key + "&q=" + encodedText + "&target=" + to + "&source=" + from;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlStr)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("Fun1", "Error: onFailure");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                ///
 //               JSONObject data = response.getJSONObject("data");
//
//                                Log.v("FunJson",data.toString());
//                                JSONArray translations = data.getJSONArray("translations");
//                                JSONObject translatedText = translations.getJSONObject(0);
//
//                               // amharicWord = translatedText.getString("translatedText");
//
//                              // amharicWord = translatedText.toString();
//                                txtOutput.setText(amharicWord);
//                                arrayList.add(txtInput.getText().toString());
//                                arrayList.add(amharicWord);
//                                listView.setAdapter(arrayAdapter);
                //



                    String result = response.body().string();
                        JsonParser parser = new JsonParser();

                JsonElement element = null;
                try {
                    element = parser.parse(result.toString());
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

                if (element.isJsonObject()) {
                            JsonObject obj = element.getAsJsonObject();
                            Log.v("Fun1" , "obj: "+obj);
                            if (obj.get("error") == null) {
                                translatedText = obj.get("data").getAsJsonObject().getAsJsonObject()
                                        .get("translations").getAsJsonArray()
                                        .get(0).getAsJsonObject()
                                        .get("translatedText").getAsString();
                            }
                        }
            }
        });


        return translatedText;
    }

    public String translate(String text, String from, String to) {
        StringBuilder result = new StringBuilder();
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlStr = "https://www.googleapis.com/language/translate/v2?key=" +
                    key + "&q=" + encodedText + "&target=" + to + "&source=" + from;

            URL url = new URL(urlStr);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            InputStream stream;


            if (conn.getResponseCode() == 200) // success
                stream = conn.getInputStream();
            else
                stream = conn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line);

            JsonParser parser = new JsonParser();

            JsonElement element = parser.parse(result.toString());

            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.get("error") == null) {
                    String translatedText = obj.get("data").getAsJsonObject().getAsJsonObject()
                            .get("translations").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("translatedText").getAsString();
                    return translatedText;
                }
            }

            if (conn.getResponseCode() != 200) {
                Log.v("Error", result.toString());
            }
        } catch (IOException | JsonSyntaxException ex) {
            Log.v("Error", ex.getMessage());
        }

        return null;
    }

}
