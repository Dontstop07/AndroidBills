package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.dto.KindDto;

public class KindsListActivityDb extends Activity implements OnClickListener  {

	final String LOG_TAG = "myLogs";
	final String ATTRIBUTE_NAME_TEXT = "text";

	ListView lvKinds;
	Button btnSaveKind;
	ArrayList<Map<String, Object>> kinds;
	String[] expTypes = {"обед", "бензин", "еда", "одежда"};
    private Db db;
    SimpleCursorAdapter scAdapter;
    Cursor cursor;



    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kindslist);

		// найдем View-элементы
		btnSaveKind = (Button) findViewById(R.id.btnSaveKind);

		// присваиваем обработчик кнопкам
		btnSaveKind.setOnClickListener(this);

		// упаковываем данные в понятную для адаптера структуру
		kinds = new ArrayList<Map<String, Object>>(
				expTypes.length);

		for (int i = 0; i < expTypes.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ATTRIBUTE_NAME_TEXT, expTypes[i]);
			kinds.add(map);
		}
		
		// массив имен атрибутов, из которых будут читаться данные
	    String[] from = { "name" };
	    // массив ID View-компонентов, в которые будут вставлять данные
	    int[] to = { R.id.tvKindsItem };

        db = new Db(this);
        db.open();

        // получаем курсор
        cursor = db.getAllKindData();
        startManagingCursor(cursor);

		// создаем адаптер
        scAdapter = new SimpleCursorAdapter(this, R.layout.kinds_list_item, cursor, from, to);

            // определяем список и присваиваем ему адаптер

		lvKinds = (ListView) findViewById(R.id.lvKinds);
		lvKinds.setAdapter(scAdapter);
		registerForContextMenu(lvKinds);

	}
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 0, 0, R.string.edit_record);
	}

	public boolean onContextItemSelected(MenuItem item) {
		/* if (item.getItemId() == CM_DELETE_ID) {
		      // получаем из пункта контекстного меню данные по пункту списка 
		      AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		      // извлекаем id записи и удаляем соответствующую запись в БД
		      db.delRec(acmi.id);
		      // обновляем курсор
		      cursor.requery();
		      return true;
		    }*/
		return super.onContextItemSelected(item);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnSaveKind:
			EditText etNewKind = (EditText) findViewById(R.id.etNewKind);
			// создаем новый Map
		    //Map<String, Object> map = new HashMap<String, Object>();
		    //map.put(ATTRIBUTE_NAME_TEXT, etNewKind.getText());
		    KindDto kindDto = new KindDto();
            kindDto.name = etNewKind.getText().toString();
            db.insertKind(kindDto);
		    // добавляем его в коллекцию
		    //kinds.add(map);
		    // уведомляем, что данные изменились
            cursor.requery();
		   // scAdapter.notifyDataSetChanged();
		    Toast.makeText(this, "Элемент добавлен", Toast.LENGTH_SHORT);
		}

	}
}