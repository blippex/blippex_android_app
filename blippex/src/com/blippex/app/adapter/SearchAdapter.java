package com.blippex.app.adapter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blippex.app.Blippex;
import com.blippex.app.R;
import com.blippex.app.misc.BlippexImageLoader;
import com.blippex.app.misc.Common;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchAdapter extends ArrayAdapter<JSONObject> {
	private ArrayList<JSONObject> data = new ArrayList<JSONObject>();
	
	static class ViewHolder {
		public TextView title = null;
		public TextView url = null;
		public TextView text = null;
		public ImageView favicon = null;
	}

	public SearchAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	@Override
	public void clear() {
		super.clear();
		data.clear();
	}
	@Override
	public int getCount() {
		return  data == null ? 0 : data.size();
	}
	@Override
	public JSONObject getItem(int position) {
		return data.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setResults(JSONArray items) throws JSONException {
		for (int i = 0; i < items.length(); i++) {
			data.add(items.getJSONObject(i));
			add(items.getJSONObject(i));
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = new ViewHolder();

		JSONObject item = (JSONObject) getItem(position);
		int layout = R.layout.item_default;
		
		if (convertView == null){
			convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layout, parent, false);
			viewHolder.title = (TextView)convertView.findViewById(R.id.title);
			viewHolder.url = (TextView)convertView.findViewById(R.id.url);
			viewHolder.text = (TextView)convertView.findViewById(R.id.text);
			viewHolder.favicon = (ImageView) convertView.findViewById(R.id.favicon);
			convertView.setTag(viewHolder);
		} else  {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (viewHolder != null){
			BlippexImageLoader.loadImage(Blippex.getAppContext(), viewHolder.favicon, item, BlippexImageLoader.getImageUrl(item));
			viewHolder.title.setText(Html.fromHtml(Common.addTextHighlighting(item.optString("title"))));
			if (viewHolder.url != null){
				viewHolder.url.setText(Html.fromHtml(Common.addTextHighlighting(Common.getDomain(item.optString("url")))));
			}
			viewHolder.text.setText(Html.fromHtml(Common.addTextHighlighting(item.optString("highlight"))));
		}
		return convertView;
	}

}
