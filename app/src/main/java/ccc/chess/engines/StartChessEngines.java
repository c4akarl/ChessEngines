package ccc.chess.engines;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

public class StartChessEngines extends Activity implements OnTouchListener 
{	//>110
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dataEnginesPath = getApplicationContext().getFilesDir() + "/engines/";
        efm = new EngineFileManager();
        efm.dataEnginesPath = dataEnginesPath;
        getEnginePrefs();
        filePrefs = getSharedPreferences("file", 0);
        getPreferences();
        btnInfo = (ImageView) findViewById(R.id.btnInfo); 
        btnOptions = (ImageView) findViewById(R.id.btnOptions);
        registerForContextMenu(btnOptions);
        btnOptions.setOnTouchListener((OnTouchListener) this);
        btnGui = (ImageView) findViewById(R.id.btnGui);
        btnMenu = (ImageView) findViewById(R.id.btnMenu);
        registerForContextMenu(btnMenu);
        btnMenu.setOnTouchListener((OnTouchListener) this);
        tvMain = (TextView) findViewById(R.id.tvMain);
        aboutApp(4);
        initEngineService();
    }
    @Override
    protected void onDestroy() 						// Program-Exit						(onDestroy)
    {
    	setPreferences();
    	releaseEngineService(true);
    	super.onDestroy();
    }
//	MENU		MENU		MENU		MENU		MENU		MENU		MENU		MENU		
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{	
	    if (keyCode == KeyEvent.KEYCODE_MENU) 
	    	openContextMenu(btnMenu);
	    return super.onKeyUp(keyCode, event);
	}
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
		if (v == btnOptions)
		{
		    getMenuInflater().inflate(R.menu.menu_engine, menu);
		    menu.setHeaderTitle(getString(R.string.menu_engine));
		}
		if (v == btnMenu)
		{
		    getMenuInflater().inflate(R.menu.menu_about, menu);
		    menu.setHeaderTitle(getString(R.string.menu_about));
		}
    }
    public boolean onContextItemSelected(MenuItem item)
    {
    	menuData = engineSearchPath;
        switch(item.getItemId())
        {
	        case R.id.menu_engineInstall:
	        	getEnginesFromSdcard(engineSearchPath);
	            return true;
	        case R.id.menu_engineUninstall:
	        	dataAction = DATA_DELETE;
	        	menuData = getString(R.string.menu_engineUninstall);
	        	getEnginesFromData();
	        	return true;
	        case R.id.menu_engineSelect:
	        	dataAction = DATA_SELECT;				// import an engine
	        	menuData = getString(R.string.menu_engineSelect);	
	        	getEnginesFromData();
	        	return true;
	        case R.id.menu_Options:
	        	Intent i;
	        	i = new Intent(StartChessEngines.this, ChessEnginePreferences.class);
				startActivityForResult(i, PREFERENCES_REQUEST_CODE);
	        	return true;
	        	
	        case R.id.menu_whatsNew: 
	        	showDialog(WHATS_NEW);
	            return true;
	        case R.id.menu_about: 
	        	c4aShowDialog(ABOUT_DIALOG);
	            return true;  
	        case R.id.menu_developer: 
	        	c4aShowDialog(DEVELOPER_DIALOG);
	            return true;
	        case R.id.menu_homepage: 
	        	Intent irw = new Intent(Intent.ACTION_VIEW);
    			irw.setData(Uri.parse("http://c4akarl.blogspot.com/"));
    			startActivityForResult(irw, HOMEPAGE_REQUEST_CODE);
	            return true;
            case R.id.menu_sourceCode:
                Intent ihp = new Intent(Intent.ACTION_VIEW);
                ihp.setData(Uri.parse("https://github.com/c4akarl/ChessEngines"));
                startActivityForResult(ihp, SOURCECODE_REQUEST_CODE);
                return true;
            case R.id.menu_contact:
	        	Intent send = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", APP_EMAIL, null)); 
	        	send.putExtra(Intent.EXTRA_SUBJECT, "");
	        	send.putExtra(Intent.EXTRA_TEXT, "");
	        	startActivity(Intent.createChooser(send, getString(R.string.sendEmail)));
	            return true;
        }
        return true;
    }
