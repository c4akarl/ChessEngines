package ccc.chess.engines;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		webView = findViewById(R.id.info);
		webView.loadUrl(engineInfo);
		engineTitle = findViewById(R.id.engineTitle);
		engineTitle.setText("Chess Engines  " + BuildConfig.VERSION_NAME);

//		getPermissions();

		skipExistingB = false;

		setPathValues();
		copyAssets();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		finish();
	}

	private void copyAssets() {

		Toast.makeText(getApplicationContext(), "Copy data (books, personalities)", Toast.LENGTH_SHORT).show();

		fromAssetsToExternalStorage(A_RODENT4_BOOKS, PATH_FILES_BOOKS);
		fromAssetsToExternalStorage(A_RODENT4_PERSONALITIES, PATH_FILES_PERSONALITIES);
	}

	private void fromAssetsToExternalStorage(String assetsPath, String storagePath) {

//		Log.i(TAG, "fromAssetsToExternalStorage(), assetsPath: " + assetsPath);
//		Log.i(TAG, "fromAssetsToExternalStorage(), storagePath: " + storagePath);

		File dir = new File(storagePath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Toast.makeText(getApplicationContext(), "fromAssetsToExternalStorage(), Error mkdirs, storagePath: " + storagePath, Toast.LENGTH_LONG).show();

//				Log.i(TAG, "fromAssetsToExternalStorage(), Error mkdirs, storagePath: " + storagePath);

				return;
			}
		}

		AssetManager assetManager = this.getAssets();
		String assets[] = null;
		try {
			assets = assetManager.list(assetsPath);

//			Log.i(TAG, "copyFileOrDir(), assets.length: " + assets.length);

			for (int i = 0; i < assets.length; ++i) {
				String assetsFileName = assetsPath + "/" + assets[i];
				if (assetsFileName.contains(".")) {
					copyFile(assetsFileName, storagePath + "/" + assets[i]);
					if (copyErrorB) {
						Toast.makeText(getApplicationContext(),"Error writing " + assetsFileName,
								Toast.LENGTH_LONG).show();
						return; // stop after the first write-error
					}
				}
			}

		} catch (IOException ex) {

//			Log.i(TAG, "fromAssetsToExternalStorage(), I/O Exception", ex);

		}
	}

	private void copyFile(String fileName, String toFile) throws IOException {

//		Log.i(TAG, "copyFile(), assets: " + fileName + ", storage: " + toFile);

		AssetManager assetManager = this.getAssets();
		File file = new File(toFile);

		//karl skipExistingB == false   ???
		if(file.exists() && skipExistingB) {

			Toast.makeText(getApplicationContext(),"Already exist: " + toFile, Toast.LENGTH_SHORT).show();

			return;
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(fileName);
			out = new FileOutputStream(toFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			if (toFile.substring(toFile.length()-4).equals(".bin"))
				Toast.makeText(getApplicationContext(),"Written " + toFile, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {

//			Log.i(TAG, e.getMessage());

			copyErrorB = true;
			throw e;
		}
	}

	//karl for getExternalFilesDir ?
//	public void getPermissions()
//	{
//
////		Log.i(TAG, "1 getPermissions(), storagePermissions: " + storagePermission);
//
//		if (storagePermission == PermissionState.UNKNOWN) {
//			String extStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//			if (ContextCompat.checkSelfPermission(this, extStorage) ==
//					PackageManager.PERMISSION_GRANTED) {
//				storagePermission = PermissionState.GRANTED;
//
//				Log.i(TAG, "getPermissions(), storagePermissions: " + storagePermission);
//
//			} else {
//				storagePermission = PermissionState.REQUESTED;
//
////				Log.i(TAG, "3 getPermissions(), storagePermissions: " + storagePermission);
//
//				ActivityCompat.requestPermissions(this, new String[]{extStorage}, PERMISSIONS_REQUEST_CODE);
//			}
//		}
//
////		Log.i(TAG, "getPermissions(), storagePermissions: " + storagePermission);
//
//	}
//
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
//	{
//		switch (requestCode)
//		{
//			case PERMISSIONS_REQUEST_CODE:
//				if (grantResults.length > 0)
//				{
//					for (int i = 0; i < grantResults.length; i++)
//					{
//						if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
//							Log.i(TAG, permissions[i] + " denied");
//
//						if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED)
//							storagePermission = PermissionState.GRANTED;
//					}
//				}
//				if (storageAvailable()) {
//					if (setPathValues()) {
//						copyAssets();
//					}
//				}
//				break;
//		}
//	}

	//karl return ???
	private void setPathValues() {

		PATH_FILES = String.valueOf(getExternalFilesDir(null));	// 		/storage/emulated/0/Android/data/ccc.chess.engines/files
		PATH_FILES_BOOKS = PATH_FILES + "/books";
		PATH_FILES_PERSONALITIES = PATH_FILES + "/personalities";
		PATH_FILES_GTB = PATH_FILES + "/gtb";
		PATH_FILES_RTB = PATH_FILES + "/rtb";

//		Log.i(TAG, "setPathValues(), PATH_FILES_BOOKS: " + PATH_FILES_BOOKS);

//		return true;
	}

	//karl not needed for getExternalFilesDir() !?
//	public Boolean isSdk29()
//	{
//		int deviceSdk = android.os.Build.VERSION.SDK_INT;
//		int targetSdk = getApplicationContext().getApplicationInfo().targetSdkVersion;
//
//		Log.i(TAG, "isSdk29(), sdkInt: " + deviceSdk + ", targetSdk: " + targetSdk);
//
//		return deviceSdk >= 29 && targetSdk >= 29;
//	}
//	private void askAndroid10Perm()
//	{
//		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//		intent.addFlags(
//		Intent.FLAG_GRANT_READ_URI_PERMISSION
//				| Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//				| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//				| Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
//		startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
//	}
//
//	private boolean storageAvailable() {
//		return storagePermission == PermissionState.GRANTED;
//	}

//	final String TAG = "MainActivity";
	WebView webView;
	final String engineInfo = "file:///android_res/raw/engine_info";

	// assets directories
	String A_RODENT4_BOOKS = "Rodent4/books";
	String A_RODENT4_PERSONALITIES = "Rodent4/personalities";

	// getExternalFilesDir() directories
	String PATH_FILES = "";
	String PATH_FILES_BOOKS = "";
	String PATH_FILES_PERSONALITIES = "";
	//karl TableBases ???
	String PATH_FILES_GTB = "";
	String PATH_FILES_RTB = "";

	TextView engineTitle = null;

	private boolean copyErrorB;
	boolean skipExistingB = true;
//	private static final int PERMISSIONS_REQUEST_CODE = 50;
//	private enum PermissionState {
//		UNKNOWN,
//		REQUESTED,
//		GRANTED,
////		DENIED
//	}
//	private PermissionState storagePermission = PermissionState.UNKNOWN;

}
