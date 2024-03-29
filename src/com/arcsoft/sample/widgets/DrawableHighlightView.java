package com.arcsoft.sample.widgets;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.arcsoft.sample.graphics.BaseDrawable;
import com.arcsoft.sample.graphics.EditableDrawable;
import com.arcsoft.sample.graphics.Point2D;
import com.viavilab.hdwallpaper.R;

/**
 * The Class DrawableHighlightView.
 */
public class DrawableHighlightView {

	/**
	 * The Enum Mode.
	 */
	public enum Mode {

		/** The None. */
		None,
		/** The Move. */
		Move,
		/** The Grow. */
		Grow,
		/** The Rotate. */
		Rotate
	};

	/**
	 * The Enum AlignModeV.
	 */
	public enum AlignModeV {

		/** The Top. */
		Top,
		/** The Bottom. */
		Bottom,
		/** The Center. */
		Center
	};

	/**
	 * The listener interface for receiving onDeleteClick events. The class that
	 * is interested in processing a onDeleteClick event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addOnDeleteClickListener<code> method. When
	 * the onDeleteClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnDeleteClickEvent
	 */
	public interface OnDeleteClickListener {

		/**
		 * On delete click.
		 */
		void onDeleteClick();
	}

	/** The m delete click listener. */
	private OnDeleteClickListener mDeleteClickListener;

	/** The Constant GROW_NONE. */
	public static final int GROW_NONE = 1 << 0; // 1

	/** The Constant GROW_LEFT_EDGE. */
	public static final int GROW_LEFT_EDGE = 1 << 1; // 2

	/** The Constant GROW_RIGHT_EDGE. */
	public static final int GROW_RIGHT_EDGE = 1 << 2; // 4

	/** The Constant GROW_TOP_EDGE. */
	public static final int GROW_TOP_EDGE = 1 << 3; // 8

	/** The Constant GROW_BOTTOM_EDGE. */
	public static final int GROW_BOTTOM_EDGE = 1 << 4; // 16

	/** The Constant ROTATE. */
	public static final int ROTATE = 1 << 5; // 32

	/** The Constant MOVE. */
	public static final int MOVE = 1 << 6; // 64

	// tolerance for buttons hits
	/** The Constant HIT_TOLERANCE. */
	private static final float HIT_TOLERANCE = 40f;

	/** The m hidden. */
	private boolean mHidden;

	/** The m context. */
	private View mContext;

	/** The m mode. */
	private Mode mMode;

	/** The m selected. */
	private boolean mSelected;

	/** The m draw rect. */
	private RectF mDrawRect;

	/** The m crop rect. */
	private RectF mCropRect;

	/** The m matrix. */
	private Matrix mMatrix;

	/** The m content. */
	private final BaseDrawable mContent;

	/** The m anchor rotate. */
	private Drawable mAnchorRotate;

	/** The m anchor delete. */
	private Drawable mAnchorDelete;

	/** The m anchor width. */
	private int mAnchorWidth;

	/** The m anchor height. */
	private int mAnchorHeight;

	/** The m outline stroke color. */
	private int mOutlineStrokeColor;

	/** The m outline stroke color pressed. */
	private int mOutlineStrokeColorPressed;

	/** The m rotate and scale. */
	private boolean mRotateAndScale;

	/** The m show delete button. */
	private boolean mShowDeleteButton = true;

	/** The m rotation. */
	private float mRotation = 0;

	/** The m ratio. */
	private float mRatio = 1f;

	/** The m min height. */
	private float mMinWidth, mMinHeight;

	/** The m rotate matrix. */
	private Matrix mRotateMatrix = new Matrix();

	/** The fpoints. */
	private final float fpoints[] = new float[] { 0, 0 };

	/** The m draw outline stroke. */
	private boolean mDrawOutlineStroke = true;

	/** The m draw outline fill. */
	private boolean mDrawOutlineFill = true;

	/** The m outline stroke paint. */
	private Paint mOutlineStrokePaint;

	/** The m outline fill paint. */
	private Paint mOutlineFillPaint;

	/** The m outline fill color normal. */
	private int mOutlineFillColorNormal = 0x66000000;

