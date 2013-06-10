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
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
        menu.add(0, CM_EDIT_ID, 0, R.string.edit_record);
	}

	public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            db.delRecKind(acmi.id);
            // обновляем курсор
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
            // извлекаем id записи и удаляем соответствующую запись в БД
            Toast.makeText(this, "редактирование", Toast.LENGTH_SHORT).show();
            etNewKind.requestFocus();

            // обновляем курсор
            // вероятно обновление курсора нужно поместить в обработчик события которое происходит
            // когда текущее активити станет опять станет активным
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
            String message = null;
            kindDto.name = etNewKind.getText().toString();
            if( kindDto.name.isEmpty()) {
                message = "введите название";
            } else {
                if (editMode == 1) {
                    KindDto oldValue = db.getKind(Long.parseLong(kindDto.id));
                    if(oldValue.name.equals(kindDto.name)) {
                        сlearKindDto(etNewKind);
                        return;
                    }
                }
                KindDto dublicate = db.getKindByName(kindDto.name);
                if (dublicate != null) {
                    message = "такое название уже существует";
                }
            }
            if (message != null) {
                Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
                toast.show();
                etNewKind.requestFocus(); // Установим фокус ввода в поле вида
                return;
            }
            Toast.makeText(this, "Проверка прошла успешно", Toast.LENGTH_SHORT);
                    if (editMode == 0) {
                        db.insertKind(kindDto);
                        сlearKindDto(etNewKind);
                        Toast.makeText(this, "Элемент добавлен", Toast.LENGTH_SHORT);
                    } else {
                        db.editKind(kindDto);
                        Toast.makeText(this, "Элемент изменен", Toast.LENGTH_SHORT);
                        сlearKindDto(etNewKind);

                    }
		    // уведомляем, что данные изменились
            cursor.requery();
		   // scAdapter.notifyDataSetChanged();

		}

	}

    private void сlearKindDto(EditText etNewKind) {
        kindDto.id = "-1";
        kindDto.name = "";
        etNewKind.setText(kindDto.name);
        editMode = 0;
    }
}
