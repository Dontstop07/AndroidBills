package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.kirill.checksfirstpage.db.Db;

/**
 * Created by K on 11.06.13.
 */
public class BillsByYearActivity extends BillsByXActivity {

    @Override
    protected int getRecordItemLayout() {
        return R.layout.bills_by_year_item;
    }

    @Override
    protected Cursor getCursor() {
        return db.getDataByYears();
    }

    protected Intent onDataClick(int id) {
        Intent intent = new Intent(this, BillsByYearMonthsActivity.class);
        BillsByYearMonthsActivity.year = (int)id;
        return intent;
    }

    @Override
    protected String getHeaderText() {
        return "Список чеков по годам";
    }
}


