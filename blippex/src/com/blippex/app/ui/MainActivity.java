package com.blippex.app.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.blippex.app.R;
import com.blippex.app.adapter.SearchAdapter;
import com.blippex.app.api.SearchOptions;
import com.blippex.app.misc.Common;
import com.blippex.app.misc.Logger;
import com.blippex.app.settings.Settings;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.net.Uri;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends SherlockActivity {

	private EditText mEditSearch;
	private SearchAdapter mAdapter;
	private ListView mListView;
	private Thread mThread;
	private ImageButton mButtonSearchOptions;
	private boolean isScrolling = false;
	private boolean isLoading = false;

	private LinearLayout mSearchOptionsPanel;
	private SeekBar mSeekDwell, mSeekSeen;
	private View footerView = null;
	private TextView mLabelSeen, mLabelDwell;

	private String searchQuery = "";

	private Typeface mFont;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setIcon(R.drawable.logo);

		ab.setCustomView(LayoutInflater.from(this).inflate(R.layout.actionbar,
				null));
		ab.setDisplayShowCustomEnabled(true);

		mFont = Typeface.createFromAsset(getAssets(),
				"fonts/PT_Sans-Web-Regular.ttf");

		mButtonSearchOptions = (ImageButton) findViewById(R.id.options);
		mEditSearch = (EditText) findViewById(R.id.search);
		mListView = (ListView) findViewById(R.id.list);
		mSearchOptionsPanel = (LinearLayout) findViewById(R.id.searchOptions);
		mSeekDwell = (SeekBar) findViewById(R.id.dwell);
		mSeekSeen = (SeekBar) findViewById(R.id.seen);
		mLabelSeen = (TextView) findViewById(R.id.label_seen);
		mLabelDwell = (TextView) findViewById(R.id.label_dwell);

		mEditSearch.setTypeface(mFont);
		mLabelSeen.setTypeface(mFont);
		mLabelDwell.setTypeface(mFont);
		((TextView) findViewById(R.id.textEmpty)).setTypeface(mFont);

		mAdapter = new SearchAdapter(this, R.layout.item_default);
		mAdapter.setNotifyOnChange(true);
		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(findViewById(R.id.empty));

		if (footerView == null) {

			footerView = this.getLayoutInflater().inflate(
					R.layout.item_loading, null);
			// mListView.removeFooterView(footerView);
			// mListView.addFooterView(footerView);
			// mListView.removeFooterView(footerView);
		}

		mListView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				isScrolling = scrollState == SCROLL_STATE_TOUCH_SCROLL;

			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (view.getAdapter() != null
						&& totalItemCount > 0
						&& ((firstVisibleItem + visibleItemCount) >= totalItemCount)
						&& ((firstVisibleItem + visibleItemCount) < SearchOptions.total)
						&& !isLoading) {
					onSearch();
				}
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long arg3) {

				if (isScrolling) {
					return;
				}

				JSONObject item = new JSONObject();
				try {
					item = ((JSONObject) adapter.getItemAtPosition(position));
				} catch (Exception e) {
				}

				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item
						.optString("url"))));

			}

		});

		mButtonSearchOptions.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSearchOptions();
			}
		});

		mSeekDwell.setProgress(Settings.dwell());
		mSeekSeen.setProgress(Settings.seen());

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc().build();

		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().destroy();
		}

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions).threadPoolSize(5)
				.threadPriority(Thread.NORM_PRIORITY)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
				.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
				.build();

		ImageLoader.getInstance().init(config);

		setSearchHandler();
		updateLabels();

	}

	private void setSearchHandler() {
		mEditSearch.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
		mEditSearch.setOnKeyListener(new EditText.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					onClear();
					onSearch();
					return true;
				}
				return false;
			}
		});

		mSeekDwell.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Settings.dwell((int) Math.round(seekBar.getProgress()));
				updateLabels();
				onClear();
				onSearch();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mLabelDwell.setText(String.format(
						getResources().getString(R.string.input_slider_dwell),
						progress));
			}
		});

		mSeekSeen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Settings.seen((int) Math.round(seekBar.getProgress()));
				updateLabels();
				onClear();
				onSearch();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mLabelSeen.setText(String.format(
						getResources().getString(R.string.input_slider_last),
						progress+1));
			}
		});
	}

	private void onClear() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				SearchOptions.offset = 0;
				mAdapter.clear();
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void onSearch() {
		searchQuery = mEditSearch.getText().toString().trim();
		if (searchQuery.length() > 0) {
			mThread = new Thread() {
				public void run() {
					onResult(loadData());
				}
			};
			mThread.start();
		}
	}

	private void onResult(JSONObject data) {
		
		this.runOnUiThread(new Runnable() {
			public void run() {
				TextView emptyText = (TextView) mListView.getEmptyView().findViewById(
						R.id.textEmpty);
				emptyText.setText(getResources().getString(
						R.string.search_nothing_found));
			}
		});

		final JSONArray results = data.optJSONArray("results");

		if (data.optInt("hits_displayed") > 0) {
			SearchOptions.offset += data.optInt("hits_displayed");
			SearchOptions.total = data.optInt("total");

			this.runOnUiThread(new Runnable() {
				public void run() {
					try {
						mAdapter.setResults(results);
						mAdapter.notifyDataSetChanged();
						isLoading = false;
						if (footerView != null && mListView != null) {
							// mListView.removeFooterView(footerView);
						}
						toggleLoading(false);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});

		} else {
			this.runOnUiThread(new Runnable() {
				public void run() {
					toggleLoading(false);
				}
			});
		}
	}

	private JSONObject loadData() {
		if (isLoading) {
			mThread.interrupt();
		}
		Logger.getDefault().info(buildQuery());

		runOnUiThread(new Runnable() {
			public void run() {
				try {
					mListView.getEmptyView().findViewById(R.id.textEmpty)
							.setVisibility(View.GONE);
					if (SearchOptions.offset == 0) {
						// mListView.addFooterView(footerView);
						mListView.getEmptyView()
								.findViewById(R.id.progressEmpty)
								.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {

				}
			}
		});

		HttpGet request = new HttpGet(buildQuery());

		StringBuilder builder = new StringBuilder();
		try {
			isLoading = true;
			HttpResponse response = Common.getThreadSafeClient().execute(
					request);

			StatusLine statusLine = response.getStatusLine();

			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				// error occured
			}
			response.getEntity().consumeContent();
			Logger.getDefault().debug("Content" + builder.toString());
		} catch (Exception e) {
			Logger.getDefault().debug(e.getMessage());
			isLoading = false;
		}
		try {
			return new JSONObject(builder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			Logger.getDefault().debug("EMPTY");
			isLoading = false;
			return new JSONObject();
		}
	}

	private final String buildQuery() {
		Uri.Builder uri = Uri.parse("https://api.blippex.org/search")
				.buildUpon();
		uri.appendQueryParameter("q", searchQuery);
		uri.appendQueryParameter("highlight", "1");
		uri.appendQueryParameter("limit", "20");
		uri.appendQueryParameter("d", Integer.toString((Settings.seen() + 1)));
		uri.appendQueryParameter("w", Integer.toString(100 - Settings.dwell()));
		uri.appendQueryParameter("offset",
				Integer.toString(SearchOptions.offset));
		return uri.build().toString();
	}

	private void onSearchOptions() {
		if (mSearchOptionsPanel.getVisibility() == View.GONE) {
			mSearchOptionsPanel.setVisibility(View.VISIBLE);
			Animation animation1 = AnimationUtils.loadAnimation(this,
					R.anim.options_start);

			RotateAnimation ra = new RotateAnimation(0, 90,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			ra.setFillAfter(true);
			ra.setDuration(200);

			mButtonSearchOptions.startAnimation(ra);

			mSearchOptionsPanel.startAnimation(animation1);
		} else {
			Animation animation1 = AnimationUtils.loadAnimation(this,
					R.anim.options_end);
			RotateAnimation ra = new RotateAnimation(90, 0,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			ra.setFillAfter(true);
			ra.setDuration(200);

			mButtonSearchOptions.startAnimation(ra);

			mSearchOptionsPanel.startAnimation(animation1);
			mSearchOptionsPanel.setVisibility(View.GONE);
		}
	}

	public void toggleLoading(boolean isLoading) {
		mListView.getEmptyView().findViewById(R.id.textEmpty)
				.setVisibility(isLoading ? View.GONE : View.VISIBLE);
		mListView.getEmptyView().findViewById(R.id.progressEmpty)
				.setVisibility(isLoading ? View.VISIBLE : View.GONE);
	}

	private void updateLabels() {
		mLabelDwell.setText(String.format(
				getResources().getString(R.string.input_slider_dwell),
				Settings.dwell()));
		mLabelSeen.setText(String.format(
				getResources().getString(R.string.input_slider_last),
				Settings.seen() + 1));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.getDefault().info("options selected " + item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.blippex.org/about")));
			break;
		default:
			super.onOptionsItemSelected(item);
		}
		return true;
	}

}
