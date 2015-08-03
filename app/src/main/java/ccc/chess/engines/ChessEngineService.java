package ccc.chess.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ChessEngineService extends Service
{	//>150 the bridge to the gui and to the engine(native processes)
	@Override
    public void onCreate() 
	{
		dataEnginesPath = getApplicationContext().getFilesDir() + "/engines/";
		efm = new EngineFileManager();
		efm.dataEnginesPath = dataEnginesPath;
		getPrefs();
		engineName = ENGINE_PROCESS_NAME;
		if (isLogOn)
		{
			Log.i(TAG, engineName + ": onCreate");
			Log.i(TAG, engineName + ": dataEnginesPath: " + dataEnginesPath);
		}
        super.onCreate();
    }
	@Override
	public IBinder onBind(Intent intent) 
	{	//>154 return the IBinder object
		if (isLogOn)
		{
			Log.i(TAG, engineName + ": onBind(Intent): " + intent);
			Log.i(TAG, engineName + ": dataEnginesPath: " + dataEnginesPath);
		}
		return mBinder;
	}
	@Override
	public boolean onUnbind(Intent intent) 
	{	//>155 unbinds the Service from the Gui
		setPrefs();
		if (isLogOn)
			Log.i(TAG, engineName + ": onUnbind(intent): " + intent);
		stopSelf();	//>156 for starting/stopping; start Service: --->332 (CCC GUI)
		currentCallPid = "";
		engineName = "";
		if (process != null)
			process.destroy();
		return super.onUnbind(intent);
	}
	public String getCurrentCallPid()	{return currentCallPid;}	//>157 the current running Gui process on ChessEngineService
	private void getPrefs() 
    {	// get user preferences
        assetsEngineProcess = ASSETS_ENGINE_PROCESS_1;
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (osVersion > 20)
            assetsEngineProcess = ASSETS_ENGINE_PROCESS_2;
		enginePrefs = getSharedPreferences("engine", 0);		//	engine Preferences
		isLogOn = enginePrefs.getBoolean("logOn", false);
        if (isLogOn)
            Log.i(TAG, "osVersion: " + osVersion + ", assetsEngineProcess: " + assetsEngineProcess);
		engineProcess = enginePrefs.getString("engineProcess", engineProcess);
		if (!efm.dataFileExist(engineProcess) | !engineProcess.equals(assetsEngineProcess))
		{
			writeDefaultEngineToData();
		}
//		isLogOn = true; // for test only
    }
	private void setPrefs() 
    {	// update user preferences
		SharedPreferences.Editor ed = enginePrefs.edit();
		ed.putBoolean("logOn", isLogOn);
		ed.putString("engineProcess", engineProcess);
		ed.commit();
    }
	private void setChessEngineName(String uciIdName) 
	{ 
		engineName = uciIdName.substring(8, uciIdName.length());
		if (uciIdName.startsWith("White(1): id name "))
			engineName = uciIdName.substring(18, uciIdName.length());
	}
	private String getChessEngineNameAndStrength() 
	{ 
		String engineNameStrength = engineName;
		if (!engineName.equals("") & enginePrefs.getInt("strength", 100) != 100)
		{
			if (isUciStrength)
			{
				if (isUciEloOption)
					engineNameStrength = engineName + " (" + enginePrefs.getInt("strength", 100) + "%, Elo: " + uciStrength + ")";
				if (isUciSkillOption)
					engineNameStrength = engineName + " (" + enginePrefs.getInt("strength", 100) + "%, Level: " + uciStrength + ")";
			}
		}
		return engineNameStrength;
	}
	private void writeDefaultEngineToData() 
    {
		try
		{
			InputStream istream = getAssets().open(assetsEngineProcess);
			if (efm.writeEngineToData("", assetsEngineProcess, istream))
				engineProcess = assetsEngineProcess;
			else
				engineProcess = "";
		}
		catch (IOException e)
        {
            e.printStackTrace();	engineProcess = "";
        }
    }
	void setUciEloValues(String message)
	{
		String[] messageSplit = message.split(" ");
		String min = "";
		String max = "";
		if (messageSplit.length >= 0)
		{
			for (int i = 0; i < messageSplit.length; i++)
			{
				if (messageSplit[i].equals("min") & messageSplit.length > i)
					min = messageSplit[i +1];
				if (messageSplit[i].equals("max") & messageSplit.length > i)
					max = messageSplit[i +1];
			}
			try		
			{
				uciEloMin = Integer.parseInt(min);
				uciEloMax = Integer.parseInt(max);
			}
	    	catch 	(NumberFormatException e) { }
		}
	}
	void setUciStrength()
	{
		if (!isUciStrength)
			return;
		int eloBase = uciEloMax - uciEloMin;
		int skillStrength = uciEloMin + ((eloBase / 100) * enginePrefs.getInt("strength", 100));
		int skillStrengthStockfish = (int) (0.2 * enginePrefs.getInt("strength", 100));
//		int skillStrength = 0;
		
		String mesStrength =  "UCI_Elo: " + skillStrength + ", strength: " + enginePrefs.getInt("strength", 100) + "%";
		try 
		{
			if (isLogOn)
			{
				if (isUciEloOption)
				{
					Log.i(TAG, engineName + ": " + mesStrength);
					Log.i(TAG, engineName + ": " + "setoption name UCI_LimitStrength value true");
					Log.i(TAG, engineName + ": " + "setoption name UCI_Elo value " + skillStrength);
				}
				if (isUciSkillOption)
				{
					Log.i(TAG, engineName + ": " + "Skill Level: " + skillStrengthStockfish + ", strength: " + enginePrefs.getInt("strength", 100) + "%");
					Log.i(TAG, engineName + ": " + "setoption name Skill Level value " + skillStrengthStockfish);
				}
			}
			if (isUciEloOption)
			{
				writeToProcess("setoption name UCI_LimitStrength value true\n");
				writeToProcess("setoption name UCI_Elo value " + skillStrength + "\n");
				uciStrength = skillStrength;
			}
			if (isUciSkillOption)
			{
				writeToProcess("setoption name Skill Level value " + skillStrengthStockfish + "\n");	// stockfish
				uciStrength = skillStrengthStockfish;
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}
	private final IChessEngineService.Stub mBinder = new IChessEngineService.Stub() 
	{	//>153 the EngineService / Gui methods(164...167)
		// ENGINE_SERVICE <--> CHESS_GUI + CALL NATIVE METHODS		ENGINE_SERVICE <--> CHESS_GUI + CALL NATIVE METHODS
		public boolean  startNewProcess(String callPid) throws RemoteException
		{	//>164 (called from Gui)
			processAlive = false;
			if (isLogOn)
				Log.i(TAG, "startNewProcess, engineProcess: " + engineProcess);
			if (isLogOn & !currentCallPid.equals(""))
				Log.i(TAG, engineName + ": current Process, startNewProcess: " + currentCallPid + ", " + callPid);
			if (currentCallPid.equals("") | currentCallPid.equals(callPid))
			{
				processAlive = startProcess(engineProcess);
				if (processAlive)
				{
					currentCallPid = callPid;
					if (isLogOn)
						Log.i(TAG, engineName + ": new session, gui: " + currentCallPid);
				}
				else
				{
					if (isLogOn)
						Log.i(TAG, engineName + ": error startProcess, engine name: " + engineProcess);
					currentCallPid = "";
				}
			}
			else
			{
				if (isLogOn)
					Log.i(TAG, engineName + ": startNewProcess failed, an process already is running: " + currentCallPid);
			}
			return processAlive;
		}
		public void writeLineToProcess(String data) throws RemoteException
		{	//>165 (called from Gui)
//			Log.i(TAG, "data: " + data);
			try {writeToProcess(data + "\n");}
			catch (IOException e) 
			{
				e.printStackTrace();
				processAlive = false;
			}
			if (isLogOn)
				Log.i(TAG, currentCallPid + " >>> " +  engineName + ": " + data);
			if (data.equals("quit"))
			{
				currentCallPid = "";
				engineName = "";
				if (process != null)
					process.destroy();
			}
//			if (data.startsWith("setoption name Hash value") & enginePrefs.getInt("strength", 100) != 100)
//				setUciStrength();
		}
		public String readLineFromProcess(int timeoutMillis)  throws RemoteException
		{	//>166 (called from Gui)
			String message = "";
			try{message =  readFromProcess();}
			catch (IOException e) 
			{
				if (isLogOn)
					Log.i(TAG, engineName + " >>> " + currentCallPid + ": IOException");
				e.printStackTrace();
			}
			if (message != null)
			{
				if (message.startsWith("id name ") | message.startsWith("White(1): id name "))
					setChessEngineName(message);
				if (message.startsWith("option name ") & message.contains("UCI_Elo"))
				{
					isUciStrength = true;
					isUciEloOption = true;
					setUciEloValues(message);
				}
				if (message.startsWith("option name ") & message.contains("Skill Level"))
				{
					isUciStrength = true;
					isUciSkillOption = true;
				}
				if (isLogOn)
					Log.i(TAG, engineName + " >>> " + currentCallPid + ": " + message);
			}
			else
				message = "";
			if (message.equals("uciok") & enginePrefs.getInt("strength", 100) != 100)
				setUciStrength();
			return message;
		}
		// ENGINE_SERVICE <--> CHESS_GUI		ENGINE_SERVICE <--> CHESS_GUI	ENGINE_SERVICE <--> CHESS_GUI
		public String getInfoFromEngineService(String infoId)  throws RemoteException
		{	//>167 for GUI <---> SERVICE only, no native process command! (called from GUI)
			String info = "";
			if (infoId.equals("CURRENT_PROCESS"))
				info =  getCurrentCallPid();
			if (infoId.startsWith("ENGINE_PROCESS"))
			{
				String tmp[] = infoId.split(" ");
				if (infoId.equals("ENGINE_PROCESS"))	
					info =  engineProcess;								// get current engine process(file name)
				else
				{
		    		if (tmp.length > 1)									// set the current engine process
		    		{
		    			if (tmp[1].equals("DEFAULT"))
		    				writeDefaultEngineToData();					// set current engine process(default from assets)
		    			else
		    				engineProcess =  tmp[1];					// set current engine process(file name)
		    			setPrefs();
		    			info =  engineProcess;
		    		}
		    		else
		    			info =  "";										// error: set current engine process to SPACE
				}
			}
			if (infoId.equals("ENGINE_NAME"))
				info =  engineName;										// get current engine name
			if (infoId.equals("ENGINE_TYPE"))
				info =  ENGINE_TYPE;
			if (infoId.equals("SET_LOGFILE_ON"))
				isLogOn = true;
			if (infoId.equals("SET_LOGFILE_OFF"))
				isLogOn = false;
			if (infoId.equals("GET_ENGINE_NAME_STRENGTH"))
			{
				if (enginePrefs.getInt("strength", 100) != 100)
					info = getChessEngineNameAndStrength();
			}
			if (infoId.equals("ENGINE_ALIVE"))
				info =  Boolean.toString(processAlive);
			if (isLogOn)
				Log.i(TAG, ENGINE_PROCESS_NAME + "(" + engineName + "): getInfoFromEngineService, " + infoId + ": " + info);
			return info;
		}
	};
	// NATIVE METHODS		NATIVE METHODS		NATIVE METHODS		NATIVE METHODS		NATIVE METHODS	
	private final boolean startProcess(String fileName)	//>174 start native process
	{
		ProcessBuilder builder = new ProcessBuilder(dataEnginesPath + fileName);
		try 
		{			process = builder.start();
			OutputStream stdout = process.getOutputStream();
			InputStream stdin = process.getInputStream();
			reader = new BufferedReader(new InputStreamReader(stdin));
			writer = new BufferedWriter(new OutputStreamWriter(stdout));
			return true;
		} 
		catch (IOException e) 
		{
			if (isLogOn)
				Log.i(TAG, engineName + ": startProcess, IOException");
			return false;
		}
	}
	private final void writeToProcess(String data) throws IOException //>175 write data to the process 
	{
		if (writer != null) 
		{
			writer.write(data);
			writer.flush();
		}
	}
	private final String readFromProcess() throws IOException //>176 read a line of data from the process
	{
		String line = null;
		if (reader != null && reader.ready())
		{
			line = reader.readLine();
		}
		return line;
	}
	
	
	final String TAG = "ChessEngineService";
//	Context context;
	private static final String ENGINE_PROCESS_NAME = "Chess_engines";	//>152 Engine name, using for logging
	private static final String ENGINE_TYPE = "CE";				//> Engine type: CE(Chess Engines)
	boolean isLogOn = false;			// LogFile on/off(SharedPreferences)
	SharedPreferences enginePrefs;		// user preferences(LogFile on/off)
	EngineFileManager efm;
	String currentCallPid = "";			// the process bound to this service(Gui or StartChessEngine) // final String GUI_PROCESS_NAME
	String engineName = "";				// the uci engine name
	String engineProcess = "";			// the compiled engine process name (file name)


//	final String ASSETS_ENGINE_PROCESS = "Sugar_050415_JA";
//	final String ASSETS_ENGINE_PROCESS = "stockfish-6-ja";
//	final String ASSETS_ENGINE_PROCESS = "bikjump1_8";
//	final String ASSETS_ENGINE_PROCESS = "deuterium-v14_3";
//	final String ASSETS_ENGINE_PROCESS = "Deuterium-v14_3_34_130";
    final String ASSETS_ENGINE_PROCESS_1 = "robbolito0085e4l";
    final String ASSETS_ENGINE_PROCESS_2 = "stockfish-6-ja";
    String assetsEngineProcess = "";
	String dataEnginesPath = "";
	
	private boolean processAlive = false;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;
	private Process process;
	boolean isUciStrength = false;		// supporting UCI strength
	boolean isUciEloOption = false;		// supporting UCI strength
	boolean isUciSkillOption = false;		// supporting UCI strength
	int uciEloMin = 1200;
	int uciEloMax = 3000;
	int uciStrength = 3000;
}