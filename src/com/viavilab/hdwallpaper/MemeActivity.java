package com.viavilab.hdwallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.arcsoft.sample.widgets.DrawableHighlightView.OnDeleteClickListener;
import com.arcsoft.sample.widgets.EditTextScaleRotateView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MemeActivity extends SherlockActivity implements
		OnFocusChangeListener, OnDeleteClickListener {

	ImageView mImvBg;
	String mUrl;
	EditText mEdtInputText;
	Button mBtnTextColor;
	Button mBtnStrokeColor;
	Button mBtnAdd;
	Button mBtnAddSticker;
	Button mBtnChange;
	FrameLayout mFlMain;
	Button mBtnSave;
	Button mBtnShare;
	Button mBtnClear;

	Spinner mSpnFont;
	ArrayList<Typeface> mListFont;
	DisplayImageOptions options;
	protected int mOriginalWidth;
	protected int mOriginalHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meme_layout);
		mImvBg = (ImageView) findViewById(R.id.imvBg);
		mUrl = getIntent().getStringExtra("URL");
		mEdtInputText = (EditText) findViewById(R.id.edtInputText);
		// new DownloadImageTask().execute(mUrl);
		mBtnAdd = (Button) findViewById(R.id.btnAdd);
		mFlMain = (FrameLayout) findViewById(R.id.flMain);
		mListFont = getFonts();
		FontAdapter adapter = new FontAdapter(mListFont, this);
		mSpnFont = (Spinner) findViewById(R.id.spnFont);
		mSpnFont.setAdapter(adapter);
		mSpnFont.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				resetText(mListFont.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mBtnSave = (Button) findViewById(R.id.btnSave);
		mBtnSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveFile();
			}
		});
		mBtnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditTextScaleRotateView e = new EditTextScaleRotateView(
						MemeActivity.this);
				// e.setOnFocusChangeListener(MemeActivity.this);
				e.setTypeface((Typeface) mSpnFont.getSelectedItem());
				e.setTextInit(mEdtInputText.getText().toString());
				e.getmHighlightView().setOnDeleteClickListener(
						MemeActivity.this);
				mFlMain.addView(e);
				setSelection();
				mEdtInputText.getText().clear();
				e.beginEditText();
			}
		});
		mBtnAddSticker = (Button) findViewById(R.id.btnAddSticker);
		mBtnAddSticker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// SingleFingerView sfv = (SingleFingerView) LayoutInflater
				// .from(MemeActivity.this)
				// .inflate(R.layout.sticker, null).findViewById(R.id.tiv);
				// mFlMain.addView(sfv);
				String file = saveFile();

				scanFile(file);

			}
		});
		mBtnTextColor = (Button) findViewById(R.id.btnTextColor);
		mBtnTextColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
							.getChildAt(mFlMain.getChildCount() - 1);
					colorpicker(e.getTextColor());
				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mBtnStrokeColor = (Button) findViewById(R.id.btnStrokeColor);
		mBtnStrokeColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
							.getChildAt(mFlMain.getChildCount() - 1);
					colorStrokepicker(e.getTextStrokeColor());
				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mBtnShare = (Button) findViewById(R.id.btnShare);
		mBtnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				shareIntent(saveFile());
			}
		});

		mBtnClear = (Button) findViewById(R.id.btnClear);
		mBtnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mFlMain.removeViews(1, mFlMain.getChildCount() - 1);
			}
		});
		if (mUrl != null) {
			ImageLoader.getInstance().init(
					ImageLoaderConfiguration
							.createDefault(getApplicationContext()));
			options = new DisplayImageOptions.Builder()
					.resetViewBeforeLoading(true).cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.considerExifParams(true)
					.displayer(new FadeInBitmapDisplayer(300)).build();
			ImageLoader.getInstance().displayImage(mUrl, mImvBg, options,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {
						}

						@Override
						public void onLoadingFailed(String arg0, View arg1,
								FailReason arg2) {
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1,
								Bitmap arg2) {
							mOriginalWidth = arg2.getWidth();
							mOriginalHeight = arg2.getHeight();
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
						}
					});
		} else {
			if (MainActivity.cropedImage != null) {
				mImvBg.setImageBitmap(MainActivity.cropedImage);
				mOriginalHeight = MainActivity.cropedImage.getHeight();
				mOriginalWidth = MainActivity.cropedImage.getWidth();
			}
		}
	}

	private void scanFile(String path) {

		MediaScannerConnection.scanFile(MemeActivity.this,
				new String[] { path }, null,
				new MediaScannerConnection.OnScanCompletedListener() {

					public void onScanCompleted(String path, Uri uri) {
						Log.i("TAG", "Finished scanning " + path);
						Intent intent = new Intent(
								"android.intent.category.LAUNCHER");
						intent.setClassName("com.facebook.katana",
								"com.facebook.katana.LoginActivity");
						startActivity(intent);
					}
				});
	}

	protected void resetText(Typeface typeface) {
		try {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(mFlMain.getChildCount() - 1);
			e.setTypeface(typeface);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.meme_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.menu_home:
			Intent i = new Intent(MemeActivity.this, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
			return true;
		}
		return false;
	}

	private ArrayList<Typeface> getFonts() {
		ArrayList<Typeface> list = new ArrayList<Typeface>();
		Typeface type1 = Typeface.createFromAsset(getAssets(), "fonts/f1.ttf");
		list.add(type1);
		Typeface type2 = Typeface.createFromAsset(getAssets(), "fonts/f2.ttf");
		list.add(type2);
		Typeface type3 = Typeface.createFromAsset(getAssets(), "fonts/f3.otf");
		list.add(type3);
		Typeface type4 = Typeface.createFromAsset(getAssets(), "fonts/f4.ttf");
		list.add(type4);
		Typeface type5 = Typeface.createFromAsset(getAssets(), "fonts/f5.ttf");
		list.add(type5);
		Typeface type6 = Typeface.createFromAsset(getAssets(), "fonts/f6.ttf");
		list.add(type6);
		Typeface type7 = Typeface.createFromAsset(getAssets(), "fonts/f7.ttf");
		list.add(type7);
		Typeface type8 = Typeface.createFromAsset(getAssets(), "fonts/f8.ttf");
		list.add(type8);
		Typeface type9 = Typeface.createFromAsset(getAssets(), "fonts/f9.ttf");
		list.add(type9);
		Typeface type10 = Typeface
				.createFromAsset(getAssets(), "fonts/f10.ttf");
		list.add(type10);
		Typeface type11 = Typeface
				.createFromAsset(getAssets(), "fonts/f11.ttf");
		list.add(type11);
		Typeface type12 = Typeface
				.createFromAsset(getAssets(), "fonts/f12.ttf");
		list.add(type12);
		Typeface type13 = Typeface
				.createFromAsset(getAssets(), "fonts/f13.ttf");
		list.add(type13);
		Typeface type14 = Typeface
				.createFromAsset(getAssets(), "fonts/f14.ttf");
		list.add(type14);
		Typeface type15 = Typeface
				.createFromAsset(getAssets(), "fonts/f15.ttf");
		list.add(type15);
		Typeface type16 = Typeface
				.createFromAsset(getAssets(), "fonts/f16.ttf");
		list.add(type16);
		Typeface type17 = Typeface
				.createFromAsset(getAssets(), "fonts/f17.ttf");
		list.add(type17);
		Typeface type18 = Typeface
				.createFromAsset(getAssets(), "fonts/f18.ttf");
		list.add(type18);
		Typeface type19 = Typeface
				.createFromAsset(getAssets(), "fonts/f19.ttf");
		list.add(type19);
		Typeface type20 = Typeface
				.createFromAsset(getAssets(), "fonts/f20.ttf");
		list.add(type20);
		Typeface type21 = Typeface
				.createFromAsset(getAssets(), "fonts/f21.ttf");
		list.add(type21);

		return list;

	}

	public void colorpicker(int startColor) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, startColor, false,
				new OnAmbilWarnaListener() {

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
					}

					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						resetTextColor(color);
					}
				});
		dialog.show();
	}

	public void colorStrokepicker(int startColor) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, startColor, false,
				new OnAmbilWarnaListener() {

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
					}

					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						resetStrokeColor(color);
					}
				});
		dialog.show();
	}

	protected void resetTextColor(int color) {
		try {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(mFlMain.getChildCount() - 1);
			e.setTextColor(color);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void resetStrokeColor(int color) {
		try {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(mFlMain.getChildCount() - 1);
			e.setTextStrokeColor(color);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void setSelection() {
		for (int i = 1; i < mFlMain.getChildCount() - 1; i++) {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(i);
			e.getmHighlightView().setSelected(false);
		}
		try {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(mFlMain.getChildCount() - 1);
			e.getmHighlightView().setSelected(true);
			e.beginEditText();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private String saveFile() {
		for (int i = 1; i < mFlMain.getChildCount(); i++) {
			EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
					.getChildAt(i);
			e.getmHighlightView().setSelected(false);
		}
		mFlMain.invalidate();
		mFlMain.setDrawingCacheEnabled(true);

		Bitmap bitmap = cropImage(mFlMain.getDrawingCache());

		File file, f = null;
		try {
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				file = new File(
						android.os.Environment.getExternalStorageDirectory(),
						"HD Wallpaper");
				if (!file.exists()) {
					file.mkdirs();

				}
				String fileName = "image_" + System.currentTimeMillis()
						+ ".jpg";
				f = new File(file.getAbsolutePath() + "/" + fileName);
			}

			FileOutputStream ostream = new FileOutputStream(f);
			bitmap.compress(CompressFormat.JPEG, 10, ostream);
			ostream.close();
			Toast.makeText(this,
					"Save file successfully\n" + f.getAbsolutePath(),
					Toast.LENGTH_SHORT).show();
			try {
				EditTextScaleRotateView e = (EditTextScaleRotateView) mFlMain
						.getChildAt(mFlMain.getChildCount() - 1);
				e.getmHighlightView().setSelected(true);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
			return f.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Bitmap cropImage(Bitmap srcBmp) {
		Bitmap dstBmp;
		Matrix m = new Matrix();
		float ratioWidth = srcBmp.getWidth() * 1f / mOriginalWidth * 1f;
		float ratioHeight = srcBmp.getHeight() * 1f / mOriginalHeight * 1f;
		if (ratioWidth < ratioHeight) {
			dstBmp = Bitmap.createBitmap(srcBmp, 0, srcBmp.getHeight() / 2
					- (int) (mOriginalHeight * ratioWidth) / 2,
					(int) (mOriginalWidth * ratioWidth),
					(int) (mOriginalHeight * ratioWidth), m, true);
		} else {
			dstBmp = Bitmap.createBitmap(srcBmp, srcBmp.getWidth() / 2
					- (int) (mOriginalWidth * ratioHeight) / 2, 0,
					(int) (mOriginalWidth * ratioHeight),
					(int) (mOriginalHeight * ratioHeight), m, true);
		}

		return dstBmp;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void shareIntent(String path) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		Uri phototUri = Uri.fromFile(new File(path));
		shareIntent.setData(phototUri);
		shareIntent.setType("image/*");
		shareIntent.putExtra(Intent.EXTRA_STREAM, phototUri);
		startActivity(shareIntent);
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = (InputStream) new URL(urldisplay).getContent();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", "image download error");
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			mImvBg.setImageBitmap(result);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		EditTextScaleRotateView mTextView = (EditTextScaleRotateView) v;
		if (hasFocus) {
			mTextView.getmHighlightView().setSelected(true);
		} else {
			mTextView.getmHighlightView().setSelected(false);
		}
	}

	@Override
	public void onDeleteClick() {
		Log.d("haipn", "on delete click");
		mFlMain.removeViewAt(mFlMain.getChildCount() - 1);
		setSelection();
	}

}