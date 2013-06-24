package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import ru.kirill.checksfirstpage.db.Db;

/**
 * Created by K on 12.06.13.
 */
public class FilterKindsActivity extends Activity implements View.OnClickListener {
    Button btnFilterUse;
    private ListView lvData;
    private String[] kinds;
    private Db db;
    Cursor cursor;
    public static int year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kind_filter);

        btnFilterUse = (Button) findViewById(R.id.btnFilterUse);

        btnFilterUse.setOnClickListener(this);

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = db.getKindsFromBills(year);
        if (cursor.moveToFirst()) {
            kinds = new String[cursor.getCount()];
            // определ€ем номера столбцов по имени в выборке
            int kindColIndex = cursor.getColumnIndex("kind");
            int index = 0;
            do {
                kinds[index] = cursor.getString(kindColIndex);
                index++;
                // переход на следующую строку
                // а если следующей нет (текуща€ - последн€€), то false - выходим из цикла
            } while (cursor.moveToNext());
        } else {
            kinds = new String[] {"нет ни одного вида затрат"};
        }
        cursor.close();
        db.close();
        lvData = (ListView) findViewById(R.id.lvData);
        // устанавливаем режим выбора пунктов списка
        lvData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // —оздаем адаптер, использу€ массив из файла ресурсов

        //kinds = new String[]{"≈да","ќбед"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_multiple_choice, kinds);
        lvData.setAdapter(adapter);

        if(db.selectedKinds != null && db.selectedKinds.length > 0 ) {
            for(int i=0; i < kinds.length; i++) {
                String currentKind = kinds[i];
                for(int j=0; j < db.selectedKinds.length; j++ ) {
                    String selectedKind = db.selectedKinds[j];
                    if(selectedKind.equals(currentKind)) {
                        lvData.setItemChecked(i, true);
                        break;
                    }
                }
            }

        }

    }

        public void onClick(View arg0) {
            // пишем в лог выделенные элементы
            ArrayList<String> checkedKinds = new ArrayList<String>(kinds.length);
            SparseBooleanArray sbArray = lvData.getCheckedItemPositions();
            for (int i = 0; i < sbArray.size(); i++) {
                    int key = sbArray.keyAt(i);
                if (sbArray.get(key)) {
                    Toast.makeText(this, kinds[key], Toast.LENGTH_SHORT).show();
                    checkedKinds.add(kinds[key]);
                }
            }
            db.selectedKinds = checkedKinds.toArray(new String[0]);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish(); // закроем текущую activity
        }
}
