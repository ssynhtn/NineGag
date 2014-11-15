package com.ssynhtn.ninegag.volley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

	private static VolleySingleton sInstance;
	
	private RequestQueue requestQueue;
	private ImageLoader imageLoader;
	private Context context;
	
	
	private VolleySingleton(Context context){
		this.context = context.getApplicationContext();
		requestQueue = Volley.newRequestQueue(this.context);
		imageLoader = new ImageLoader(requestQueue, new BitmapLruCache());
	}
	
	public static synchronized VolleySingleton getInstance(Context context){
		if(sInstance == null){
			sInstance = new VolleySingleton(context);
		}
		
		return sInstance;
	}
	
	public RequestQueue getRequestQueue(){
		return requestQueue;
	}
	public ImageLoader getImageLoader(){
		return imageLoader;
	}

    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }

	
}
