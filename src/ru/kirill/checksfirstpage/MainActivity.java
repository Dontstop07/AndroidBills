package ru.kirill.checksfirstpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	TextView tvOut;
	Button btnBill;
	Button btnBillsList;

	private static final String TAG = "myLogs";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// ������ View-��������
		Log.d(TAG, "������ View-��������");
		btnBill = (Button) findViewById(R.id.btnBill);
		btnBillsList = (Button) findViewById(R.id.btnBillsList);

		// ����������� ���������� �������
		Log.d(TAG, "����������� ���������� �������");
		btnBill.setOnClickListener(this);
		btnBillsList.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// �� id ���������� ������, ��������� ���� ����������
		Log.d(TAG, "�� id ���������� ������, ��������� ���� ����������");
		switch (v.getId()) {
		case R.id.btnBill: {
			// ������ ��
			Log.d(TAG, "������ ��");
			Toast.makeText(this, "������ ������ �������� ���", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, BillActivity.class);
			startActivity(intent);
			break; }
		case R.id.btnBillsList: {
			// ������ Cancel
			Toast.makeText(this, "������ ������ ������ �����", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, DbContentActivity.class);
			startActivity(intent);
			break; }
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

