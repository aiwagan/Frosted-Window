package com.frostedwindow;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;

public class CapturePictureCallback implements PictureCallback {

	private View sourceView;
	private Activity activity;

	public CapturePictureCallback(Activity activity, View sourceView){
		this.activity = activity;
		this.sourceView = sourceView;
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		Bitmap picBitmap = BitmapFactory.decodeByteArray(data, 0,
				data.length);

		Bitmap flipperBitmap = Bitmap.createBitmap(
				sourceView.getWidth(), sourceView.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas flipperCanvas = new Canvas(flipperBitmap);
		sourceView.draw(flipperCanvas);
		Bitmap resizedFlipperBitmap = Bitmap.createScaledBitmap(
				flipperBitmap, picBitmap.getWidth(), picBitmap.getHeight(),
				false);

		// Insert image on top
		final Bitmap overlaidBitmap = overlay(picBitmap, resizedFlipperBitmap);

		picBitmap.recycle();
		resizedFlipperBitmap.recycle();

		// Create file
		final Bitmap shareOnFacebookBitmap = overlaidBitmap.copy(Bitmap.Config.ARGB_8888, true);
		save(overlaidBitmap);
		
		// Suggest share on facebook
		
		AlertDialog alert = new AlertDialog.Builder(activity).create();
		alert.setMessage("Nice picture! We saved it in your gallery. Would you like to share it on Facebook?");
		alert.setButton2("No",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface alert, int arg1) {
						alert.dismiss();
					}

				});

		alert.setButton("Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface alert, int arg1) {
						FacebookHandler facebookHandler = new FacebookHandler(activity);
						facebookHandler.authenticateWithFacebook(shareOnFacebookBitmap);
					}

				});

		alert.show();			
						
		
		// Restablish camera preview
		camera.startPreview();
	}

	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(),
				bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, new Matrix(), null);
		return bmOverlay;
	}
	
	private void save(Bitmap bitmap) {

		File picturesFolder = null;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
			picturesFolder = new File(
					Environment.getExternalStorageDirectory(), "Pictures");
		} else {
			picturesFolder = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		}

		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String dateString = sdf.format(date);			
		String fileName = "frostedwindow" + dateString + ".jpeg";
		File pictureFile = new File(picturesFolder, fileName);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
			bitmap.recycle();

			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
				ContentValues values = new ContentValues(7);

				values.put(Images.Media.TITLE, fileName);
				values.put(Images.Media.DISPLAY_NAME, fileName);
				values.put(Images.Media.DATE_TAKEN, dateString);
				values.put(Images.Media.MIME_TYPE, "image/jpeg");
				values.put(Images.Media.ORIENTATION, 0);
				values.put(Images.Media.DATA, pictureFile.toString());
				values.put(Images.Media.SIZE, pictureFile.length());

				Uri uri = Uri.fromFile(pictureFile);
				activity.getContentResolver().insert(uri, values);

			} else {
				MediaScannerConnection
						.scanFile(
								activity.getApplicationContext(),
								new String[] { pictureFile.toString() },
								null,
								new MediaScannerConnection.OnScanCompletedListener() {
									public void onScanCompleted(
											String path, Uri uri) {
										Log.i("ExternalStorage", "Scanned "
												+ path + ":");
										Log.i("ExternalStorage", "-> uri="
												+ uri);
									}
								});

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}	