package com.example.util;

import java.io.Serializable;

public class Constant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String URL_1 = "http://www.haipn.byethost7.com/hd_wallpaper";
	public static final String URL_2 = "http://lunarplease.biz/meme-photo";
	//this is the path of uploaded image of server where image store
	public static final String SERVER_IMAGE_UPFOLDER_CATEGORY= URL_1 + "/categories/";
	
	//this is the path of uploaded image of server where image store
	public static final String SERVER_IMAGE_UPFOLDER_THUMB= URL_1 + "/images/thumbs/";
		
	//this url is used to get latest 15 image in 1st tab.here 15 indicate that display latest 15 image if you want change to another then do.
	public static final String LATEST_URL =  URL_1 + "/api.php?latest=15";
	//this url gives list of category in 2nd tab
	public static final String CATEGORY_URL = URL_1 + "/api.php";
	//this url gives item of specific category.
	public static final String CATEGORY_ITEM_URL = URL_1 + "/api.php?cat_id=";
	
	
	
	
	public static final String LATEST_ARRAY_NAME="HDwallpaper";
	public static final String LATEST_IMAGE_CATEGORY_NAME="category_name";
	public static final String LATEST_IMAGE_URL="image";
	 
	
	
	public static final String CATEGORY_ARRAY_NAME="HDwallpaper";
	public static final String CATEGORY_NAME="category_name";
	public static final String CATEGORY_CID="cid";
	public static final String CATEGORY_IMAGE_URL="category_image";
	
	//for title display in CategoryItemF
	public static String CATEGORY_TITLE;
	public static int CATEGORY_ID;
	
	
	public static final String CATEGORY_ITEM_ARRAY ="HDwallpaper";
	public static final String CATEGORY_ITEM_CATNAME ="cat_name";
	public static final String CATEGORY_ITEM_IMAGEURL ="images";
	
	 
	// Number of columns of Grid View
		public static final int NUM_OF_COLUMNS = 3;

		// Gridview image padding
		public static final int GRID_PADDING = 8; // in dp

}
