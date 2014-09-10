package com.viavilab.hdwallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.adapter.PinchZoomPageAdapter;
import com.example.favorite.DatabaseHandler;
import com.example.favorite.DatabaseHandler.DatabaseManager;
import com.example.favorite.Pojo;
import com.example.util.Constant;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class SlideImageActivity extends SherlockActivity implements
		SensorEventListener {

	int position;
	String[] mAllImages, mAllImageCatName;

	public DatabaseHandler db;
	ImageView vp_imageview;
	ViewPager viewpager;
	int TOTAL_IMAGE;
	private SensorManager sensorManager;
	private boolean checkImage = false;
	private long lastUpdate;
	Handler handler;
	Runnable Update;
	boolean Play_Flag = false;
	private Menu menu;
	private DatabaseManager dbManager;
	String Image_catName, Image_Url;
	Bitmap bgr;
	DisplayImageOptions options;
	private AdView mAdView;
	private int mCountPage;
	private InterstitialAd mInterstitial;
	private AdRequest request;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fullimageslider);
		db = new DatabaseHandler(this);
		dbManager = DatabaseManager.INSTANCE;
		dbManager.init(getApplicationContext());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		setTitle(Constant.CATEGORY_TITLE);
		// Look up the AdView as a resource and load a request.
		mAdView = (AdView) findViewById(R.id.adView);
		mAdView.loadAd(new AdRequest.Builder().build());

		Intent i = getIntent();
		position = i.getIntExtra("POSITION_ID", 0);
		mAllImages = i.getStringArrayExtra("IMAGE_ARRAY");
		mAllImageCatName = i.getStringArrayExtra("IMAGE_CATNAME");

		TOTAL_IMAGE = mAllImages.length - 1;
		viewpager = (ViewPager) findViewById(R.id.image_slider);
		handler = new Handler();

		PinchZoomPageAdapter adapter = new PinchZoomPageAdapter(this,
				mAllImages, mAllImageCatName);
		viewpager.setAdapter(adapter);
		viewpager.setCurrentItem(position);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		lastUpdate = System.currentTimeMillis();
		mCountPage = 0;
		mInterstitial = new InterstitialAd(this);
		mInterstitial.setAdUnitId(getResources().getString(
				R.string.admob_publisher_id));
		request = new AdRequest.Builder().addTestDevice(
				"7F0E780CC56F3BA7C1CA7488A61F46BE").build();

		mInterstitial.loadAd(request);
		mInterstitial.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				mInterstitial.loadAd(request);
			}
		});
		viewpager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				mCountPage++;
				Log.d("haipn", "page count:" + mCountPage);
				if (mCountPage >= 20) {

					if (mInterstitial.isLoaded()) {
						mInterstitial.show();
						mCountPage = 0;
					}
				}
				position = viewpager.getCurrentItem();
				Image_Url = mAllImages[position];

				List<Pojo> pojolist = db.getFavRow(Image_Url);
				if (pojolist.size() == 0) {
					menu.getItem(3).setIcon(
							getResources().getDrawable(R.drawable.fav));
				} else {
					if (pojolist.get(0).getImageurl().equals(Image_Url)) {
						menu.getItem(3).setIcon(
								getResources()
										.getDrawable(R.drawable.fav_hover));
					}

				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int position) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int position) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.photo_menu, menu);
		this.menu = menu;
		// for when 1st item of view pager is favorite mode
		FirstFav();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.menu_back:

			position = viewpager.getCurrentItem();
			position--;
			if (position < 0) {
				position = 0;
			}
			viewpager.setCurrentItem(position);

			return true;

		case R.id.menu_next:

			position = viewpager.getCurrentItem();
			position++;
			if (position == TOTAL_IMAGE) {
				position = TOTAL_IMAGE;
			}
			viewpager.setCurrentItem(position);

			return true;

		case R.id.menu_play:

			if (Play_Flag) {
				handler.removeCallbacks(Update);
				menuItem.setIcon(getResources().getDrawable(R.drawable.play));
				Play_Flag = false;
				ShowMenu();
			} else {
				/*
				 * when Play_Flag false then Play but when image is last not
				 * start auto play now hide all menu when auto play start
				 */
				if (viewpager.getCurrentItem() == TOTAL_IMAGE) {
					Toast.makeText(getApplicationContext(),
							"Currently Last Image!! Not Start Auto Play",
							Toast.LENGTH_SHORT).show();
				} else {
					AutoPlay();
					menuItem.setIcon(getResources()
							.getDrawable(R.drawable.stop));
					Play_Flag = true;
					HideMenu();
				}

			}
			return true;

		case R.id.menu_fav:

			position = viewpager.getCurrentItem();

			Image_Url = mAllImages[position];

			List<Pojo> pojolist = db.getFavRow(Image_Url);
			if (pojolist.size() == 0) {
				AddtoFav(position);// if size is zero i.e means that record not
									// in database show add to favorite
			} else {
				if (pojolist.get(0).getImageurl().equals(Image_Url)) {
					RemoveFav(position);
				}

			}

			return true;

		case R.id.menu_overflow:
			// just override click
			return true;
		case R.id.menu_make_meme:
			String ImgURL = Constant.SERVER_IMAGE_UPFOLDER_CATEGORY
					+ mAllImageCatName[position] + "/"
					+ mAllImages[viewpager.getCurrentItem()];
			Intent i = new Intent(this, MemeActivity.class);
			i.putExtra("URL", ImgURL);
			startActivity(i);
			return true;
		case R.id.menu_share:

			position = viewpager.getCurrentItem();

			(new ShareTask(SlideImageActivity.this))
					.execute(Constant.SERVER_IMAGE_UPFOLDER_CATEGORY
							+ mAllImageCatName[position] + "/"
							+ mAllImages[position]);

			return true;

		case R.id.menu_save:

			position = viewpager.getCurrentItem();

			(new SaveTask(SlideImageActivity.this, false))
					.execute(Constant.SERVER_IMAGE_UPFOLDER_CATEGORY
							+ mAllImageCatName[position] + "/"
							+ mAllImages[position]);

			return true;

		case R.id.menu_setaswallaper:

			position = viewpager.getCurrentItem();
			Intent intwall = new Intent(getApplicationContext(),
					SetAsWallpaperActivity.class);
			intwall.putExtra("WALLPAPER_IMAGE_URL", mAllImages);
			intwall.putExtra("WALLPAPER_IMAGE_CATEGORY", mAllImageCatName);
			intwall.putExtra("POSITION_ID", position);
			startActivity(intwall);

			return true;
		case R.id.menu_editbyapp:
			position = viewpager.getCurrentItem();

			(new SaveTask(SlideImageActivity.this, true))
					.execute(Constant.SERVER_IMAGE_UPFOLDER_CATEGORY
							+ mAllImageCatName[position] + "/"
							+ mAllImages[position]);
			return true;
		case R.id.menu_zoom:
			position = viewpager.getCurrentItem();
			Intent intzoom = new Intent(getApplicationContext(),
					PinchZoom.class);
			intzoom.putExtra("ZOOM_IMAGE_URL", mAllImages);
			intzoom.putExtra("ZOOM_IMAGE_CATEGORY", mAllImageCatName);
			intzoom.putExtra("POSITION_ID", position);
			startActivity(intzoom);

			return true;

		default:
			return super.onOptionsItemSelected(menuItem);
		}

	}

	// add to favorite

	public void AddtoFav(int position) {

		Image_catName = mAllImageCatName[position];
		Image_Url = mAllImages[position];

		db.AddtoFavorite(new Pojo(Image_catName, Image_Url));
		Toast.makeText(getApplicationContext(), "Added to Favorite",
				Toast.LENGTH_SHORT).show();
		menu.getItem(3).setIcon(
				getResources().getDrawable(R.drawable.fav_hover));
	}

	// remove from favorite
	public void RemoveFav(int position) {
		Image_Url = mAllImages[position];
		db.RemoveFav(new Pojo(Image_Url));
		Toast.makeText(getApplicationContext(), "Removed from Favorite",
				Toast.LENGTH_SHORT).show();
		menu.getItem(3).setIcon(getResources().getDrawable(R.drawable.fav));

	}

	// auto play slide show

	public void AutoPlay() {
		Update = new Runnable() {

			@Override
			public void run() {
				AutoPlay();
				// TODO Auto-generated method stub
				position = viewpager.getCurrentItem();
				position++;
				if (position == TOTAL_IMAGE) {
					position = TOTAL_IMAGE;
					handler.removeCallbacks(Update);// when last image play mode
													// goes to Stop
					Toast.makeText(getApplicationContext(),
							"Last Image Auto Play Stoped", Toast.LENGTH_SHORT)
							.show();
					menu.getItem(1).setIcon(
							getResources().getDrawable(R.drawable.play));
					Play_Flag = false;
					// Show All Menu when Auto Play Stop
					ShowMenu();
				}
				viewpager.setCurrentItem(position);

			}
		};

		handler.postDelayed(Update, 1500);
	}

	public void ShowMenu() {
		menu.getItem(0).setVisible(true);
		menu.getItem(2).setVisible(true);
		menu.getItem(3).setVisible(true);
		menu.getItem(4).setVisible(true);
	}

	public void HideMenu() {
		menu.getItem(0).setVisible(false);
		menu.getItem(2).setVisible(false);
		menu.getItem(3).setVisible(false);
		menu.getItem(4).setVisible(false);
	}

	public void FirstFav() {
		int first = viewpager.getCurrentItem();
		String Image_id = mAllImages[first];

		List<Pojo> pojolist = db.getFavRow(Image_id);
		if (pojolist.size() == 0) {
			menu.getItem(3).setIcon(getResources().getDrawable(R.drawable.fav));

		} else {
			if (pojolist.get(0).getImageurl().equals(Image_id)) {
				menu.getItem(3).setIcon(
						getResources().getDrawable(R.drawable.fav_hover));

			}

		}
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		public ImagePagerAdapter() {
			// TODO Auto-generated constructor stub
			ImageLoader.getInstance().init(
					ImageLoaderConfiguration
							.createDefault(getApplicationContext()));
			inflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mAllImages.length;

		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			View imageLayout = inflater.inflate(R.layout.viewpager_item,
					container, false);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			ImageLoader.getInstance().displayImage(
					Constant.SERVER_IMAGE_UPFOLDER_CATEGORY
							+ mAllImageCatName[position] + "/"
							+ mAllImages[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							Toast.makeText(SlideImageActivity.this, message,
									Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			container.addView(imageLayout, 0);
			return imageLayout;

		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}

	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 2) //
		{
			if (actualTime - lastUpdate < 200) {
				return;
			}
			lastUpdate = actualTime;
			// Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
			// .show();
			if (checkImage) {

				position = viewpager.getCurrentItem();
				viewpager.setCurrentItem(position);

			} else {

				position = viewpager.getCurrentItem();
				position++;
				if (position == TOTAL_IMAGE) {
					position = TOTAL_IMAGE;
				}
				viewpager.setCurrentItem(position);
			}
			checkImage = !checkImage;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
		if (dbManager == null) {
			dbManager = DatabaseManager.INSTANCE;
			dbManager.init(getApplicationContext());
		} else if (dbManager.isDatabaseClosed()) {
			dbManager.init(getApplicationContext());
		}
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		// unregister listener
		super.onPause();
		if (!dbManager.isDatabaseClosed())
			dbManager.closeDatabase();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(Update);
		sensorManager.unregisterListener(this);
		if (dbManager != null)
			dbManager.closeDatabase();

	}

	public class SaveTask extends AsyncTask<String, String, String> {
		private Context context;
		private ProgressDialog pDialog;
		String image_url;
		URL myFileUrl;
		String myFileUrl1;
		Bitmap bmImg = null;
		File file;
		boolean mEdit;

		public SaveTask(Context context, boolean edit) {
			this.context = context;
			mEdit = edit;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();

			pDialog = new ProgressDialog(context);
			pDialog.setMessage("Downloading Image ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub

			try {
				myFileUrl = new URL(args[0]);

				String path = myFileUrl.getPath();
				String idStr = path.substring(path.lastIndexOf('/') + 1);
				File filepath = Environment.getExternalStorageDirectory();
				File dir = new File(filepath.getAbsolutePath()
						+ "/HD Wallpaper/");
				dir.mkdirs();
				if (!dir.exists())
					dir.mkdirs();
				String fileName = idStr;
				file = new File(dir, fileName);

				// myFileUrl1 = args[0];

				HttpURLConnection conn = (HttpURLConnection) myFileUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				bmImg = BitmapFactory.decodeStream(is);

				FileOutputStream fos = new FileOutputStream(file);
				bmImg.compress(CompressFormat.JPEG, 75, fos);
				fos.flush();
				fos.close();
				if (!mEdit) {
					Intent intent = new Intent(
							"android.intent.category.LAUNCHER");
					intent.setClassName("com.facebook.katana",
							"com.facebook.katana.LoginActivity");
					startActivity(intent);
				}
				scanFile(file.getAbsolutePath(), mEdit);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String args) {
			if (!mEdit) {
				// TODO Auto-generated method stub
				Toast.makeText(SlideImageActivity.this,
						"Image Saved Succesfully HD Wallpaper/",
						Toast.LENGTH_SHORT).show();
			}
			pDialog.dismiss();
		}
	}

	private void scanFile(String path, final boolean edit) {

		MediaScannerConnection.scanFile(SlideImageActivity.this,
				new String[] { path }, null,
				new MediaScannerConnection.OnScanCompletedListener() {

					public void onScanCompleted(String path, Uri uri) {
						Log.i("TAG", "Finished scanning " + path);
						if (edit) {
							Intent editIntent = new Intent(Intent.ACTION_EDIT);
							// editIntent.setType("image/*");
							editIntent.setDataAndType(uri, "image/*");
							startActivity(Intent
									.createChooser(editIntent, null));
						}
					}
				});
	}

	public class ShareTask extends AsyncTask<String, String, String> {
		private Context context;
		private ProgressDialog pDialog;
		String image_url;
		URL myFileUrl;
		String myFileUrl1;
		Bitmap bmImg = null;
		File file;

		public ShareTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();

			pDialog = new ProgressDialog(context);
			pDialog.setMessage("Please Wait ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub

			try {

				myFileUrl = new URL(args[0]);
				// myFileUrl1 = args[0];
				String path = myFileUrl.getPath();
				String idStr = path.substring(path.lastIndexOf('/') + 1);
				File filepath = Environment.getExternalStorageDirectory();
				File dir = new File(filepath.getAbsolutePath()
						+ "/HD Wallpaper/");

				if (!dir.exists())
					dir.mkdirs();
				String fileName = idStr;
				file = new File(dir, fileName);
				if (!file.exists()) {
					HttpURLConnection conn = (HttpURLConnection) myFileUrl
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					bmImg = BitmapFactory.decodeStream(is);

					FileOutputStream fos = new FileOutputStream(file);
					bmImg.compress(CompressFormat.JPEG, 75, fos);
					fos.flush();
					fos.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String args) {
			// TODO Auto-generated method stub

			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("image/jpeg");
			share.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + file.getAbsolutePath()));
			startActivity(Intent.createChooser(share, "Share Image"));
			pDialog.dismiss();
		}
	}

}
