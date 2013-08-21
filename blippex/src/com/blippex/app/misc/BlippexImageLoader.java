package com.blippex.app.misc;

import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.widget.ImageView;

public class BlippexImageLoader {

	public static void loadImage(final Context context, final View view, final JSONObject item, final String uri){
		ImageLoader.getInstance().displayImage(uri, (ImageView) view, getDisplayOptions(item, true));
	}
	
    private static Bitmap makeImageRound(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, (bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth()) / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
	
	private static final DisplayImageOptions getDisplayOptions(JSONObject item){
		return getDisplayOptions(item, false);
	}
	
	private static final DisplayImageOptions getDisplayOptions(JSONObject item, boolean preProcess){
		Builder displayOptions = new DisplayImageOptions.Builder()
        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
		.cacheInMemory()
		.cacheOnDisc();
		return displayOptions.build();
	}
	
	public static String getImageUrl(JSONObject item){
		return String.format("https://getfavicon.appspot.com/%s?defaulticon=https://blippex.org/css/img/default-favicon.png", Common.getProtoDomain(item.optString("url")));
	}
}