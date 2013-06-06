
package ru.kirill.checksfirstpage.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import android.util.Log;
import android.widget.Toast;
import ru.kirill.checksfirstpage.dto.BillDto;
import ru.kirill.checksfirstpage.dto.KindDto;

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
	private final int DB_VERSION = 4;
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
        ContentValues cv = getFilledContentValues(billDto);
        cv.put("exp_imp", "0"); // - чек может быть выгружен на почту
        mDb.update("bills", cv, "_id=?", new String[] {billDto.id});
	}

    public BillDto get(long id) {
        String[] whereParamaters = new String[] {""+id};
        String where = "_id = ?"; // при выборе записей из таблицы bills
                                  // вместо "вопросика" будет использовано
                                  // значение находящееся в массиве whereParamaters
        Cursor cursor = mDb.query("bills", null, where, whereParamaters, null, null, "pay_date, cash");
        if(cursor.moveToNext() ) {
            BillDto result = new BillDto();
            //result.payDate = cursor.getString(cursor.getColumnIndex("pay_date"));
            fillBillFields(cursor, result);
            return result;
        }
        return null;
    }

    private String getField(Cursor cursor, String fieldName) {
        return cursor.getString(cursor.getColumnIndex(fieldName));
    }

    private float getFieldFloat(Cursor cursor, String fieldName) {
        return cursor.getFloat(cursor.getColumnIndex(fieldName));
    }

    private int getFieldInt(Cursor cursor, String fieldName) {
        return cursor.getInt(cursor.getColumnIndex(fieldName));
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
        if(dto.uuid == null || dto.uuid == null) {
            dto.uuid = UUID.randomUUID().toString();
        }
        cv.put("uuid", dto.uuid);
        cv.put("exp_imp", dto.expImp);
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

    // получить данные из таблицы DB_TABLE
    public Cursor getData(int expImp) {
        // 0 - новые
        // 1 - новые и выгруженные
        String where="";
        if(expImp == 0) {
            where = "exp_imp = 0";
        } else if(expImp == 1) {
            where = "exp_imp in (0, 1)";
        }
        return mDb.query("bills", null, where, null, null, null, "pay_date, cash");
    }

	// получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDb.query("bills", null, null, null, null, null, "pay_date, cash");
    }
    public void beginTransaction() {
        mDb.beginTransaction();
    }

    public void fillBillFields(Cursor cursor, BillDto result) {
        result.id = getField(cursor, "_id");
        String sPayDate = getField(cursor, "pay_date");
        try {
            result.payDate = dateFormat.parse(sPayDate);
        } catch (ParseException e) {
            result.payDate = new Date(); // ToDo переделать на выбрасывание исключения и оповещение пользователя о ошибке
        }

        result.cash = Float.toString(getFieldFloat(cursor, "cash"));
        result.kind = getField(cursor, "kind");
        result.description = getField(cursor, "description");
        result.uuid = getField(cursor, "uuid");
        result.expImp = getFieldInt(cursor, "exp_imp");
    }

    public void transactionSuccessful() {
        mDb.setTransactionSuccessful();
    }

    public void endTransaction() {
        mDb.endTransaction();
    }
    // kinds
    // создать запись
    public void insertKind(KindDto kindDto) {
        mDb.insert("kinds", null, getKindFilledContentValues(kindDto));
    }
    //получить запись
    public KindDto getKind(long id) {
        String[] whereParamaters = new String[] {""+id};
        String where = "_id = ?"; // при выборе записей из таблицы bills
        // вместо "вопросика" будет использовано
        // значение находящееся в массиве whereParamaters
        Cursor cursor = mDb.query("kinds", null, where, whereParamaters, null, null, "name");
        if(cursor.moveToNext() ) {
            KindDto result = new KindDto();
            result.id = getField(cursor, "_id");
            result.name = getField(cursor, "name");
            return result;
        }
        return null;
    }

    //сохранить запись
    public void editKind(KindDto kindDto) {
        // TODO Auto-generated method stub
        mDb.update("kinds", getKindFilledContentValues(kindDto), "_id=?", new String[] {kindDto.id});
    }

    //удалить запись
    public void delRecKind(long id) {
        mDb.delete("kinds", "_id=?", new String[] {""+id});
    }

    //получить список всех Kinds


    public Cursor getAllKindData() {
        return mDb.query("kinds", null, null, null, null, null, "name");
    }

    private ContentValues getKindFilledContentValues(KindDto dto) {
        ContentValues cv = new ContentValues();
        cv.put("name", dto.name);
        return cv;
    }

    public void setSended(long id) {
        setExpImp(id, 1); // 1 - отправлен
    }

    private void setExpImp(long id, int expImp) {
        ContentValues cv = new ContentValues();
        cv.put("exp_imp", expImp);
        int cnt = mDb.update("bills", cv, "_id="+id, null);
        if(cnt != 1) {
            throw new IllegalStateException("Таблица bills. Запись не найдена. _id = " + id);
        }
    }

    private class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context ctx, String dbName, SQLiteDatabase.CursorFactory cursorFactory, int dbVersion)  {
			super(ctx, dbName, cursorFactory, dbVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createBills(sqLiteDatabase);
            createKinds(sqLiteDatabase);
		}

        private void createBills(SQLiteDatabase sqLiteDatabase) {
			String DB_DDL =
					" CREATE TABLE bills ( "
							+ " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ " pay_date DATE NOT NULL, "
							+ " cash DECIMAL(15,2) NOT NULL DEFAULT 0, "
							+ " kind VARCHAR(250) NOT NULL DEFAULT '', "
							+ " description VARCHAR(250) DEFAULT '', "
							+ " uuid VARCHAR(50), "
							+ " input_date DATETIME default current_timestamp, "
							+ " pay_date_year_month decimal(6), "
							+ " exp_imp decimal(1) default 0"
							+ "); ";
			sqLiteDatabase.execSQL(DB_DDL);
		}

		@Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {
                createKinds(sqLiteDatabase);
            }
            if (oldVersion == 2 && newVersion == 3) {
                billsAddExpImp(sqLiteDatabase);
            }
            if (oldVersion == 3 && newVersion == 4) {
                billsAddExpImp(sqLiteDatabase);
            }
        }

        private void billsAddExpImp(SQLiteDatabase sqLiteDatabase) {
            String DB_DDL =
                    " ALTER TABLE bills "
                            + " add  "
                            + " exp_imp decimal(1) default 0"
                            + "; ";
            try {
                sqLiteDatabase.execSQL(DB_DDL);
            } catch (Exception ex) {
                Log.e("", ex.toString());
            }
        }

        private void createKinds(SQLiteDatabase sqLiteDatabase) {
            String DB_DDL =
                    " CREATE TABLE kinds ( "
                            + " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + " name VARCHAR(250) NOT NULL DEFAULT '' "
                            + "); ";
            sqLiteDatabase.execSQL(DB_DDL);
        }
    }


}
