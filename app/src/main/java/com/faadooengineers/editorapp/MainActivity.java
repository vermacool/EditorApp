package com.faadooengineers.editorapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String JSON_URL = "http://www.twominds.co.in/api/secure_detail.php?";
    RTManager rtManager;
    Button submitBtn;
    RTEditText rtEditText;
    Uri uri;
    String stringUri;
    RTApi rtApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set theme before calling setContentView!
        setTheme(R.style.RTE_ThemeLight);
        // create RTManager


        setContentView(R.layout.activity_main);

        rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));

        // rtApi.createImage(modelObj.getImgUrl());

        rtManager = new RTManager(rtApi, savedInstanceState);

        // register toolbar
        ViewGroup toolbarContainer = (ViewGroup) findViewById(R.id.rte_toolbar_container);
        RTToolbar rtToolbar = (RTToolbar) findViewById(R.id.rte_toolbar);


        if (rtToolbar != null) {
            rtManager.registerToolbar(toolbarContainer, rtToolbar);
        }
        rtManager.onPickImage();
        // register editor & set text
        rtEditText = (RTEditText) findViewById(R.id.rtEditText);
        rtManager.registerEditor(rtEditText, true);


        sendRequest();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        rtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        rtManager.onDestroy(isFinishing());
    }

    private void sendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showJSON(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("topic_id", "7934");
                params.put("api_key", "656e67696e656572696e67746f70657261");

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
/*to pass url as simple text*/

    private void showJSON(String json) {
        Model modelObj = new Model();
        JSONArray data = null;
        try {
            data = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = data.getJSONObject(i);
                int topicId = jsonObject.getInt("ID");
                String content = jsonObject.getString("contenxxt");

                String clean = StringEscapeUtils.unescapeHtml4(content).replaceAll("[^\\x20-\\x7e]", "");
/*get spanned text from content*/
                //  Spanned spn=Html.fromHtml(content);

                //    stringUri = uri.toString();

                //imgCount = new ArrayList<>();
                //imgCount.add(imgSrc);

                //Pattern p = Pattern.compile("src=\"(.*?)\"");
                Pattern p = Pattern.compile("<img(.*?)/>");
                Matcher m = p.matcher(clean);
                int counter = 0;
                while (m.find()) {
                    String imgSrc = m.group();
                    modelObj.setImgUrl(imgSrc);
                    Log.d("urls", "=====>" + imgSrc);
                    String src = imgSrc.replace("<img alt=\"\" src=\"", "").replace("\" />", "");
                    clean = clean.replace(imgSrc, "<a href=\"" + src + "\"> image - " + counter++ + " : " + src + "</a>");
                    //      Glide.with(this).load(imgCount.get(j)).into();
                }
/*here we have to set content at @param message to edit*/

                // rtEditText.setRichTextEditing(true, true);
                // clean = clean.replaceAll("<img alt=\"\"", "<a").replaceAll("src=", "href=").replaceAll(".png\" />", ".png\">image</a>");
                rtEditText.setRichTextEditing(true, clean);
                Log.e("content", clean);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                  /*get the content of editor*/

                String text = rtEditText.getText(RTFormat.HTML);
                Log.d("output", text);
                new AlertDialog.Builder(this).setMessage(text).show();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

    }
}
