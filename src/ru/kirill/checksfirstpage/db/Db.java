package ru.kirill.checksfirstpage.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.kirill.checksfirstpage.dto.BillDto;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by oleg on 25.05.13.
 */
public class Db {
	public static final String COLUMN_PAY_DATE = "pay_date";
	public static final String COLUMN_CASH = "cash";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_KIND = "kind";
	private final Context ctx;
	private DBHelper dbHelper;
	private final int DB_VERSION = 1;
	private final String DB_NAME = "myDb";
	private SQLiteDatabase mDb;

	public Db(Context ctx) {
		this.ctx = ctx;
	}

	public void open() {
		dbHelper = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
		mDb = dbHelper.getWritableDatabase();
	}

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Calendar calendar = new GregorianCalendar();

	public void insert(BillDto billDto) {
		mDb.insert("bills", null, getFilledContentValues(billDto));
	}

	public void delRec(long id) {
		mDb.delete("bills", COLUMN_ID + " = " + id, null);
	}

	public void edit(BillDto billDto) {
		// TODO Auto-generated method stub
		mDb.update("bills", getFilledContentValues(billDto), "_id=?", new String[] {billDto.id});
	}

	private ContentValues getFilledContentValues(BillDto dto) {
		ContentValues cv = new ContentValues();
		cv.put("pay_date", dateFormat.format(dto.payDate));
		cv.put("cash", dto.cash);
		cv.put("kind", dto.kind);
		cv.put("description", dto.description);
		cv.put("input_date", dateFormat.format(dto.payDate));
		calendar.setTime(dto.payDate);
		cv.put("pay_date_year_month", calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH) + 1);
		return cv;
	}

	public void clear() {
		mDb.execSQL("delete from bills");
	}

	public void close() {
		if (dbHelper!=null) {
			mDb.close();
			dbHelper.close();
		}
	}

	// получить все данные из таблицы DB_TABLE
	public Cursor getAllData() {
		return mDb.query("bills", null, null, null, null, null, "pay_date, cash");
	}

	private class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context ctx, String dbName, SQLiteDatabase.CursorFactory cursorFactory, int dbVersion)  {
			super(ctx, dbName, cursorFactory, dbVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			String DB_DDL =
					" CREATE TABLE bills ( "
							+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ " pay_date DATE NOT NULL, "
							+ " cash DECIMAL(15,2) NOT NULL DEFAULT 0, "
							+ " kind VARCHAR(250) NOT NULL DEFAULT '', "
							+ " description VARCHAR(250) DEFAULT '', "
							+ " uuid VARCHAR(50), "
							+ " input_date DATETIME default current_timestamp, "
							+ " pay_date_year_month decimal(6) "
							+ "); ";
			sqLiteDatabase.execSQL(DB_DDL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

		}
	}


}
