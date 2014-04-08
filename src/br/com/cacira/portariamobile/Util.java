package br.com.cacira.portariamobile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class Util {

	public final static long ONE_SECOND = 1000;
    public final static long SECONDS = 60;

    public final static long ONE_MINUTE = ONE_SECOND * 60;
    public final static long MINUTES = 60;

    public final static long ONE_HOUR = ONE_MINUTE * 60;
    public final static long HOURS = 24;

    public final static long ONE_DAY = ONE_HOUR * 24;
    
	/**
     * converts time (in milliseconds) to human-readable format
     *  "<w> days, <x> hours, <y> minutes and (z) seconds"
     */
    public static String millisToLongDHMS(long duration) {
      StringBuffer res = new StringBuffer();
      long temp = 0;
      if (duration >= ONE_SECOND) {
        temp = duration / ONE_DAY;
        if (temp > 0) {
          duration -= temp * ONE_DAY;
          res.append(temp).append("d").append(temp > 1 ? "" : "")
             ;//.append(duration >= ONE_MINUTE ? ", " : "");
          
          return res.toString();
        }

        temp = duration / ONE_HOUR;
        if (temp > 0) {
          duration -= temp * ONE_HOUR;
          res.append(temp).append("h").append(temp > 1 ? "" : "")
             ;//.append(duration >= ONE_MINUTE ? ", " : "");
          
          return res.toString();
        }

        temp = duration / ONE_MINUTE;
        if (temp > 0) {
          duration -= temp * ONE_MINUTE;
          res.append(temp).append("m").append(temp > 1 ? "" : "");
          
          return res.toString();
        }

//        if (!res.toString().equals("") && duration >= ONE_SECOND) {
//          res.append(" e ");
//        }

        temp = duration / ONE_SECOND;
        if (temp > 0) {
          res.append(temp).append("s").append(temp > 1 ? "" : "");
          
          return res.toString();
        }
        return res.toString();
      } else {
        return "0 s";
      }
    }
    
	public static boolean respostaSolicitacaoEntrada(final Context context, boolean deixou, String id, String obs)
	{
		boolean retorno = false;
		
		if (isOnline(context))
		{
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	        	String url = context.getString(R.string.webserviceUrl) + "?id=" + id + "&autoriza=" + deixou + "&mor_obs=";
	        	obs = URLEncoder.encode(obs, "UTF-8");
	        	url += obs;
	            response = httpclient.execute(new HttpGet(url));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                JSONObject jsonObject;
	                try {
						jsonObject = new JSONObject(responseString.trim());
						if (jsonObject.getString("sucesso").equals("1")) {
		                	retorno = true;
		                }
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();	                
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
		}
		
		return retorno;
	}
	
	public static JSONObject loginUsuario(final Context context, String email, String senha)
	{		
		JSONObject jsonObject = null;
		
		if (isOnline(context))
		{
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(context.getString(R.string.webserviceUrl) + "?action=login&email=" + email + "&senha=" + senha));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                
	                try {
						jsonObject = new JSONObject(responseString.trim());

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	                
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();	                
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
		}
		
		return jsonObject;
	}
	
	public static JSONObject getEntradas(final Context context, String mor_id)
	{		
		JSONObject jsonObject = null;
		
		if (isOnline(context))
		{
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(context.getString(R.string.webserviceUrl) + "?action=lista_entradas&mor_id=" + mor_id));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	                
	                try {
						jsonObject = new JSONObject(responseString.trim());
						
						// Salva o json numa user preference
						SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);	  
			            SharedPreferences.Editor editor = sharedPreferences.edit();
			            editor.putString("jsonListaEntradas", responseString.trim());
			            editor.commit();
			            
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	                
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();	                
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
		}
		
		return jsonObject;
	}
	
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public static boolean isOnline(Context ctx)
	{
		boolean isInternetAvailable = false;
		ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		
		if(networkInfo != null && (networkInfo.isConnected()))
		{
			isInternetAvailable  = true;
		}
		
		return isInternetAvailable;
	}
}
