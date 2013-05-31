package ru.kirill.checksfirstpage;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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

		// найдем View-элементы
		btnBill = (Button) findViewById(R.id.btnBill);
		btnBillsList = (Button) findViewById(R.id.btnBillsList);

		// присваиваем обработчик кнопкам
		btnBill.setOnClickListener(this);
		btnBillsList.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// по id определяем кнопку, вызвавшую этот обработчик
		switch (v.getId()) {
		case R.id.btnBill: {
			// кнопка ОК

            BillActivity.editMode = 0;
            Intent intent = new Intent(this, BillActivity.class);
			startActivity(intent);
			BillActivity.billDto.cash = "";
			BillActivity.billDto.payDate = new Date();
			BillActivity.billDto.kind = "";
			BillActivity.billDto.description = "";	
			
			break; }
		case R.id.btnBillsList: {
			// кнопка Cancel
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

