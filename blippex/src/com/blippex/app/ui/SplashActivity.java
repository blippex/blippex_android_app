package com.blippex.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import com.blippex.app.Blippex;
import com.blippex.app.R;

public class SplashActivity extends Activity {
	SplashActivity _this = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		View view = findViewById(R.id.rootView);
		view.setBackgroundDrawable(new DrawableGradient(new int[] { 0xFFFDFDFD,
				0xFFDEDFE0 }, 0));
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				onStartMainActivity();
			}
		}, 1000);

	}

	public class DrawableGradient extends GradientDrawable {
		DrawableGradient(int[] colors, int cornerRadius) {
			super(GradientDrawable.Orientation.BOTTOM_TOP, colors);
			try {
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				this.setGradientRadius((float) ((metrics.heightPixels > metrics.widthPixels ? metrics.widthPixels
						: metrics.heightPixels) / 1.5));
				this.setGradientType(GradientDrawable.RADIAL_GRADIENT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onStartMainActivity() {
		Intent intent = new Intent(Blippex.getAppContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.left_in, R.anim.left_out);
		finish();
	}
}