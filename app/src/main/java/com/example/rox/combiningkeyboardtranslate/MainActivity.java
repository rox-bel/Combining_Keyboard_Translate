package com.example.rox.combiningkeyboardtranslate;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {

    private RequestTranslation requestTranslation;
    //@+id/xEt
    @InjectView(R.id.txtOutPut)
    TextView txtOutput;
    @InjectView(R.id.txtInput)
    TextView txtInput;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.translateBtn)
    Button mTranslateBtn;
    @InjectView(R.id.listView)
    ListView listView;

    @InjectView(R.id.switchBtn)
    Button mSwitchBtn;

    ///for amharic keyboard
    //@InjectView(R.id.amhKeyBoard) RelativeLayout mAmhKeyboard;
    private RelativeLayout mAmhKeyboard;
    private Button mB[] = new Button[32];
    private Button mBSpace, mBdone, mBack, mNum;
    private boolean isEdit = false, isEdit1 = false;

    String inputText;

    String finalURL;
    String translatedText = "";
    JsonObjectRequest jsonObject;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;
    boolean isSystemKeyboardOn = true;
    boolean isAmhKeyboardOn = false;

    private String mUpper = "upper", mLower = "lower";
    private int mWindowWidth;
    private String cL[] = {"\u1290", "\u1330", "\u1320", "\u12a0", "\u1218", "\u12a8", "\u12c8",
            "\u12d8", "\u1260", "\u12e0", "\u12e8", "\u12f0", "\u1238", "\u1230", "\u1270",
            "\u1278", "\u1200", "\u1228", "\u1298", "\u1230", "\u1240", "\u1328", "\u1208",
            "\u1308", "\u1238", "Z", "\u1300", "Ã ", "\u1348", "Ã¨", "Ã»", "Ã®"};
    private String nS[] = {"!", ")", "'", "#", "3", "$", "%", "&", "8", "*",
            "?", "/", "+", "-", "9", "0", "1", "4", "@", "5", "7", "(", "2",
            "\"", "6", "_", "=", "]", "[", "<", ">", "|"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        requestTranslation = new RequestTranslation("AIzaSyCjv1fCfI0Yc8aBsAsAwz_CalylT0TvDVM");

        arrayList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        mProgressBar.setVisibility(View.INVISIBLE);

        setKeys();
        txtInput.setOnTouchListener(this);
        txtInput.setOnFocusChangeListener(this);
        mAmhKeyboard = (RelativeLayout) findViewById(R.id.amhKeyBoard);
        hideAmhKeyboard();


    }

    @OnClick(R.id.translateBtn)
    public void translateButtonClick() {
//        toggleTxtinputFoccusable();
//        toggleKeyboard();
        hideSystemKeyboard(this);
        isSystemKeyboardOn = false;
        if (txtInput.isCursorVisible() == true) {
            txtInput.setCursorVisible(false);
        }
        getTranslation();
    }

    @OnClick(R.id.txtInput)
    public void txtInputClicked() {
        txtInput.setCursorVisible(true);
        toggleSystemKeyboard(this);
    }

    @OnClick(R.id.switchBtn)
    public void switchBtnClicked() {
        if (isSystemKeyboardOn) {
            hideSystemKeyboard(this);
            isSystemKeyboardOn = false;
            showAmhKeyboard();
            isAmhKeyboardOn = true;
        } else {
            hideAmhKeyboard();
            isAmhKeyboardOn = false;
            showSystemKeyboard(this);
            isSystemKeyboardOn = true;
        }
    }

    private void showAmhKeyboard() {
        mAmhKeyboard.setVisibility(RelativeLayout.VISIBLE);
        isAmhKeyboardOn = true;
    }

    private void hideAmhKeyboard() {
        mAmhKeyboard.setVisibility(RelativeLayout.INVISIBLE);
        isAmhKeyboardOn = false;
    }

    private void toggleSystemKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);

        if (isSystemKeyboardOn) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            isSystemKeyboardOn = false;
        } else {
            imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            isSystemKeyboardOn = true;
        }
    }

    private void hideSystemKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        isSystemKeyboardOn = false;
    }

    private void showSystemKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
        isSystemKeyboardOn = true;
    }

    public void getTranslation() {

        inputText = getFormatedWord(txtInput.getText().toString());
        Log.v("Fun1","inputText :"+inputText);
        translatedText = requestTranslation.requestTranslation(inputText, "am", "en");

        txtOutput.setText(translatedText);
        arrayList.add(inputText);
        arrayList.add(translatedText);
        listView.setAdapter(arrayAdapter);
        Log.v("Fun1", "Translated text: " + translatedText);

//        if (txtInput.length() != 0) try {
//            {
//
//                EnWord = getFormatedWord(txtInput.getText().toString());
//                Log.v("enword",EnWord);
//                    String encodedText = URLEncoder.encode(EnWord,"UTF-8");
//
//                finalURL = "https://www.googleapis.com/language/" +
//                        "translate/v2?key=AIzaSyCjv1fCfI0Yc8aBsAsAwz_CalylT0TvDVM&q="
//                        +encodedText+ EnWord + "&source=am&target=en";
//
//                if (isNetWorkAvailable()) {
//                    toggleRefresh();
//                    jsonObject = new JsonObjectRequest(Request.Method.GET, finalURL, (String) null, new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            try {
//
//                                ////
//                                JsonParser parser = new JsonParser();
//
//                                JsonElement element = parser.parse(response.toString());
//
//                                if(element.isJsonObject()){
//                                    JsonObject obj = element.getAsJsonObject();
//                                    if(obj.get("error")== null) {
//                                        amharicWord = obj.get("data").getAsJsonObject().
//                                                get("translations").getAsJsonArray().
//                                                get(0).getAsJsonObject().
//                                                get("translatedText").getAsString();
//                                    }
//                                }
//                                /////
//                                toggleRefresh();
//                                JSONObject data = response.getJSONObject("data");
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
//                                Log.v("Fun1", "Amharic word set to: " + amharicWord + " /n try block is done");
//                            } catch (JSONException exception) {
//                                Log.v("Fun1", "inside the Catch Block");
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            toggleRefresh();
//                            Log.v("fun", "Err1:" + error.getLocalizedMessage());
//                        }
//                    });
//
//                    Volley.newRequestQueue(this).add(jsonObject);
//                } else {
//                    alertUserAboutError();
//                }
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        else {
//            Toast toast = Toast.makeText(getApplicationContext(), "Please enter a word first", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP, 0, 950);
//            toast.show();
//        }


    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mTranslateBtn.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mTranslateBtn.setVisibility(View.VISIBLE);
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "Network Unavailable." +
                "/n Please connect to the Internet and try again.");
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public String getFormatedWord(String input) {
        input = input.trim();
        while (input.contains(" ")) {
            input = input.replace(' ', '+');
        }
        Log.v("newWord : ", input);
        return input;
    }

    private void setKeys() {
        mWindowWidth = getWindowManager().getDefaultDisplay().getWidth(); // getting
        // window
        // height
        // getting ids from xml files
        mB[0] = (Button) findViewById(R.id.xA);
        mB[1] = (Button) findViewById(R.id.xB);
        mB[2] = (Button) findViewById(R.id.xC);
        mB[3] = (Button) findViewById(R.id.xD);
        mB[4] = (Button) findViewById(R.id.xE);
        mB[5] = (Button) findViewById(R.id.xF);
        mB[6] = (Button) findViewById(R.id.xG);
        mB[7] = (Button) findViewById(R.id.xH);
        mB[8] = (Button) findViewById(R.id.xI);
        mB[9] = (Button) findViewById(R.id.xJ);
        mB[10] = (Button) findViewById(R.id.xK);
        mB[11] = (Button) findViewById(R.id.xL);
        mB[12] = (Button) findViewById(R.id.xM);
        mB[13] = (Button) findViewById(R.id.xN);
        mB[14] = (Button) findViewById(R.id.xO);
        mB[15] = (Button) findViewById(R.id.xP);
        mB[16] = (Button) findViewById(R.id.xQ);
        mB[17] = (Button) findViewById(R.id.xR);
        mB[18] = (Button) findViewById(R.id.xS);
        mB[19] = (Button) findViewById(R.id.xT);
        mB[20] = (Button) findViewById(R.id.xU);
        mB[21] = (Button) findViewById(R.id.xV);
        mB[22] = (Button) findViewById(R.id.xW);
        mB[23] = (Button) findViewById(R.id.xX);
        mB[24] = (Button) findViewById(R.id.xY);
        //take out this key z
        mB[25] = (Button) findViewById(R.id.xZ);
        mB[26] = (Button) findViewById(R.id.xS1);
        mB[27] = (Button) findViewById(R.id.xS2);
        mB[28] = (Button) findViewById(R.id.xS3);
        mB[29] = (Button) findViewById(R.id.xS4);
        mB[30] = (Button) findViewById(R.id.xS5);
        mB[31] = (Button) findViewById(R.id.xS6);
        mBSpace = (Button) findViewById(R.id.xSpace);
        mBdone = (Button) findViewById(R.id.xDone);
        mBack = (Button) findViewById(R.id.xBack);
        mNum = (Button) findViewById(R.id.xNum);
        for (int i = 0; i < mB.length; i++)
            mB[i].setOnClickListener(this);
        mBSpace.setOnClickListener(this);
        mBdone.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mNum.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v != mBdone && v != mBack && v != mNum) {
            addText(v);

        } else if (v == mBdone) {
            hideAmhKeyboard();
//            disableKeyboard();
        } else if (v == mBack) {
            isBack(v);
        } else if (v == mNum) {
            String nTag = (String) mNum.getTag();
            if (nTag.equals("num")) {
                changeSyNuLetters();
                changeSyNuTags();
                //  mBChange.setVisibility(Button.INVISIBLE);

            }
        }

    }

    private void addText(View v) {
        if (isEdit == true) {
            String b = "";
            b = (String) v.getTag();
            if (b != null) {
                // adding text in Edittext
                txtInput.append(b);

            }
        }
    }

    private void isBack(View v) {
        if (isEdit == true) {
            CharSequence cc = txtInput.getText();
            if (cc != null && cc.length() > 0) {
                {
                    txtInput.setText("");
                    txtInput.append(cc.subSequence(0, cc.length() - 1));
                }

            }
        }

    }

    private void changeSyNuLetters() {

        for (int i = 0; i < nS.length; i++)
            mB[i].setText(nS[i]);
        mNum.setText("ABC");
    }

    private void changeSyNuTags() {
        for (int i = 0; i < nS.length; i++)
            mB[i].setTag(nS[i]);
        mNum.setTag("ABC");
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == txtInput && hasFocus == true) {
            isEdit = true;
            isEdit1 = false;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (v == txtInput) {
//            hideSystemKeyboard(this);
//            showAmhKeyboard();
//        }
        return true;
    }
}