	/** The m outline fill color pressed. */
	private int mOutlineFillColorPressed = 0x66a5a5a5;

	/** The m outline ellipse. */
	private int mOutlineEllipse = 0;

	/** The m padding. */
	private int mPadding = 0;

	/** The m show anchors. */
	private boolean mShowAnchors = true;

	/** The m align vertical mode. */
	private AlignModeV mAlignVerticalMode = AlignModeV.Center;

	/**
	 * Instantiates a new drawable highlight view.
	 * 
	 * @param ctx
	 *            the ctx
	 * @param content
	 *            the content
	 */
	public DrawableHighlightView(final View ctx, final BaseDrawable content) {
		mContext = ctx;
		mContent = content;
		updateRatio();
		setMinSize(20f);
	}

	/**
	 * Sets the align mode v.
	 * 
	 * @param mode
	 *            the new align mode v
	 */
	public void setAlignModeV(AlignModeV mode) {
		mAlignVerticalMode = mode;
	}

	/**
	 * Request a layout update.
	 * 
	 * @return the rect f
	 */
	protected RectF computeLayout() {
		return getDisplayRect(mMatrix, mCropRect);
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		mContext = null;
		mDeleteClickListener = null;
	}

	/** The m outline path. */
	private Path mOutlinePath = new Path();

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *            the canvas
	 */
	protected void draw(final Canvas canvas) {
		if (mHidden)
			return;

		RectF drawRectF = new RectF(mDrawRect);
		drawRectF.inset(-mPadding, -mPadding);

		final int saveCount = canvas.save();
		canvas.concat(mRotateMatrix);

		if (mSelected) {
			mOutlinePath.reset();
			mOutlinePath.addRoundRect(drawRectF, mOutlineEllipse,
					mOutlineEllipse, Path.Direction.CW);

			if (mDrawOutlineFill) {
				canvas.drawPath(mOutlinePath, mOutlineFillPaint);
			}
			if (mDrawOutlineStroke) {
				canvas.drawPath(mOutlinePath, mOutlineStrokePaint);
			}
		}

		if (mContent instanceof EditableDrawable)
			((EditableDrawable) mContent).setBounds(mDrawRect.left,
					mDrawRect.top, mDrawRect.right, mDrawRect.bottom);
		else
			mContent.setBounds((int) mDrawRect.left, (int) mDrawRect.top,
					(int) mDrawRect.right, (int) mDrawRect.bottom);

		mContent.draw(canvas);

		if (mSelected) {

			if (mShowAnchors) {
				final int left = (int) (drawRectF.left);
				final int right = (int) (drawRectF.right);
				final int top = (int) (drawRectF.top);
				final int bottom = (int) (drawRectF.bottom);

				if (mAnchorRotate != null) {
					mAnchorRotate.setBounds(right - mAnchorWidth, bottom
							- mAnchorHeight, right + mAnchorWidth, bottom
							+ mAnchorHeight);
					mAnchorRotate.draw(canvas);
				}

				if ((mAnchorDelete != null) && mShowDeleteButton) {
					mAnchorDelete.setBounds(left - mAnchorWidth, top
							- mAnchorHeight, left + mAnchorWidth, top
							+ mAnchorHeight);
					mAnchorDelete.draw(canvas);
				}
			}
		}

		canvas.restoreToCount(saveCount);

		Log.i("XXXXX", "highlightview,draw mSelected: " + mSelected);
		if (mContent instanceof EditableDrawable && mSelected) {
			if (((EditableDrawable) mContent).isEditing()) {
				Log.i("XXXXX", "highlightview,draw : isEditing");
				mContext.postInvalidateDelayed(300L);
			}
		}
	}

	/**
	 * Show anchors.
	 * 
	 * @param value
	 *            the value
	 */
	public void showAnchors(boolean value) {
		mShowAnchors = value;
	}

