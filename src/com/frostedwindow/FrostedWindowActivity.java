package com.frostedwindow;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FrostedWindowActivity extends Activity {

	private Preview preview;
	private Camera mCamera;
	private LinearLayout frostedViewLayout;
	private Button eraseButton;
	private Button cameraButton;	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (checkCameraHardware()) {
			start();
		} else {
			Toast.makeText(this,
					"Sorry! Can't detect a camera in this device!",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void start() {
		preview = new Preview(this);
		setContentView(preview);

		View main = LayoutInflater.from(getBaseContext()).inflate(
				R.layout.main, null);

		addContentView(main, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));
		
		frostedViewLayout = (LinearLayout) main
				.findViewById(R.id.frostedViewLayout);
		frostedViewLayout.addView(new DrawOnTop(this));

		cameraButton = (Button) main.findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new CameraClickListener());
		
		eraseButton = (Button) main.findViewById(R.id.erase_button);
		eraseButton.setOnClickListener(new CameraClickListener());		
		
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mCamera != null) {
			preview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCamera = Camera.open();
		preview.setCamera(mCamera);

	}

	private boolean checkCameraHardware() {
		return getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA);
	}

	private class CameraClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(cameraButton == v){
				mCamera.takePicture(null, null, new CapturePictureCallback(FrostedWindowActivity.this, frostedViewLayout));	
			}
			else  if (eraseButton == v){
				frostedViewLayout.removeAllViews();
				frostedViewLayout.addView(new DrawOnTop(FrostedWindowActivity.this));
			}
			
		}
	}		
}