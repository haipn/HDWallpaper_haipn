package com.viavilab.hdwallpaper;

import java.util.ArrayList;
import java.util.List;

import com.example.adapter.FavoriteAdapter;
import com.example.favorite.DatabaseHandler;
import com.example.favorite.DatabaseHandler.DatabaseManager;
import com.example.favorite.Pojo;
import com.example.util.Constant;
import com.example.util.JsonUtils;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class FavoriteFragment extends Fragment {
	
	GridView grid_fav;
	DatabaseHandler db;
	private DatabaseManager dbManager;
	FavoriteAdapter adapter;
	ArrayList<String> allListImage,allListImageCatName;
	String[] allArrayImage,allArrayImageCatName;
	
	List<Pojo> allData;
	TextView txt_no;
	private int columnWidth;
	JsonUtils util;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
		
		//Log.e("View", "called");
		grid_fav=(GridView)rootView.findViewById(R.id.favorite_grid);
		txt_no=(TextView)rootView.findViewById(R.id.textView1);
		db=new DatabaseHandler(getActivity());
		dbManager = DatabaseManager.INSTANCE;
		dbManager.init(getActivity());
		util=new JsonUtils(getActivity());
		
		InitilizeGridLayout();
		allData=db.getAllData();
		adapter=new FavoriteAdapter(allData,getActivity(),columnWidth);
		grid_fav.setAdapter(adapter);
		if(allData.size()==0)
		{
			txt_no.setVisibility(View.VISIBLE);
		}
		else
		{
			txt_no.setVisibility(View.INVISIBLE);
		}
		
		grid_fav.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
			
				Intent intslider=new Intent(getActivity(),SlideImageActivity.class);
				intslider.putExtra("POSITION_ID", position);
				intslider.putExtra("IMAGE_ARRAY", allArrayImage);
 				intslider.putExtra("IMAGE_CATNAME", allArrayImageCatName);
				 
				startActivity(intslider);
			 
				
			}
		});
		return rootView;
	}
	
	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				Constant.GRID_PADDING, r.getDisplayMetrics());

		columnWidth = (int) ((util.getScreenWidth() - ((Constant.NUM_OF_COLUMNS + 1) * padding)) / Constant.NUM_OF_COLUMNS);

		grid_fav.setNumColumns(Constant.NUM_OF_COLUMNS);
		grid_fav.setColumnWidth(columnWidth);
		grid_fav.setStretchMode(GridView.NO_STRETCH);
		grid_fav.setPadding((int) padding, (int) padding, (int) padding,
				(int) padding);
		grid_fav.setHorizontalSpacing((int) padding);
		grid_fav.setVerticalSpacing((int) padding);
	}
	
	public void onDestroyView() {
		
		//Log.e("OnDestroy", "called");
		if(!dbManager.isDatabaseClosed())
			dbManager.closeDatabase();
		super.onDestroyView();
	}
	
	@Override
	public void onResume() {
	super.onResume();
	//Log.e("OnResume", "called");
	//when back key pressed or go one tab to another we update the favorite item so put in resume
	allData=db.getAllData();
	adapter=new FavoriteAdapter(allData,getActivity(),columnWidth);
	grid_fav.setAdapter(adapter);
	if(allData.size()==0)
	{
		txt_no.setVisibility(View.VISIBLE);
	}
	else
	{
		txt_no.setVisibility(View.INVISIBLE);
	}
	allListImage=new ArrayList<String>();
	allListImageCatName=new ArrayList<String>();

	
	allArrayImage=new String[allListImage.size()];
	allArrayImageCatName=new String[allListImageCatName.size()];

	
	 
	for(int j=0;j<allData.size();j++)
	{
		 
		 Pojo objAllBean=allData.get(j);
		
		allListImage.add(objAllBean.getImageurl());
		allArrayImage=allListImage.toArray(allArrayImage);
		
		allListImageCatName.add(objAllBean.getCategoryName());
		allArrayImageCatName=allListImageCatName.toArray(allArrayImageCatName);
		

		
	}
	if(dbManager == null){
	dbManager = DatabaseManager.INSTANCE;
	dbManager.init(getActivity());
	}else if(dbManager.isDatabaseClosed()){
	dbManager.init(getActivity());
	}
	}

	@Override
	public void onPause() {
	super.onPause();
	//Log.e("OnPaused", "called");
	if(!dbManager.isDatabaseClosed())
	dbManager.closeDatabase();
	}

}
