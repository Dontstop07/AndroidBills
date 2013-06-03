package ru.kirill.checksfirstpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
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
import android.widget.Toast;

public class KindsListActivity extends Activity implements OnClickListener  {

	final String LOG_TAG = "myLogs";
	final String ATTRIBUTE_NAME_TEXT = "text";

	ListView lvKinds;
	Button btnSaveKind;
	ArrayList<Map<String, Object>> kinds;
	String[] expTypes = {"����", "������", "���", "������"};
	SimpleAdapter sAdapter;



	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kindslist);

		// ������ View-��������
		btnSaveKind = (Button) findViewById(R.id.btnSaveKind);

		// ����������� ���������� �������
		btnSaveKind.setOnClickListener(this);

		// ����������� ������ � �������� ��� �������� ���������
		kinds = new ArrayList<Map<String, Object>>(
				expTypes.length);

		for (int i = 0; i < expTypes.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ATTRIBUTE_NAME_TEXT, expTypes[i]);
			kinds.add(map);
		}
		
		// ������ ���� ���������, �� ������� ����� �������� ������
	    String[] from = { ATTRIBUTE_NAME_TEXT };
	    // ������ ID View-�����������, � ������� ����� ��������� ������
	    int[] to = { R.id.tvKindsItem };


		// ������� �������
	    sAdapter = new SimpleAdapter(this, kinds, R.layout.kinds_list_item, from, to);

	    // ���������� ������ � ����������� ��� �������

		lvKinds = (ListView) findViewById(R.id.lvKinds);
		lvKinds.setAdapter(sAdapter);
		registerForContextMenu(lvKinds);

	}
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 0, 0, R.string.edit_record);
	}

	public boolean onContextItemSelected(MenuItem item) {
		/* if (item.getItemId() == CM_DELETE_ID) {
		      // �������� �� ������ ������������ ���� ������ �� ������ ������ 
		      AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		      // ��������� id ������ � ������� ��������������� ������ � ��
		      db.delRec(acmi.id);
		      // ��������� ������
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
			// ������� ����� Map
		    Map<String, Object> map = new HashMap<String, Object>();
		    map.put(ATTRIBUTE_NAME_TEXT, etNewKind.getText());
		    // ��������� ��� � ���������
		    kinds.add(map);
		    // ����������, ��� ������ ����������
		    sAdapter.notifyDataSetChanged();
		    Toast.makeText(this, "������� ��������", Toast.LENGTH_SHORT);
		}

	}
}