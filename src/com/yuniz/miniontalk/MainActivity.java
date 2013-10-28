package com.yuniz.miniontalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.yuniz.miniontalk.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import com.revmob.RevMob;
import com.revmob.RevMobTestingMode;
import com.revmob.ads.banner.RevMobBanner;

public class MainActivity extends Activity implements OnInitListener, OnUtteranceCompletedListener {
	
	public int screenWidth = 0;
	public int screenHeight = 0;
	
	private RelativeLayout mainCanvas;
	private ImageView calculateBtn;
	private TextView hintText;
	
	private String botApiConvoID = "9927bf362a";
	
	private int SPEECH_REQUEST_CODE = 1234;
	private TextToSpeech tts;
	
	private RevMob revmob;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int sdk = android.os.Build.VERSION.SDK_INT;
		
		//----------detect device setting and adapt environment
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		
		boolean smallScreen = false;
		try
		{ 
			display.getSize(size); 
			screenWidth = size.x; 
			screenHeight = size.y; 
			smallScreen = false;
		} 
		catch (NoSuchMethodError e) 
		{ 
			screenWidth = display.getWidth(); 
			screenHeight = display.getHeight(); 
			smallScreen = true;
		} 
	
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		//----------detect device setting and adapt environment

	    double setNewHeight = screenHeight;
		double setNewWidth = screenWidth;
		
		mainCanvas = (RelativeLayout) findViewById(R.id.mainCanvas);
		calculateBtn = (ImageView) findViewById(R.id.imageView1);
		hintText = (TextView) findViewById(R.id.textView2);
		
		try 
		{
			InputStream ims = getAssets().open("bg.jpg");
		    Drawable d = Drawable.createFromStream(ims, null);

		    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
		    	mainCanvas.setBackgroundDrawable(d);
		    } else {
		    	mainCanvas.setBackground(d);
		    }
			
			InputStream ims1 = getAssets().open("jarvisBTN.png");
		    Drawable d1 = Drawable.createFromStream(ims1, null);
		    calculateBtn.setImageDrawable(d1);
		}
		catch(IOException ex) 
		{
		    return;
		}
		
		//----------auto Adjust UI Elements size----------
		if(smallScreen == true){
			calculateBtn.setAdjustViewBounds(true);
			calculateBtn.setScaleType( ImageView.ScaleType.FIT_CENTER);
		}
		
		setNewWidth = screenWidth * 0.9;
		calculateBtn.setMinimumHeight((int)setNewWidth);
		calculateBtn.setMaxHeight((int)setNewWidth);
		calculateBtn.setMinimumWidth((int)setNewWidth);
		calculateBtn.setMaxWidth((int)setNewWidth);
		
		setNewHeight = screenWidth * 0.1;
		hintText.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)setNewHeight);
		//----------auto Adjust UI Elements size----------
		
		if(!isNetworkAvailable()){
			Toast.makeText(getApplicationContext(), "You need a smooth internet connection before you can use this app." , Toast.LENGTH_LONG).show();
		}else{
			botApiConvoID = getApiConvoID( getUrlContents("http://demo.program-o.com/b0tco/") );
		}

		tts = new TextToSpeech(this, this);
		
		/*----RevMob Ads----*/
		revmob = RevMob.start(this);
//revmob.setTestingMode(RevMobTestingMode.WITH_ADS);
		revmob.showFullscreen(this);
        /*----RevMob Ads----*/
		
	}
	
	@Override
    public void onInit(int status)
    {
		if (status == TextToSpeech.SUCCESS) {
			tts.setOnUtteranceCompletedListener(this);
			
			tts.setPitch(0);
			tts.setSpeechRate((float) 1.5);
		}
    }
	
	public String getUrlContents(String url){
	    String content = "";
	    HttpClient hc = new DefaultHttpClient();
	    HttpGet hGet = new HttpGet(url);
	    ResponseHandler<String> rHand = new BasicResponseHandler();
	    try {
	        content = hc.execute(hGet,rHand);
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return content;
	}
	
	public String getApiConvoID(String rawSource){
		String convoID = botApiConvoID;
			
		String[] stringSpliter = rawSource.split("id=\"convo_id\"");
		stringSpliter = stringSpliter[1].split("value=\"");
		stringSpliter = stringSpliter[1].split("\"");
		convoID = stringSpliter[0];
	
		return convoID;
	}
	
	public void onUtteranceCompleted(String uttId) {
		sendRecognizeIntent();
    }
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void startChat(View v) {
		sendRecognizeIntent();
		//lansychatAPI("hello");
	}
	
	private void sendRecognizeIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your words");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SPEECH_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                
                if (matches.size() == 0)
                {
                	tts.setPitch(0);
        			tts.setSpeechRate((float) 1.5);
                	tts.speak("ERROR : HEARD NOTHING, PLEASE SPEAK AGAIN.", TextToSpeech.QUEUE_FLUSH, null);
                }
                else
                {
                    String mostLikelyThingHeard = matches.get(0);
                    
                    String wordsEncoded = mostLikelyThingHeard;
            		try {
            			wordsEncoded = URLEncoder.encode(mostLikelyThingHeard, "utf-8");
            		} catch (UnsupportedEncodingException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
                    
                    lansychatAPI(wordsEncoded);
                }
            }
            else
            {
            	tts.setPitch(0);
    			tts.setSpeechRate((float) 1.5);
            	tts.speak("ERROR : CONNECTION TO JARVIS FAILED.", TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	public void lansychatAPI(String words){
		//String url = "http://lansyai.yuniz.com/?in=" + words;
		String url = "http://demo.program-o.com/b0tco/get_response.php";
		//-------load JSON
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("convo_id", botApiConvoID));
        nameValuePairs.add(new BasicNameValuePair("say", words));
        
		JSONObject json = getJSONfromURL(url, nameValuePairs);
		try {
			//Log.v("DEMO",json.getString("botsay"));
			if(json == null){
				Toast.makeText(getApplicationContext(), "You need internet connection to continue." , Toast.LENGTH_LONG).show();
			}else{
				tts.setPitch(0);
				tts.setSpeechRate((float) 1.5);
				tts.speak(json.getString("botsay"), TextToSpeech.QUEUE_FLUSH, null);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tts.setPitch(0);
			tts.setSpeechRate((float) 1.5);
			tts.speak("ERROR : CONNECTION TO JARVIS DISCONNECTED.", TextToSpeech.QUEUE_FLUSH, null);
		}
		//-------load JSON
	}
	
	public static JSONObject getJSONfromURL(String url,List<NameValuePair> postDatas ){

		//initialize
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			
	        httppost.setEntity(new UrlEncodedFormEntity(postDatas));
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}

		//try parse the string to a JSON object
		try{
	        	jArray = new JSONObject(result);
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return jArray;
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onDestroy()
    {
        if (tts != null)
        {
            tts.shutdown();
        }
        super.onDestroy();
    }
}
