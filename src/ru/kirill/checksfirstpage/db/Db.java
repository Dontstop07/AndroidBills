

package ru.kirill.checksfirstpage.db;

import java.sql.Timestamp;
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
    public static final String COLUMN_CAPTION = "caption";
    public static final String COLUMN_KIND = "kind";
    public static final String COLUMN_EXP_IMP = "exp_imp";
    private final Context ctx;
    private DBHelper dbHelper;
    private final int DB_VERSION = 6;
    private final String DB_NAME = "myDb";
    private SQLiteDatabase mDb;
    public static String[] selectedKinds;

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
        edit(billDto, false);
    }

    public void edit(BillDto billDto, boolean isImported) {
        ContentValues cv = getFilledContentValues(billDto);
        if( ! isImported ) {
            // Не импортирован - значит введён вручную на этом телефоне
            cv.put("exp_imp", "0"); // - чек может быть выгружен на почту
        }
        mDb.update("bills", cv, "_id=?", new String[] {billDto.id});
    }

    public BillDto getBillDtoByUuid(String uuid) {
        String[] whereParamaters = new String[] {uuid};
        String where = "uuid = ?"; // при выборе записей из таблицы bills
        // вместо "вопросика" будет использовано
        // значение находящееся в массиве whereParamaters
        Cursor cursor = mDb.query("bills", null, where, whereParamaters, null, null, null);
        BillDto result = null;
        if(cursor.moveToNext() ) {
            result = new BillDto();
            fillBillFields(cursor, result);
        }
        cursor.close();
        return result;
    }

    public BillDto get(long id) {
        String[] whereParamaters = new String[] {""+id};
        String where = "_id = ?"; // при выборе записей из таблицы bills
        // вместо "вопросика" будет использовано
        // значение находящееся в массиве whereParamaters
        Cursor cursor = mDb.query("bills", null, where, whereParamaters, null, null, "pay_date, cash");
        BillDto result = null;
        if(cursor.moveToNext() ) {
            result = new BillDto();
            //result.payDate = cursor.getString(cursor.getColumnIndex("pay_date"));
            fillBillFields(cursor, result);
        }
        return result;
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
        if( dto.inputDate != null ) {
            //  Поле заполнено в случае импорта чеков
            cv.put("input_date", dateFormat.format(dto.inputDate));
        }
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
        String sInputDate = getField(cursor, "input_date");

        try {
            result.inputDate = dateFormat.parse(sInputDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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
        KindDto result = null;
        if(cursor.moveToNext() ) {
            result = new KindDto();
            result.id = getField(cursor, "_id");
            result.name = getField(cursor, "name");
        }
        cursor.close();
        return result;
    }

    //сохранить запись
    public void editKind(KindDto kindDto) {
        mDb.update("kinds", getKindFilledContentValues(kindDto), "_id=?", new String[] {kindDto.id});
    }

    //удалить запись
    public void delRecKind(long id) {
        mDb.delete("kinds", "_id=?", new String[] {""+id});
    }

    //получить список всех Kinds


    public Cursor getAllKindData() {
        return mDb.query("kinds", null, null, null, null, null, "position, name");
    }

    public KindDto getKindByName(String name) {

        String[] whereParamaters = new String[] {name.toUpperCase()};
        String where = "upper (name) = ?"; // при выборе записей из таблицы bills
        // вместо "вопросика" будет использовано
        // значение находящееся в массиве whereParamaters
        Cursor cursor = mDb.query("kinds", null, where, whereParamaters, null, null, "name");
        KindDto result = extractKindDto(cursor);
        cursor.close();
        return result;
    }

    private KindDto extractKindDto(Cursor cursor) {
        if(cursor.moveToNext() ) {
            KindDto result = new KindDto();
            result.id = getField(cursor, "_id");
            result.name = getField(cursor, "name");
            return result;
        }
        return null;
    }

    private ContentValues getKindFilledContentValues(KindDto dto) {
        ContentValues cv = new ContentValues();
        cv.put("name", dto.name);
        cv.put("position", ""+dto.position);
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

    public Cursor getDataByYears() {
        return mDb.rawQuery("SELECT sum(cash) as cash, strftime('%Y', pay_date) as _id \n" +
                "FROM bills \n" +
//              "where kind in ('ggg', 'hhh')" +
                getSelectedKinds("where") +
                "group by strftime('%Y', pay_date)\n" +
                "order by 2 desc\n", null);
    }

    public Cursor getDataByYearMonths(int year) {
        String[] params = {
                ""+year+"00", ""+year+"12"
        };
        return mDb.rawQuery("SELECT sum(cash) as cash, cast(strftime('%m', pay_date) as number) as _id \n" +
                "FROM bills where pay_date_year_month between ? and ? \n" +
                getSelectedKinds("and") +
                "group by strftime('%m', pay_date)\n", params);
    }

    private String getSelectedKinds(String prefix) {
        String sKinds = "";
        if (selectedKinds != null && selectedKinds.length > 0) {
            sKinds = "";
            for (String kind : selectedKinds) {
                if (sKinds.length() > 0) {
                    sKinds = sKinds + ", ";
                }
                sKinds = sKinds + "'" + kind + "'";
            }
            sKinds = " " + prefix + " kind in (" + sKinds + ") ";
        }
        return sKinds;
    }

    public Cursor getDataByYearMonthKinds(int year, int month) {
        String[] params = {
                ""+(year*100+month)
        };
        return mDb.rawQuery("SELECT sum(cash) as cash, kind as _id \n" +
                "FROM bills where pay_date_year_month = ? \n" +
                getSelectedKinds("and") +
                "group by kind \n" +
                "order by 1 desc", params);
    }

    public Cursor getFilteredData(int selectedYear, int selectedMonth, String selectedKind) {
        String[] params = {
                ""+(selectedYear*100+selectedMonth),
                selectedKind
        };
        return mDb.rawQuery("SELECT * \n" +
                "FROM bills where pay_date_year_month = ? and kind = ?\n" +
                "order by pay_date, cash", params);
    }

    public Cursor getKindsFromBills(int year) {
        if (year == -1) {
            return mDb.rawQuery("Select distinct kind from bills", null);
        } else {
            String[] params = {
                    ""+year+"00", ""+year+"12"
            };

            return mDb.rawQuery("Select distinct kind from bills \n" +
                    "where pay_date_year_month between ? and ?", params);
        }
    }

    public int getMaxKindPosition() {
        Cursor cursor = mDb.rawQuery("select max(position) from kinds", null);
        int result = 0;
        if(cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
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
            if (oldVersion == 1 && newVersion >= 2) {
                createKinds(sqLiteDatabase);
                oldVersion++;
            }

            if (oldVersion == 2 && newVersion >= 3) {
                billsAddExpImp(sqLiteDatabase);
                oldVersion++;
            }

            if (oldVersion == 3 && newVersion >= 4) {
                billsAddExpImp(sqLiteDatabase);
                oldVersion++;
            }

            if (oldVersion == 4 && newVersion >= 5) {
                kindsAddPosition(sqLiteDatabase);
                oldVersion++;
            }

            if (oldVersion == 5 && newVersion >= 6) {
                kindsUpdatePositions(sqLiteDatabase);
                oldVersion++;
            }
        }

        private void billsAddExpImp(SQLiteDatabase sqLiteDatabase) {
            String DB_DDL =
                    " ALTER TABLE bills "
                            + " add  "
                            + " exp_imp decimal(1) default 0"
                            + "; ";
            sqLiteDatabase.beginTransaction();
            try{
                sqLiteDatabase.execSQL(DB_DDL);
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception nothing) {
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }

        private void kindsAddPosition(SQLiteDatabase sqLiteDatabase) {
            String DB_DDL =
                    " ALTER TABLE kinds "
                            + " add  "
                            + " position DECIMAL(5) NOT NULL DEFAULT 0"
                            + "; ";
            sqLiteDatabase.beginTransaction();
            try{
                sqLiteDatabase.execSQL(DB_DDL);
                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception nothing) {
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }

        private void kindsUpdatePositions(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.beginTransaction();
            try{
                Cursor cursor = sqLiteDatabase.query("kinds", null, null, null, null, null, "position, name");

                int i = 1;
                int fIdIdx = cursor.getColumnIndex("_id");
                cursor.moveToFirst();
                String[] params = new String[2];
                final String SQL = " update kinds "
                        + " set position = ?"
                        + " where _id = ? ";
                do {
                    params[0] = ""+ i;
                    params[1] = cursor.getString(fIdIdx);
                    i++;
                    sqLiteDatabase.execSQL(SQL, params);
                } while(cursor.moveToNext());

                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception nothing) {
                Log.d("DB", nothing.toString());
            } finally {
                sqLiteDatabase.endTransaction();
            }
        }

        private void createKinds(SQLiteDatabase sqLiteDatabase) {
            String DB_DDL =
                    " CREATE TABLE kinds ( "
                            + " _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + " name VARCHAR(250) NOT NULL DEFAULT '', "
                            + " position DECIMAL(5) NOT NULL DEFAULT 0"
                            + "); ";
            sqLiteDatabase.execSQL(DB_DDL);
        }
    }
}