	/**
	 * Draw.
	 * 
	 * @param canvas
	 *            the canvas
	 * @param source
	 *            the source
	 */
	public void draw(final Canvas canvas, final Matrix source) {

		final Matrix matrix = new Matrix(source);
		matrix.invert(matrix);

		final int saveCount = canvas.save();
		canvas.concat(matrix);
		canvas.concat(mRotateMatrix);

		mContent.setBounds((int) mDrawRect.left, (int) mDrawRect.top,
				(int) mDrawRect.right, (int) mDrawRect.bottom);
		mContent.draw(canvas);

		canvas.restoreToCount(saveCount);
	}

	/**
	 * Returns the cropping rectangle in image space.
	 * 
	 * @return the crop rect
	 */
	public Rect getCropRect() {
		return new Rect((int) mCropRect.left, (int) mCropRect.top,
				(int) mCropRect.right, (int) mCropRect.bottom);
	}

	/**
	 * Gets the crop rect f.
	 * 
	 * @return the crop rect f
	 */
	public RectF getCropRectF() {
		return mCropRect;
	}

	/**
	 * Gets the crop rotation matrix.
	 * 
	 * @return the crop rotation matrix
	 */
	public Matrix getCropRotationMatrix() {
		final Matrix m = new Matrix();
		m.postTranslate(-mCropRect.centerX(), -mCropRect.centerY());
		m.postRotate(mRotation);
		m.postTranslate(mCropRect.centerX(), mCropRect.centerY());
		return m;
	}

	/**
	 * Gets the display rect.
	 * 
	 * @param m
	 *            the m
	 * @param supportRect
	 *            the support rect
	 * @return the display rect
	 */
	protected RectF getDisplayRect(final Matrix m, final RectF supportRect) {
		final RectF r = new RectF(supportRect);
		m.mapRect(r);
		return r;
	}

	/**
	 * Gets the display rect f.
	 * 
	 * @return the display rect f
	 */
	public RectF getDisplayRectF() {
		final RectF r = new RectF(mDrawRect);
		mRotateMatrix.mapRect(r);
		return r;
	}

	/**
	 * Gets the draw rect.
	 * 
	 * @return the draw rect
	 */
	public RectF getDrawRect() {
		return mDrawRect;
	}

	/**
	 * Gets the hit.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the hit
	 */
	public int getHit(float x, float y) {

		final RectF rect = new RectF(mDrawRect);
		rect.inset(-mPadding, -mPadding);

		final float pts[] = new float[] { x, y };

		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postTranslate(-rect.centerX(), -rect.centerY());
		rotateMatrix.postRotate(-mRotation);
		rotateMatrix.postTranslate(rect.centerX(), rect.centerY());
		rotateMatrix.mapPoints(pts);

		x = pts[0];
		y = pts[1];

		mContext.invalidate();

		int retval = DrawableHighlightView.GROW_NONE;
		final boolean verticalCheck = (y >= (rect.top - DrawableHighlightView.HIT_TOLERANCE))
				&& (y < (rect.bottom + DrawableHighlightView.HIT_TOLERANCE));
		final boolean horizCheck = (x >= (rect.left - DrawableHighlightView.HIT_TOLERANCE))
				&& (x < (rect.right + DrawableHighlightView.HIT_TOLERANCE));

		if (!mRotateAndScale) {
			if ((Math.abs(rect.left - x) < DrawableHighlightView.HIT_TOLERANCE)
					&& verticalCheck)
				retval |= DrawableHighlightView.GROW_LEFT_EDGE;
			if ((Math.abs(rect.right - x) < DrawableHighlightView.HIT_TOLERANCE)
					&& verticalCheck)
				retval |= DrawableHighlightView.GROW_RIGHT_EDGE;
			if ((Math.abs(rect.top - y) < DrawableHighlightView.HIT_TOLERANCE)
					&& horizCheck)
				retval |= DrawableHighlightView.GROW_TOP_EDGE;
			if ((Math.abs(rect.bottom - y) < DrawableHighlightView.HIT_TOLERANCE)
					&& horizCheck)
				retval |= DrawableHighlightView.GROW_BOTTOM_EDGE;
		}

		if ((Math.abs(rect.right - x) < DrawableHighlightView.HIT_TOLERANCE)
				&& (Math.abs(rect.bottom - y) < DrawableHighlightView.HIT_TOLERANCE)
				&& verticalCheck && horizCheck)
			retval = DrawableHighlightView.ROTATE;

		if ((retval == DrawableHighlightView.GROW_NONE)
				&& rect.contains((int) x, (int) y))
			retval = DrawableHighlightView.MOVE;
		return retval;
	}

