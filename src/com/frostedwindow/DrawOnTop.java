package com.frostedwindow;

import com.frostedwindow.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class DrawOnTop extends View {

	private static final float DEFAULT_RADIUS = 12;

	private Bitmap overlay;
	private Paint pTouch;
	private Canvas c2;

	public DrawOnTop(Activity activity) {
		super(activity);

		Display display = activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		overlay = BitmapFactory.decodeResource(getResources(),
				R.drawable.transparent_layer).copy(Config.ARGB_8888, true);
		overlay = getResizedBitmap(overlay, height, width);

		c2 = new Canvas(overlay);

		pTouch = new Paint(Paint.ANTI_ALIAS_FLAG);
		pTouch.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
		pTouch.setColor(Color.TRANSPARENT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(overlay, 0, 0, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:

			final int N = event.getHistorySize();
			final int P = event.getPointerCount();
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < P; j++) {
					paint(event.getHistoricalX(j, i),
							event.getHistoricalY(j, i),
							event.getHistoricalPressure(j, i));
				}
			}
			for (int j = 0; j < P; j++) {
				paint(event.getX(j), event.getY(j), event.getPressure(j));
			}
			break;
		}

		return true;

	}

	private void paint(float x, float y, float pressure) {
		if (overlay != null) {
			pTouch.setAlpha(Math.min((int) (pressure * 128), 255));
			c2.drawCircle(x, y, DEFAULT_RADIUS, pTouch);
		}
		invalidate();
	}

	private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);

		return resizedBitmap;
	}

}