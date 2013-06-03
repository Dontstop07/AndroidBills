package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

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
    private static final int CM_DELETE_ID = 1;
    private static final int CM_EDIT_ID = 2;
    SimpleCursorAdapter scAdapter;
    Cursor cursor;
    public static int editMode = 0; //0 - добавление, 1 - редактирование
    public static KindDto kindDto;
    static {
        kindDto = new KindDto();
        kindDto.id = "";
        kindDto.name = "";
    }



    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kindslist);

		// найдем View-элементы
		btnSaveKind = (Button) findViewById(R.id.btnSaveKind);
		// присваиваем обработчик кнопкам
		btnSaveKind.setOnClickListener(this);

		// упаковываем данные в пон€тную дл€ адаптера структуру
		kinds = new ArrayList<Map<String, Object>>(
				expTypes.length);

		for (int i = 0; i < expTypes.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ATTRIBUTE_NAME_TEXT, expTypes[i]);
			kinds.add(map);
		}
		
		// массив имен атрибутов, из которых будут читатьс€ данные
	    String[] from = { "name" };
	    // массив ID View-компонентов, в которые будут вставл€ть данные
	    int[] to = { R.id.tvKindsItem };

        db = new Db(this);
        db.open();


        // получаем курсор
        cursor = db.getAllKindData();
        startManagingCursor(cursor);

		// создаем адаптер
        scAdapter = new SimpleCursorAdapter(this, R.layout.kinds_list_item, cursor, from, to);

            // определ€ем список и присваиваем ему адаптер

		lvKinds = (ListView) findViewById(R.id.lvKinds);
		lvKinds.setAdapter(scAdapter);
		registerForContextMenu(lvKinds);

	}
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
        menu.add(0, CM_EDIT_ID, 0, R.string.edit_record);
	}

	public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удал€ем соответствующую запись в Ѕƒ
            db.delRecKind(acmi.id);
            // обновл€ем курсор
            cursor.requery();
            return true;
        } else if (item.getItemId() == CM_EDIT_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            long id = acmi.id;
            kindDto = db.getKind(id);
            EditText etNewKind = (EditText) findViewById(R.id.etNewKind);
            etNewKind.setText(kindDto.name);
            editMode = 1;
            // извлекаем id записи и удал€ем соответствующую запись в Ѕƒ
            Toast.makeText(this, "редактирование", Toast.LENGTH_SHORT).show();
            etNewKind.requestFocus();

            // обновл€ем курсор
            // веро€тно обновление курсора нужно поместить в обработчик событи€ которое происходит
            // когда текущее активити станет оп€ть станет активным
            // cursor.requery();
            return true;
        }
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
            kindDto.name = etNewKind.getText().toString();
            if( kindDto.name.isEmpty()) {
                Toast toast = Toast.makeText(this, "—умма не должна равн€тьс€ нулю", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
                toast.show();
                etNewKind.requestFocus(); // ”становим фокус ввода в поле вида
                return;
            }
            Toast.makeText(this, "ѕроверка прошла успешно", Toast.LENGTH_SHORT);
                    if (editMode == 0) {
                        db.insertKind(kindDto);
                        kindDto.name = "";
                        etNewKind.setText(kindDto.name);
                        Toast.makeText(this, "Ёлемент добавлен", Toast.LENGTH_SHORT);
                    } else {
                        db.editKind(kindDto);
                        Toast.makeText(this, "Ёлемент изменен", Toast.LENGTH_SHORT);
                        kindDto.id = "-1";
                        kindDto.name = "";
                        editMode = 0;
                    }
		    // уведомл€ем, что данные изменились
            cursor.requery();
		   // scAdapter.notifyDataSetChanged();

		}

	}
}