	/**
	 * On single tap confirmed.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public void onSingleTapConfirmed(float x, float y) {

		final RectF rect = new RectF(mDrawRect);
		rect.inset(-mPadding, -mPadding);

		final float pts[] = new float[] { x, y };

		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postTranslate(-rect.centerX(), -rect.centerY());
		rotateMatrix.postRotate(-mRotation);
		rotateMatrix.postTranslate(rect.centerX(), rect.centerY());
		rotateMatrix.mapPoints(pts);

		x = pts[0];
		y = pts[1];

		mContext.invalidate();

		final boolean verticalCheck = (y >= (rect.top - DrawableHighlightView.HIT_TOLERANCE))
				&& (y < (rect.bottom + DrawableHighlightView.HIT_TOLERANCE));
		final boolean horizCheck = (x >= (rect.left - DrawableHighlightView.HIT_TOLERANCE))
				&& (x < (rect.right + DrawableHighlightView.HIT_TOLERANCE));

		if (mShowDeleteButton)
			if ((Math.abs(rect.left - x) < DrawableHighlightView.HIT_TOLERANCE)
					&& (Math.abs(rect.top - y) < DrawableHighlightView.HIT_TOLERANCE)
					&& verticalCheck && horizCheck)
				if (mDeleteClickListener != null) {
					mDeleteClickListener.onDeleteClick();
				}
	}

	/**
	 * Gets the invalidation rect.
	 * 
	 * @return the invalidation rect
	 */
	protected Rect getInvalidationRect() {
		final RectF r = new RectF(mDrawRect);
		r.inset(-mPadding, -mPadding);
		mRotateMatrix.mapRect(r);

		final Rect rect = new Rect((int) r.left, (int) r.top, (int) r.right,
				(int) r.bottom);
		rect.inset(-mAnchorWidth * 2, -mAnchorHeight * 2);
		return rect;
	}

	/**
	 * Gets the matrix.
	 * 
	 * @return the matrix
	 */
	public Matrix getMatrix() {
		return mMatrix;
	}

	/**
	 * Gets the mode.
	 * 
	 * @return the mode
	 */
	public Mode getMode() {
		return mMode;
	}

	/**
	 * Gets the rotation.
	 * 
	 * @return the rotation
	 */
	public float getRotation() {
		return mRotation;
	}

	public Matrix getRotationMatrix() {
		return mRotateMatrix;
	}

	/**
	 * Gets the selected.
	 * 
	 * @return the selected
	 */
	public boolean getSelected() {
		return mSelected;
	}

	/**
	 * Increase the size of the View.
	 * 
	 * @param dx
	 *            the dx
	 */
	protected void growBy(final float dx) {
		growBy(dx, dx / mRatio, true);
	}

	/**
	 * Increase the size of the View.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param checkMinSize
	 *            the check min size
	 */
	protected void growBy(final float dx, final float dy, boolean checkMinSize) {
		final RectF r = new RectF(mCropRect);

		if (mAlignVerticalMode == AlignModeV.Center) {
			r.inset(-dx, -dy);
		} else if (mAlignVerticalMode == AlignModeV.Top) {
			r.inset(-dx, 0);
			r.bottom += dy * 2;
		} else {
			r.inset(-dx, 0);
			r.top -= dy * 2;
		}

		RectF testRect = getDisplayRect(mMatrix, r);
		if (!mContent.validateSize(testRect) && checkMinSize)
			return;

		/*
		 * if (checkMinSize) { if ((r.width() < mMinWidth) || (r.height() <
		 * mMinHeight)) { final float diffw = mMinWidth - r.width(); final float
		 * diffh = mMinHeight - r.height(); r.inset(-diffw, -diffh);
		 * Logger.error(this, "restored min sizes");
		 * 
		 * invalidate(); mContext.invalidate();
		 * 
		 * return; } }
		 */
		mCropRect.set(r);
		invalidate();
		mContext.invalidate();
	}

