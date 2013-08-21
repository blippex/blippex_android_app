package com.blippex.app.misc;

import java.security.KeyStore;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;

/**
 * @author James
 * 
 */
public class Common {

	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo inetInfo = connectivityManager.getActiveNetworkInfo();
		return inetInfo != null && inetInfo.isConnected();
	}

	public static String getDomain(String uri) {
		try {
			uri = Uri.parse(uri).buildUpon().build().getHost();
		} catch (Exception e) {
		}
		return uri;
	}
	
	public static String getProtoDomain(String uri) {
		try {
			uri = Uri.parse(uri).buildUpon().build().getScheme() + "://" + getDomain(uri) + "/";
		} catch (Exception e) {
		}
		return uri;
	}


	public static DefaultHttpClient getThreadSafeClient() {
//		DefaultHttpClient client = new DefaultHttpClient();
//		ClientConnectionManager mgr = client.getConnectionManager();
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//				.permitAll().build();
//
//		StrictMode.setThreadPolicy(policy);
//
//		HttpParams params = client.getParams();
//		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
//				mgr.getSchemeRegistry()), params);
//		return client;
		
		
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
		
	}

	public static JSONArray concatArray(JSONArray... arrs) throws JSONException {
		JSONArray result = new JSONArray();
		for (JSONArray arr : arrs) {
			for (int i = 0; i < arr.length(); i++) {
				result.put(arr.get(i));
			}
		}
		return result;
	}

	public static String addTextHighlighting(final String string) {
		return string.replaceAll("<mark>", "<b>").replaceAll("</mark>", "</b>")
				.replaceAll("[<](/)?img[^>]*", "");

	}
}
