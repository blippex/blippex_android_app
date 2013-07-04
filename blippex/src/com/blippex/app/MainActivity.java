package com.blippex.app;

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

import com.blippex.app.adapter.SearchAdapter;
import com.blippex.app.api.SearchOptions;
import com.blippex.app.misc.Common;
import com.blippex.app.misc.Logger;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	private ImageView mImageLogo;
	private EditText mEditSearch;
	private SearchAdapter mAdapter;
	private ListView mListView;
	private Thread mThread;
	private boolean isScrolling = false;
	private boolean isLoading = false;

	private String searchQuery = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mImageLogo = (ImageView) findViewById(R.id.logo);
		mEditSearch = (EditText) findViewById(R.id.search);
		mListView = (ListView) findViewById(R.id.list);

		mAdapter = new SearchAdapter(this, R.layout.item_default);
		mAdapter.setNotifyOnChange(true);
		mListView.setAdapter(mAdapter);

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

		setAnimation();
		setSearchHandler();

	}

	private void setAnimation() {
		Animation mAnimation = AnimationUtils.loadAnimation(this,
				R.anim.logo_start);
		mImageLogo.startAnimation(mAnimation);
	}

	private void setSearchHandler() {
		mEditSearch.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
		mEditSearch.setOnKeyListener(new EditText.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() != KeyEvent.ACTION_DOWN) {
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
					mImageLogo.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
					onClear();
					onSearch();
				}
				return false;
			}
		});
	}

	private void onClear() {
		this.runOnUiThread(new Runnable() {
			public void run() {
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
		final JSONArray results = data.optJSONArray("results");

		SearchOptions.offset += data.optInt("hits_displayed");
		SearchOptions.total = data.optInt("total");

		this.runOnUiThread(new Runnable() {
			public void run() {
				try {
					mAdapter.setResults(results);
					mAdapter.notifyDataSetChanged();
					isLoading = false;
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private JSONObject loadData() {
		if (isLoading) {
			mThread.interrupt();
		}
		Logger.getDefault().info(buildQuery());
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
		} catch (Exception e) {
			isLoading = false;
		}
		try {
			return new JSONObject(builder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
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
		uri.appendQueryParameter("offset",
				Integer.toString(SearchOptions.offset));
		Logger.getDefault().debug(uri.build().toString());
		return uri.build().toString();
	}
}
