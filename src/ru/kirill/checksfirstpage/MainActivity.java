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
	Button btnKindsList;
    Button btnBillsByYear;

	private static final String TAG = "myLogs";
    private Button btnSend;
    private Button btnReceive;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// ������ View-��������
		btnBill = (Button) findViewById(R.id.btnBill);
		btnBillsList = (Button) findViewById(R.id.btnBillsList);
		btnKindsList = (Button) findViewById(R.id.btnKindsList);
        btnBillsByYear = (Button) findViewById(R.id.btnBillsByYear);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnReceive = (Button) findViewById(R.id.btnReceive);


		// ����������� ���������� �������
		btnBill.setOnClickListener(this);
        btnBillsList.setOnClickListener(this);
		btnKindsList.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnReceive.setOnClickListener(this);
        btnBillsByYear.setOnClickListener(this);
    }

	@Override
    public void onClick(View v) {
        // �� id ���������� ������, ��������� ���� ����������
        switch (v.getId()) {
            case R.id.btnBill: {
                // ������ ��

                BillActivity.editMode = 0;
                Intent intent = new Intent(this, BillActivity.class);
                startActivity(intent);
                BillActivity.billDto.cash = "";
                BillActivity.billDto.payDate = new Date();
                BillActivity.billDto.kind = "";
                BillActivity.billDto.description = "";

                break; }
            case R.id.btnBillsList: {
                // ������ ������ �����
                Intent intent = new Intent(this, DbContentActivity.class);
                DbContentActivity.useSelected = false;
                startActivity(intent);
                break; }
            case R.id.btnKindsList: {
                // ������ Cancel
                Intent intent = new Intent(this, KindsListActivityDb.class);
                startActivity(intent);
                break;
            }
            case R.id.btnSend: {
                // ������ ��������� ����
                Intent intent = new Intent(this, SendBillsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btnReceive: {
                // ������ �������� ����
                Intent intent = new Intent(this, ReceiveBillsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btnBillsByYear: {
                // кнопка Получить чеки
                Intent intent = new Intent(this,BillsByYearActivity.class);
                startActivity(intent);
                break;
            }

        }
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}