	/**
	 * On mouse move.
	 * 
	 * @param edge
	 *            the edge
	 * @param event2
	 *            the event2
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 */
	void onMouseMove(int edge, MotionEvent event2, float dx, float dy) {
		if (edge == GROW_NONE) {
			return;
		}

		fpoints[0] = dx;
		fpoints[1] = dy;

		float xDelta;
		@SuppressWarnings("unused")
		float yDelta;

		if (edge == MOVE) {
			moveBy(dx * (mCropRect.width() / mDrawRect.width()), dy
					* (mCropRect.height() / mDrawRect.height()));
		} else if (edge == ROTATE) {
			dx = fpoints[0];
			dy = fpoints[1];
			xDelta = dx * (mCropRect.width() / mDrawRect.width());
			yDelta = dy * (mCropRect.height() / mDrawRect.height());
			rotateBy(event2.getX(), event2.getY(), dx, dy);

			invalidate();
			mContext.invalidate(getInvalidationRect());
		} else {

			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(-mRotation);
			rotateMatrix.mapPoints(fpoints);
			dx = fpoints[0];
			dy = fpoints[1];

			if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0)
				dx = 0;
			if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0)
				dy = 0;

			xDelta = dx * (mCropRect.width() / mDrawRect.width());
			yDelta = dy * (mCropRect.height() / mDrawRect.height());
			growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta);

			invalidate();
			mContext.invalidate(getInvalidationRect());
		}
	}

	/**
	 * Inits the.
	 */
	private void init() {

		final android.content.res.Resources resources = mContext.getResources();
		mAnchorRotate = resources
				.getDrawable(R.drawable.ic_rotate_scale_control);
		mAnchorDelete = resources.getDrawable(com.viavilab.hdwallpaper.R.drawable.ic_delete_button);

		mAnchorWidth = mAnchorRotate.getIntrinsicWidth() / 2;
		mAnchorHeight = mAnchorRotate.getIntrinsicHeight() / 2;

		mOutlineStrokeColor = 0xFFA0CE00;
		mOutlineStrokeColorPressed = 0xFFFFA500;

		mOutlineStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOutlineStrokePaint.setStrokeWidth(2.0f);
		mOutlineStrokePaint.setStyle(Paint.Style.STROKE);
		mOutlineStrokePaint.setColor(mOutlineStrokeColor);

		mOutlineFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mOutlineFillPaint.setStyle(Paint.Style.FILL);
		mOutlineFillPaint.setColor(mOutlineFillColorNormal);

		setMode(Mode.None);
	}

	/**
	 * Invalidate.
	 */
	public void invalidate() {
		mDrawRect = computeLayout(); // true

		mRotateMatrix.reset();
		mRotateMatrix.postTranslate(-mDrawRect.centerX(), -mDrawRect.centerY());
		mRotateMatrix.postRotate(mRotation);
		mRotateMatrix.postTranslate(mDrawRect.centerX(), mDrawRect.centerY());
	}

	/**
	 * Move by.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 */
	void moveBy(final float dx, final float dy) {
		mCropRect.offset(dx, dy);
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Rotate by.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param diffx
	 *            the diffx
	 * @param diffy
	 *            the diffy
	 */
	void rotateBy(final float dx, final float dy, float diffx, float diffy) {
		final float pt1[] = new float[] { mDrawRect.centerX(),
				mDrawRect.centerY() };
		final float pt2[] = new float[] { mDrawRect.right, mDrawRect.bottom };
		final float pt3[] = new float[] { dx, dy };

		final double angle1 = Point2D.angleBetweenPoints(pt2, pt1);
		final double angle2 = Point2D.angleBetweenPoints(pt3, pt1);

		if (!mRotateAndScale)
			mRotation = -(float) (angle2 - angle1);
		final Matrix rotateMatrix = new Matrix();
		rotateMatrix.postRotate(-mRotation);

		if (mRotateAndScale) {
			final float points[] = new float[] { diffx, diffy };

			rotateMatrix.mapPoints(points);
			diffx = points[0];
			diffy = points[1];

			final float xDelta = diffx
					* (mCropRect.width() / mDrawRect.width());
			final float yDelta = diffy
					* (mCropRect.height() / mDrawRect.height());

			final float pt4[] = new float[] { mDrawRect.right + xDelta,
					mDrawRect.bottom + yDelta };
			final double distance1 = Point2D.distance(pt1, pt2);
			final double distance2 = Point2D.distance(pt1, pt4);
			final float distance = (float) (distance2 - distance1);

			mRotation = -(float) (angle2 - angle1);
			growBy(distance);
		}
	}

	/**
	 * Toggle visibility to the current View.
	 * 
	 * @param hidden
	 *            the new hidden
	 */
	public void setHidden(final boolean hidden) {
		mHidden = hidden;
	}

	/**
	 * Sets the min size.
	 * 
	 * @param size
	 *            the new min size
	 */
	public void setMinSize(final float size) {
		if (mRatio >= 1) {
			mMinWidth = size;
			mMinHeight = mMinWidth / mRatio;
			mContent.setMinSize(size, size / mRatio);
		} else {
			mMinHeight = size;
			mMinWidth = mMinHeight * mRatio;
			mContent.setMinSize(size * mRatio, size);
		}
	}

	/**
	 * Sets the mode.
	 * 
	 * @param mode
	 *            the new mode
	 */
	public void setMode(final Mode mode) {
		if (mode != mMode) {
			mMode = mode;

			mOutlineStrokePaint
					.setColor(mMode != Mode.None ? mOutlineStrokeColorPressed
							: mOutlineStrokeColor);
			mOutlineFillPaint
					.setColor(mMode == Mode.None ? mOutlineFillColorNormal
							: mOutlineFillColorPressed);
			mContext.invalidate();
		}
	}

	/**
	 * Sets the on delete click listener.
	 * 
	 * @param listener
	 *            the new on delete click listener
	 */
	public void setOnDeleteClickListener(final OnDeleteClickListener listener) {
		mDeleteClickListener = listener;
	}

	/**
	 * Sets the rotate and scale.
	 * 
	 * @param value
	 *            the new rotate and scale
	 */
	public void setRotateAndScale(final boolean value) {
		mRotateAndScale = value;
	}

	public void setRotate(final float degree) {
		mRotation = degree;
	}

	public float getRotate() {
		return mRotation;
	}

	/**
	 * Show delete.
	 * 
	 * @param value
	 *            the value
	 */
	public void showDelete(boolean value) {
		mShowDeleteButton = value;
	}

	/**
	 * Sets the selected.
	 * 
	 * @param selected
	 *            the new selected
	 */
	public void setSelected(final boolean selected) {
		if (mSelected != selected) {
			mSelected = selected;
		}

		mContext.invalidate();
	}

	/**
	 * Setup.
	 * 
	 * @param m
	 *            the m
	 * @param imageRect
	 *            the image rect
	 * @param cropRect
	 *            the crop rect
	 * @param maintainAspectRatio
	 *            the maintain aspect ratio
	 */
	public void setup(final Matrix m, final Rect imageRect,
			final RectF cropRect, final boolean maintainAspectRatio) {
		init();
		mMatrix = new Matrix(m);
		mRotation = 0;
		mRotateMatrix = new Matrix();
		mCropRect = cropRect;
		invalidate();
	}

	/**
	 * Update.
	 * 
	 * @param imageMatrix
	 *            the image matrix
	 * @param imageRect
	 *            the image rect
	 */
	public void update(final Matrix imageMatrix, final Rect imageRect) {
		setMode(Mode.None);
		mMatrix = new Matrix(imageMatrix);
		mRotation = 0;
		mRotateMatrix = new Matrix();
		invalidate();
	}

	/**
	 * Draw outline stroke.
	 * 
	 * @param value
	 *            the value
	 */
	public void drawOutlineStroke(boolean value) {
		mDrawOutlineStroke = value;
	}

	/**
	 * Draw outline fill.
	 * 
	 * @param value
	 *            the value
	 */
	public void drawOutlineFill(boolean value) {
		mDrawOutlineFill = value;
	}

	/**
	 * Gets the outline stroke paint.
	 * 
	 * @return the outline stroke paint
	 */
	public Paint getOutlineStrokePaint() {
		return mOutlineStrokePaint;
	}

	/**
	 * Gets the outline fill paint.
	 * 
	 * @return the outline fill paint
	 */
	public Paint getOutlineFillPaint() {
		return mOutlineFillPaint;
	}

	/**
	 * Sets the outline fill color.
	 * 
	 * @param color
	 *            the new outline fill color
	 */
	public void setOutlineFillColor(int color) {
		mOutlineFillColorNormal = color;
		mOutlineFillPaint.setColor(mMode == Mode.None ? mOutlineFillColorNormal
				: mOutlineFillColorPressed);
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Sets the outline fill color pressed.
	 * 
	 * @param color
	 *            the new outline fill color pressed
	 */
	public void setOutlineFillColorPressed(int color) {
		mOutlineFillColorPressed = color;
		mOutlineFillPaint.setColor(mMode == Mode.None ? mOutlineFillColorNormal
				: mOutlineFillColorPressed);
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Sets the outline ellipse.
	 * 
	 * @param value
	 *            the new outline ellipse
	 */
	public void setOutlineEllipse(int value) {
		mOutlineEllipse = value;
		invalidate();
		mContext.invalidate();
	}

	public int getOutlineEllipse() {
		return mOutlineEllipse;
	}

	/**
	 * Sets the outline stroke color.
	 * 
	 * @param color
	 *            the new outline stroke color
	 */
	public void setOutlineStrokeColor(int color) {
		mOutlineStrokeColor = color;
		mOutlineStrokePaint.setColor(mOutlineStrokeColor);
		mOutlineStrokePaint
				.setColor(mMode != Mode.None ? mOutlineStrokeColorPressed
						: mOutlineStrokeColor);
		invalidate();
		mContext.invalidate();
	}

	public int getOutlineStrokeColor() {
		return mOutlineStrokeColor;
	}

	/**
	 * Sets the outline stroke color pressed.
	 * 
	 * @param color
	 *            the new outline stroke color pressed
	 */
	public void setOutlineStrokeColorPressed(int color) {
		mOutlineStrokeColorPressed = color;
		mOutlineStrokePaint
				.setColor(mMode != Mode.None ? mOutlineStrokeColorPressed
						: mOutlineStrokeColor);
		invalidate();
		mContext.invalidate();
	}

	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	public BaseDrawable getContent() {
		return mContent;
	}

	/**
	 * Update ratio.
	 */
	private void updateRatio() {
		final int w = mContent.getIntrinsicWidth();
		final int h = mContent.getIntrinsicHeight();
		mRatio = (float) w / (float) h;
	}

	/**
	 * Force update.
	 */
	public void forceUpdate() {
		RectF cropRect = getCropRectF();
		RectF drawRect = getDrawRect();

		if (mContent instanceof EditableDrawable) {

			final int textWidth = mContent.getIntrinsicWidth();
			final int textHeight = mContent.getIntrinsicHeight();

			updateRatio();

			RectF textRect = new RectF(cropRect);
			getMatrix().mapRect(textRect);

			float dx = textWidth - textRect.width();
			float dy = textHeight - textRect.height();

			float[] fpoints = new float[] { dx, dy };

			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(-mRotation);

			dx = fpoints[0];
			dy = fpoints[1];

			float xDelta = dx * (cropRect.width() / drawRect.width());
			float yDelta = dy * (cropRect.height() / drawRect.height());

			if (xDelta != 0 || yDelta != 0) {
				growBy(xDelta / 2, yDelta / 2, false);
			}

			invalidate();
			mContext.invalidate(getInvalidationRect());
		}
	}

	/**
	 * Sets the padding.
	 * 
	 * @param value
	 *            the new padding
	 */
	public void setPadding(int value) {
		mPadding = value;
	}

	public int getPadding() {
		return mPadding;
	}
}