//	USER ACTION		USER ACTION		USER ACTION		USER ACTION		USER ACTION    
    public boolean onTouch(View view, MotionEvent event)
	{	// Touch Listener (chess board)
		if (view.getId() == R.id.btnOptions & event.getAction() == MotionEvent.ACTION_UP)
			openContextMenu(btnOptions);
		if (view.getId() == R.id.btnMenu & event.getAction() == MotionEvent.ACTION_UP)	
     		openContextMenu(btnMenu);
		return true;
	}
    public void myClickHandler(View view) 			// ClickHandler 					(ButtonEvents)
    {
    	Intent i;
		switch (view.getId())
		{
		case R.id.btnInfo:
			aboutCounter++;
			if (aboutCounter > 3)
				aboutCounter = 1;
			aboutApp(aboutCounter);
			break;
		case R.id.btnGui:
			setPreferences();
			releaseEngineService(false);
			if (this.getCallingPackage() == null)
			{
				boolean startMarket = false;
				try
				{
					PackageManager pm = this.getPackageManager();
				    i = pm.getLaunchIntentForPackage(GUI_C4A);
				    if (i != null)
				        startActivity(i);
				    else
				    	startMarket = true;
				}
				catch (ActivityNotFoundException e)	{startMarket = true;}
				if (startMarket)
				{
					try
					{
						i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse("market://details?id=" + GUI_C4A));
						startActivity(i);
					}
					catch (ActivityNotFoundException e) {}
				}
			}
			finish();
			break;
		}
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)			// SubActivityResult
    {
 	   getEnginePrefs();
	}
    @Override
	protected Dialog onCreateDialog(int id) 
    {
		switch (id) 
		{
			case FILE_DIALOG:  
	        {
				CharSequence[] items = new String[fileList.size()];
				if (fileList.size() > 0)
		    	  {
			    	  for (int i = 0; i < fileList.size(); i++)
			          {
			    		  if (i == 0)
			    			  items[i] = fileList.get(i);
			    		  else
			    		  {
			    			  
			    			  items[i] = fileList.get(i);
			    		  }
			          }
		    	  }
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(engineSearchPath);
				builder.setItems(items, new OnClickListener()
				{
				    public void onClick(DialogInterface dialog, int item) 
				    {
//				    	Log.i(TAG, "File selected: " + fileList.get(item));
				    	if (fileList.get(item).startsWith("<"))
				    	{
				    		if (fileList.get(item).equals("< < <"))
				    		{
//				    			Log.i(TAG, "directory back: " + efm.currentFilePath);
				    			engineSearchPath = efm.getParentFile(efm.currentFilePath);
//				    			Log.i(TAG, "engineSearchPath: " + engineSearchPath);
				    			getEnginesFromSdcard(engineSearchPath);
				    		}
				    		else
				    		{
					    		engineSearchPath = efm.currentFilePath + "/" + fileList.get(item).substring(1, fileList.get(item).length() -1);
//					    		Log.i(TAG, "engineSearchPath: " + engineSearchPath);
					    		getEnginesFromSdcard(engineSearchPath);
				    		}
				    	}
				    	else
				    	{
				    		boolean writeOk = efm.writeEngineToData(efm.currentFilePath, fileList.get(item), null);
				        	if (writeOk)
				        		aboutApp(aboutCounter);
				    	}
				    }
				});
				AlertDialog alert = builder.create();
	            return alert;
	        }
			case DATA_SELECT_DIALOG:  
	        {
	        	CharSequence[] items = new String[dataList.size()];
	        	int selectedItem = -1;
				if (dataList.size() > 0)
		    	  {
			    	  for (int i = 0; i < dataList.size(); i++)
			          {
			    		  items[i] = dataList.get(i);
			    		  if (dataChecked[i] == true)
			    		  {
			    			  selectedItem = i;
			    			  dataChecked[i] = false;
			    		  }
			          }
		    	  }
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(menuData);
				builder.setSingleChoiceItems(items, selectedItem, new OnClickListener()
				{
				    public void onClick(DialogInterface dialog, int item) 
				    {
			    		dataChecked[item] = true;
				    }
				});
				builder.setPositiveButton("OK", new OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						for (int i = 0; i < dataChecked.length; i++)	
			        	{
							if (dataChecked[i] == true)
							{
					        	changeEngine(dataList.get(i));
							}
			        	}
				    	aboutApp(aboutCounter);
					}
				});
				AlertDialog alert = builder.create();
	            return alert;
	        }
			case DATA_DELETE_DIALOG:  
	        {
				CharSequence[] items = new String[dataList.size()];
				if (dataList.size() > 0)
				{
					for (int i = 0; i < dataList.size(); i++)
					{
						  items[i] = dataList.get(i);
					}
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(menuData);
				builder.setItems(items, new OnClickListener()
				{
				    public void onClick(DialogInterface dialog, int item) 
				    {
						efm.deleteFileFromData(dataList.get(item));
						dataList.remove(item);
						if (dataList.size() == 0)
							changeEngine("DEFAULT");
						else
						{
							if (!isEngineInData())
								changeEngine(dataList.get(0));
						}
				    	aboutApp(aboutCounter);
				    }
				});
				AlertDialog alert = builder.create();
	            return alert;
	        }
			case NO_CHESS_ENGINE_DIALOG: 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String notFound = "";
				if (!currentProcess.equals(""))
					notFound = getString(R.string.engineAlreadyRunning) + " " + currentProcess;
				else
					notFound = getString(R.string.engineNotFound) + ENGINE_INTERFACE;
				builder.setTitle(R.string.app_name_uci).setMessage(notFound);
				builder.setOnCancelListener(new OnCancelListener() 
				{
			        public void onCancel(DialogInterface dialog) 
			        {
			        	finish();
				    }
				});
				AlertDialog alert = builder.create(); 
				return alert;
			}
			case ABOUT_DIALOG: 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.menu_about).setMessage(R.string.menu_about_text);
				AlertDialog alert = builder.create(); 
				return alert;
			}
			case DEVELOPER_DIALOG: 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.menu_developer).setMessage(R.string.menu_developer_text);
				AlertDialog alert = builder.create(); 
				return alert;
			}
			case CONTACT_DIALOG: 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.menu_contact).setMessage(R.string.menu_contact_text);
				AlertDialog alert = builder.create(); 
				return alert;
			}
			case WHATS_NEW: 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.whatsNew).setMessage(R.string.whatsNew_text);
				AlertDialog alert = builder.create(); 
				return alert;
			}
		}
		return null;
	}

