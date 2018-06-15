package com.thinkernote.ThinkerNote.Database;

import java.util.Vector;

import com.thinkernote.ThinkerNote.Action.TNAction;
import com.thinkernote.ThinkerNote.Action.TNAction.TNActionResult;
import com.thinkernote.ThinkerNote.General.Log;
import com.thinkernote.ThinkerNote.General.TNActionType;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class TNDb extends SQLiteOpenHelper {
	private static final String TAG = "TNDatabase";
	private static TNDb singleton = null;
	private final static int DB_VER = 2;
	private final static String DB_NAME = "ThinkerNote3.db";
	private SQLiteDatabase db = null;
	
	private int changeBits;
	
	public TNDb() {
		super(TNUtils.getAppContext(), DB_NAME, null, DB_VER);
		
		db = getWritableDatabase(); 
		
		TNAction.regRunner(TNActionType.Db_Execute, this, "executeSQL");
		TNAction.regRunner(TNActionType.DBReset, this, "DBReset");
	}

	public static TNDb getInstance(){
		if (singleton == null){
			synchronized (TNDb.class){
				if (singleton == null){
					singleton = new TNDb();
				}
			}
		}
		
		return singleton;
	}

	@Override
	public void onCreate(SQLiteDatabase aDB) {
		Log.d(TAG, "onCreate");
		aDB.execSQL(TNSQLString.USER_CREATE_TABLE);
		aDB.execSQL(TNSQLString.CAT_CREATE_TABLE);
		aDB.execSQL(TNSQLString.TAG_CREATE_TABLE);
		aDB.execSQL(TNSQLString.NOTE_CREATE_TABLE);
		aDB.execSQL(TNSQLString.ATT_CREATE_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase aDB, int oldVer, int newVer) {
		if (oldVer == 1 && newVer == 2) {
			aDB.execSQL("ALTER TABLE `Category` ADD `strIndex` TEXT(8) NOT NULL DEFAULT ''");
		}
	}
	
	public void DBReset(TNAction aAction){
		beginTransaction();
		try {
			//drop tables
			getInstance().db.execSQL(TNSQLString.USER_DROP_TABLE);
			getInstance().db.execSQL(TNSQLString.CAT_DROP_TABLE);
			getInstance().db.execSQL(TNSQLString.TAG_DROP_TABLE);
			getInstance().db.execSQL(TNSQLString.NOTE_DROP_TABLE);
			getInstance().db.execSQL(TNSQLString.ATT_DROP_TABLE);
			
			//create tables
			getInstance().db.execSQL(TNSQLString.USER_CREATE_TABLE);
			getInstance().db.execSQL(TNSQLString.CAT_CREATE_TABLE);
			getInstance().db.execSQL(TNSQLString.TAG_CREATE_TABLE);
			getInstance().db.execSQL(TNSQLString.NOTE_CREATE_TABLE);
			getInstance().db.execSQL(TNSQLString.ATT_CREATE_TABLE);
			
			setTransactionSuccessful();
		} finally {
			endTransaction();
		}
	}
	
	private long insert(String sql, String[] args) {
		int start = 0, end = 0;
		String tableName = "";
		ContentValues values = new ContentValues();

		start = sql.indexOf("`");
		end = sql.indexOf("`", start + 1);
		tableName = sql.substring(start, end + 1);

		for (int i = 0; i < args.length; i++) {
			start = sql.indexOf("`", end + 1);
			end = sql.indexOf("`", start + 1);
			values.put(sql.substring(start, end + 1), args[i]);
		}
		return db.insertOrThrow(tableName, null, values);
	}

	private Vector<Vector<String>> select(String sql, String[] args) {
		Cursor cursor = db.rawQuery(sql, args);

		Vector<Vector<String>> allData = new Vector<Vector<String>>();
		while (cursor.moveToNext()) {
			Vector<String> rowData = new Vector<String>();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String value = cursor.getString(i);
				if (value != null)
					rowData.add(value);
				else
					rowData.add("0");
			}
			allData.add(rowData);
		}
		cursor.close();
		Log.d(TAG, allData.toString());

		return allData;
	}

	private void execute(String sql, String[] args) {
		db.execSQL(sql, args);
	}

	public Object execSQL(String sql, Object... args) {
		String[] valus = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			valus[i] = String.valueOf(args[i]);
		}
		printSql(sql, valus);
		if (sql.startsWith("SELECT")) {
			return select(sql, valus);
		} else if (sql.startsWith("INSERT")) {
			return insert(sql, valus);
		} else {
			execute(sql, valus);
		}

		return null;
	}
	
	public void executeSQL(TNAction aAction){
		Log.d(TAG, aAction.inputs.toString());
		try{
			String sql = (String)aAction.inputs.get(0);
			if( sql.startsWith("SELECT")){
				String[] args = new String[aAction.inputs.size()-1];
				for(int i=1; i < aAction.inputs.size(); i ++){
					args[i-1] = aAction.inputs.get(i).toString();
				}
				Cursor cursor = db.rawQuery(sql, args);					
				
				Vector<Vector<String>> allData = new Vector<Vector<String>>();
				while (cursor.moveToNext()) { 
					Vector<String> rowData = new Vector<String>();
					for(int i=0; i < cursor.getColumnCount(); i ++){
						String value = cursor.getString(i);
						if( value != null)
							rowData.add( value );
						else
							rowData.add("0");
					}
					allData.add(rowData);
				} 
				aAction.outputs.add(allData);
				cursor.close(); 
			}
			else if(sql.startsWith("INSERT")){
				int start=0, end=0;
				String tableName="";
				ContentValues values = new ContentValues();
				
				start = sql.indexOf("`");
				end = sql.indexOf("`", start+1);
				tableName = sql.substring(start, end+1);
				//Log.i(TAG, "tableName:" + tableName + start + end);
				
				for(int i=1; i < aAction.inputs.size(); i ++){
					start = sql.indexOf("`", end+1);
					end = sql.indexOf("`", start+1);
					values.put(sql.substring(start, end+1), aAction.inputs.get(i).toString());
				}
				//Log.i(TAG, "values:" + values);
				long id = db.insertOrThrow(tableName, null, values);
				aAction.outputs.add(id);
			}
			else{ 
				Object[] args = new Object[aAction.inputs.size()-1];
				for(int i=1; i < aAction.inputs.size(); i ++){
					args[i-1] = aAction.inputs.get(i);
				}
				db.execSQL(sql, args);
			
			}
			aAction.result = TNActionResult.Finished;
		}catch(SQLiteException e){
			e.printStackTrace();
			TNAction.runAction(TNActionType.DbReportError,
					"username:" + TNSettings.getInstance().username +
					" SQLiteException:" + e.toString());
			aAction.result = TNActionResult.Finished;
		}
	}

	public static void beginTransaction() {
		getInstance().db.beginTransaction();
	}

	public static void setTransactionSuccessful() {
		getInstance().db.setTransactionSuccessful();
	}

	public static void endTransaction() {
		getInstance().db.endTransaction();
	}

	public static boolean isChanges(int aChange) {
		return (getInstance().changeBits & aChange) != 0;
	}

	public static void addChange(int aChange) {
		if ((getInstance().changeBits & aChange) == 0)
			getInstance().changeBits += aChange;
	}

	public static void removeChange(int aChange) {
		if ((getInstance().changeBits & aChange) != 0)
			getInstance().changeBits -= aChange;
	}

	private void printSql(String sql, String[] args) {
		String values = "";
		for (String arg : args) {
			arg = "`" + arg + "` ";
			values = values + arg;
		}
		Log.d(TAG, sql + "\r\n" + values);
	}
}