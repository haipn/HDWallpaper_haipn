package com.viavilab.hdwallpaper;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FontAdapter extends BaseAdapter {
	public FontAdapter(ArrayList<Typeface> mListFont, Context mContext) {
		super();
		this.mListFont = mListFont;
		this.mContext = mContext;
	}

	ArrayList<Typeface> mListFont;
	Context mContext;

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListFont.size();
	}

	@Override
	public Typeface getItem(int position) {
		// TODO Auto-generated method stub
		return mListFont.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text = new TextView(mContext);
		Typeface font = getItem(position);
		text.setText("Sample text");
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				10, mContext.getResources().getDisplayMetrics());
		text.setPadding(px, px, px, px);
		text.setTypeface(font);
		return text;
	}

}