//	ENGINE-FILE(intall, uninstall, select)		ENGINE-FILE(intall, uninstall, select)	
    private void getEnginesFromSdcard(String path) 
	{
		Log.i(TAG, "path: " + path);
    	fileArray = efm.getFileArrayFromPath(path);
// ERROR	v1.0	14.11.2011 02:24:11
    	if (fileArray != null)
    	{
	    	fileList.clear();
	    	if (fileArray != null)
	    	{
	//    		Log.i(TAG, "Files:");
	    		fileList.add("< < <");	// first entry: back to previous directory
	    		for (int i = 0; i < fileArray.length; i++)	
	        	{
	//    			Log.i(TAG, fileArray[i]);
	    			if (efm.fileIsDirectory(fileArray[i]))
	    			{
	    				fileList.add("<" + fileArray[i] + ">");
	    			}
	    			else
	   					fileList.add(fileArray[i]);
	        	}
	    		c4aShowDialog(FILE_DIALOG);
	    	}
    	}
//		else
//			c4aShowDialog(FILE_DIALOG);
	}
    private void getEnginesFromData() 
	{
    	fileArray = efm.getFileArrayFromData();
    	dataList.clear();
    	if (fileArray != null)
    	{
//    		Log.i(TAG, "Files:");
    		for (int i = 0; i < fileArray.length; i++)	
        	{
//    			Log.i(TAG, fileArray[i]);
    			if (!efm.fileIsDirectory(fileArray[i]))
    				dataList.add(fileArray[i]);
        	}
    		dataChecked = new boolean[dataList.size()];
    		for (int i = 0; i < dataList.size(); i++)	
        	{
    			if (dataList.get(i).equals(engineProcess) & dataAction == DATA_SELECT)
    				dataChecked[i] = true;
    			else
    				dataChecked[i] = false;
        	}
    		switch (dataAction) 
            { 
    	        case DATA_DELETE: 
    	        	c4aShowDialog(DATA_DELETE_DIALOG);
    	            break;
    	        case DATA_SELECT: 
    	        	c4aShowDialog(DATA_SELECT_DIALOG);
    	            break;
            }
    	}
    	else
    	{
    		
    	}
	}

    //	HELPERS		HELPERS		HELPERS		HELPERS		HELPERS		HELPERS		HELPERS		HELPERS
    public void getPreferences() 
	{
//    	engineSearchPath = filePrefs.getString("engineSearchPath", efm.getExternalDirectory());
//    	engineSearchPath = filePrefs.getString("engineSearchPath", Environment.getExternalStorageDirectory().getAbsolutePath());
    	engineSearchPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	aboutCounter = filePrefs.getInt("aboutCounter", 1);
	}
    public void setPreferences() 
	{
    	SharedPreferences.Editor ed = filePrefs.edit();
		ed.putString("engineSearchPath", engineSearchPath);
		ed.putInt("aboutCounter", aboutCounter);
		ed.commit();
	}
    private void getEnginePrefs() 
    {
    	enginePrefs = getSharedPreferences("engine", 0);
//    	isLogOn = enginePrefs.getBoolean("logOn", false);
    	if (enginePrefs.getBoolean("logOn", false))
    		getInfoFromEngineService("SET_LOGFILE_ON");
    	else
    		getInfoFromEngineService("SET_LOGFILE_OFF");
    }
    public void c4aShowDialog(int dialogId)					
    {	// show dialog (remove and show)
		removeDialog(dialogId);
		showDialog(dialogId);
    }
    private boolean isEngineInData() 
    {
    	boolean engineInData = false;
    	for (int i = 0; i < dataList.size(); i++)
        {
    		if (dataList.get(i).equals(engineProcess))
    			engineInData = true;
        }
    	return engineInData;
    }
    boolean isNamedProcessRunning(String processName)
    {
    	if (processName == null) return false;
    	ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
    	List<RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
    	for (RunningAppProcessInfo process : processes)
    	{
    	    if (processName.equals(process.processName))
    	        return true;
    	}
    	return false;
    }
    public Runnable mUpdateEngineIsready = new Runnable() 		
	{	
	   public void run() 
	   {
		   	String s = readLineFromProcess(1000);
			if (s == null)
				s = "";
    		if (s.equals("readyok") | s.endsWith("readyok"))
    		{
//	    			Log.i(TAG, "mUpdateEngineIsready, readyok");
    			isReady = true;
    			aboutApp(aboutCounter);
    			handlerEngineIsready.removeCallbacks(mUpdateEngineIsready);
    		}
    		else
    		{
    			handlerEngineIsreadyCnt++;
    			if (handlerEngineIsreadyCnt < 100)
    			{
	    			aboutApp(4);
	    			handlerEngineIsready.postDelayed(mUpdateEngineIsready, 30);
    			}
    			else
    			{
    				aboutApp(aboutCounter);
    				quitEngine();
    				handlerEngineIsready.removeCallbacks(mUpdateEngineIsready);
    			}
    		}
	   }
	};
	public Runnable mUpdateEngineUciOptions = new Runnable() 		
	{	
		public void run() 
		{
			String s = readLineFromProcess(1000);
			if (s == null)
				s = "";
    		if (s.equals("uciok") | s.endsWith("uciok"))
    		{
    			tvMain.setText(infoText);
    			handlerEngineUciOptions.removeCallbacks(mUpdateEngineUciOptions);
    		}
    		else 
    		{
    			handlerEngineUciOptionsCnt++;
				if (handlerEngineUciOptionsCnt < 100)
				{
					if (s.length() > 0)
					{
	    				infoText.append(s);
	    				infoText.append("\n\n");
					}
	    			handlerEngineUciOptions.postDelayed(mUpdateEngineUciOptions, 30);
				}
				else
				{
					aboutApp(1);
					quitEngine();
					handlerEngineUciOptions.removeCallbacks(mUpdateEngineUciOptions);
				}
    		}
		}
	};

	// 	INNER CLASS: EngineServiceConnection			INNER CLASS: EngineServiceConnection
	// This class represents the actual service connection. 
	// It casts the bound stub implementation of the service to the AIDL interface.
	class EngineServiceConnection implements ServiceConnection
 
	{	//>130
		public void onServiceConnected(ComponentName name, IBinder boundService) 
		{	//>131
//			Log.i(TAG, "onServiceConnected!!!");
			engineService = IChessEngineService.Stub.asInterface((IBinder) boundService);
			try {Thread.sleep(100);} 
			catch (InterruptedException e) {}
			startEngine();
		}
		public void onServiceDisconnected(ComponentName name) 
		{	//>132
			engineService = null;
		}
	}
	
