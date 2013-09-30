package maddox.music.player;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity  {
	public String accessToken, expiresIn, userId;
	WebView webview;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{ 
	   super.onCreate(savedInstanceState);    
       setContentView(R.layout.webview);
       webview = (WebView)findViewById(R.id.webview);
       webview.getSettings().setJavaScriptEnabled(true);
       webview.setVisibility(WebView.INVISIBLE);
       
       webview.setWebViewClient(new WebViewClient()
       {

       public boolean shouldOverrideUrlLoading(WebView view, String url) {
           view.loadUrl(url);
           return true;
       }

       public void onPageStarted(WebView view, String url, Bitmap favicon) 
       {
           Log.d("TAG", url);       
           
           String[] separated = url.split("#");
           if(separated[0].contentEquals("http://oauth.vk.com/blank.html"))
           {
        	webview.setVisibility(WebView.INVISIBLE);
           	String[] separated2 = separated[1].split("&");        	
           	for(int i = 0; i < 3; i++)
           	{
           		String str = separated2[i];
           		String[] separated3 = str.split("=");
           		String name = separated3[0];
           		if(name.contentEquals("access_token"))
           		{
           			accessToken = separated3[1];
           		}
           		else if(name.contentEquals("expires_in"))
           		{
           			expiresIn = separated3[1];
           		}
           		else if(name.contentEquals("user_id"))
           		{
           			userId = separated3[1];
           		}        		
           	}
           	
           	Calendar c = Calendar.getInstance(); 
    		long currentTime = c.getTimeInMillis();
    		
    		String ipAddress = null;
		    try {
		        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		            NetworkInterface inf = en.nextElement();
		            for (Enumeration<InetAddress> enumIp = inf.getInetAddresses(); enumIp.hasMoreElements();) {
		                InetAddress inetAddress = enumIp.nextElement();
		                if (!inetAddress.isLoopbackAddress()) {
		                    ipAddress = inetAddress.getHostAddress();
		                }
		            }
		        }
		    } catch (SocketException ignored) {}
           	
		    

		      // We need an Editor object to make preference changes.
		      // All objects are from android.context.Context
		      SharedPreferences settings = getSharedPreferences("MadPlayerPreferences", 0);
		      SharedPreferences.Editor editor = settings.edit();
		      editor.putString("accessToken", accessToken);
		      editor.putString("userId", userId);
		      editor.putString("loginIP", ipAddress);
		      editor.putLong("expiresIn", Integer.parseInt(expiresIn));
		      editor.putLong("loginTime", currentTime);

		      // Commit the edits!
		      editor.commit();
		    
           	Intent intent = new Intent();
            intent.putExtra("accessToken", accessToken);
            intent.putExtra("expiresIn", Integer.parseInt(expiresIn));
            intent.putExtra("userId", userId);
            intent.putExtra("loginTime", currentTime);
            intent.putExtra("loginIP", ipAddress);
            setResult(RESULT_OK, intent);
            
            finish();
           	
           }
           else
           {
        	   webview.setVisibility(WebView.VISIBLE);
           }
           
           if(accessToken != (null))
           {
        	   Log.d("accessToken", accessToken);
           }
           if(expiresIn != (null))
           {
        	   Log.d("expiresIn", expiresIn);
           }
           if(userId != (null))
           {
        	   Log.d("userId", userId);
           }
       }});
       webview.loadUrl("http://oauth.vk.com/authorize?client_id=3195643&scope=audio&redirect_uri=http://oauth.vk.com/blank.html&display=mobile&response_type=token");
       
   }


}