// 	ENGINE-SERVICE(sub methodes)		ENGINE-SERVICE(sub methodes)		ENGINE-SERVICE(sub methodes)    
    private void initEngineService() 
    {	//>133 binds this activity to the service. 
    	engineServiceConnection = new EngineServiceConnection();
    	engineIntent = new Intent(ENGINE_INTERFACE);
		engineIntent.setPackage("ccc.chess.engines");
        startService(engineIntent);		// starting once for visibility on device(running services)
    	bindService(engineIntent, engineServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private void startEngine() 
    {	//>134
    	startProcess();
//    	Log.i(TAG, "processAlive: " + processAlive);
    	if (processAlive)
    	{
    		engineProcess = getInfoFromEngineService("ENGINE_PROCESS");
	    	writeLineToProcess("isready");
	    	handlerEngineIsreadyCnt = 0;
	    	handlerEngineIsready.removeCallbacks(mUpdateEngineIsready);
	    	handlerEngineIsready.postDelayed(mUpdateEngineIsready, 100);
    	}
    	else
    	{
    		aboutApp(aboutCounter);
    		currentProcess = getInfoFromEngineService("CURRENT_PROCESS");
    		c4aShowDialog(NO_CHESS_ENGINE_DIALOG);
    	}
    }
    private void releaseEngineService(boolean unbind) 
    {	//>135 unbinds this activity from the service
    	quitEngine();
    	if (unbind)
    	{
			unbindService(engineServiceConnection);
			engineServiceConnection = null;
    	}
    }
    private void quitEngine() 
    {
    	if (processAlive) 
		{
    		writeLineToProcess("quit");
    		isReady = false;
    		processAlive = false;
		}
    }
    private void changeEngine(String newEngine) 
    {
    	quitEngine();
    	try {Thread.sleep(150);} 
		catch (InterruptedException e) {}
		engineProcess = getInfoFromEngineService("ENGINE_PROCESS " + newEngine);
//		Log.i(TAG, "changeEngine, engineProcess: " + engineProcess);
    	startEngine();
    }
    private void readUCIOptions() 
	{
    	aboutApp(4);
    	this.setTitle(R.string.app_name_uci);
    	tvMain.setText("\n" + getString(R.string.engineReadingUciOptions));
    	writeLineToProcess("uci");
    	infoText  =  new StringBuffer(2000);
    	handlerEngineUciOptionsCnt = 0;
    	handlerEngineUciOptions.removeCallbacks(mUpdateEngineUciOptions);
    	handlerEngineUciOptions.postDelayed(mUpdateEngineUciOptions, 100);
    }
    private void aboutApp(int aboutId) 
	{
    	switch (aboutId) 
        { 
	        case 1: // about app
	        	this.setTitle(R.string.app_name_about);
	        	tvMain.setText(getString(R.string.about).replace("\\n", "\n"));
	            break;  
	        case 2: // UCI option
	        	if (isReady)
					readUCIOptions();
	        	break;
	        case 3: // info imported engines
	        	this.setTitle(R.string.app_name_engines);
	        	dataAction = DATA_LIST;
	        	getEnginesFromData();
	        	String engineInfo = "";
	        	engineInfo = engineInfo + getString(R.string.engineCurrent) + "\n   " + engineProcess + "\n\n";
	        	engineInfo = engineInfo + getString(R.string.engineList) + "\n";
	        	if (dataList.size() > 0)
		    	  {
			    	  for (int i = 0; i < dataList.size(); i++)
			          {
		    			  engineInfo = engineInfo + "   " + dataList.get(i) + "\n";
			          }
		    	  }
	        	tvMain.setText(engineInfo);
	        	break;
	        case 4: // connecting
	        	this.setTitle(R.string.app_name_connecting);
	        	tvMain.setText("");
	        	return;
//	            break;  
        }
    	if (aboutId != 4)
    		aboutCounter = aboutId;
	}
    public String[] tokenize(String cmdLine) 
    {
        cmdLine = cmdLine.trim();
        return cmdLine.split("\\s+");
    }

// 	ENGINE-SERVICE(main methodes)		ENGINE-SERVICE(main methodes)		ENGINE-SERVICE(main methodes)		
    public final void startProcess() 
	{	//>184
    	try 	
		{ 
    		if (engineService != null)
    			processAlive = engineService.startNewProcess(GUI_PROCESS_NAME);
		} 
    	catch 	( RemoteException e) 
		{
			e.printStackTrace(); 
			processAlive = false;
			engineService = null;
		}
	}
    public final synchronized void writeLineToProcess(String data) 
	{	//>185
    	if (engineService != null)
    	{
			if (processAlive)
			{
				try 	{ engineService.writeLineToProcess(data); } 
				catch 	( RemoteException e) 
				{
					e.printStackTrace();
					processAlive = false;
					engineService = null;
				}
			}
    	}
	}
    public final String readLineFromProcess(int timeoutMillis) 
	{	//>186
		String ret = "";
//		Log.i(TAG, "engineService: " + engineService);
		if (engineService != null)
    	{
			if (processAlive)
			{
				try 	{ ret = engineService.readLineFromProcess(timeoutMillis); } 
				catch 	( RemoteException e) 
				{
					e.printStackTrace();
					processAlive = false;
					engineService = null;
				}
				catch 	( NullPointerException e) // ???
				{
					e.printStackTrace();
					ret = "";
				}
			}
    	}
		return ret;
	}
	public String getInfoFromEngineService(String infoId)
	{	//>187
    	String info = "";
    	if (engineService != null)
    	{
	    	try 	{ info = engineService.getInfoFromEngineService(infoId); } 
			catch 	(RemoteException e) { info = ""; }
			catch 	(NullPointerException e) { info = ""; }
    	}
		return info;
	}
	
	final String TAG = "StartChessEngine";
	private static final String ENGINE_INTERFACE = "ccc.chess.engines.IChessEngineService";	//>111
	private static final String GUI_C4A = "ccc.chess.gui.chessforall";
	private static final String GUI_PROCESS_NAME = "START_CHESS_ENGINES";					//>112
	final static int HOMEPAGE_REQUEST_CODE = 10;
    final static int SOURCECODE_REQUEST_CODE = 20;
	final String APP_EMAIL = "c4akarl@gmail.com";
	//	DIALOG		DIALOG		DIALOG		DIALOG		DIALOG		DIALOG
	final int NO_CHESS_ENGINE_DIALOG = 1;
	final int FILE_DIALOG = 2;
	final int DATA_SELECT_DIALOG = 3;
	final int DATA_DELETE_DIALOG = 4;
	final int ABOUT_DIALOG = 7;
	final int DEVELOPER_DIALOG = 8;
	final int CONTACT_DIALOG = 9;
	final int WHATS_NEW = 10;
	
	//	FILE-ACTION		FILE-ACTION		FILE-ACTION		FILE-ACTION		FILE-ACTION
	final int FILE_INSTALL = 301;
	final int DATA_LIST = 310;
	final int DATA_SELECT = 311;
	final int DATA_DELETE = 312;
	int dataAction = DATA_LIST;
//	subActivity RequestCode
	private static final int PREFERENCES_REQUEST_CODE = 1;
	IChessEngineService engineService;
	EngineServiceConnection engineServiceConnection;
	EngineFileManager efm;
	Intent engineIntent;
	SharedPreferences enginePrefs;
	SharedPreferences filePrefs;
	public Handler handlerEngineIsready = new Handler();
	public Handler handlerEngineUciOptions = new Handler();
	int handlerEngineIsreadyCnt = 0;
	int handlerEngineUciOptionsCnt = 0;
	int aboutCounter = 1;
	boolean isStartApp = true;					
	boolean stopService = true;	
	boolean processAlive;
	String currentProcess = "";
	String engineProcess = ""; // file name (/data/data ...)
	boolean isReady = false;
	ImageView btnInfo = null;
	ImageView btnOptions = null;
	ImageView btnGui = null;
	ImageView btnMenu = null;
	TextView tvMain = null;
	Menu menuE;
	String menuData = "";
	ProgressDialog progressDialog = null;
	
	String engineSearchPath = "";
//	String engineSearchPath = "/sdcard/chess_engines/";
	String dataEnginesPath = "";
	String[] fileArray;
	ArrayList<String> fileList = new ArrayList<String>();	// file list(sd-card)
	ArrayList<String> dataList = new ArrayList<String>();	// data list(engines, package file system)
	boolean[] dataChecked;
	StringBuffer infoText;
}