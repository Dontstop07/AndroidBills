package ru.kirill.checksfirstpage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.kirill.checksfirstpage.db.Db;
import ru.kirill.checksfirstpage.dto.BillDto;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BillActivity extends Activity implements OnClickListener {

	static String[] expType = {"обед", "бензин", "еда", "одежда"};
	TextView tvDate;
	int DIALOG_DATE = 1;
	int myYear = 2013;
	int myMonth = 05;
	int myDay = 18;
	Button btnSave;
	private TextView tvLastBill;
	private EditText etSum;
	private Spinner sKind;
	private EditText inputDesc;
	public static int editMode = 0; //0 - добавление, 1 - редактирование
	public static BillDto billDto;
	static {
		billDto = new BillDto();
		billDto.cash = "0";
		billDto.payDate = new Date();
		billDto.kind = expType[2];
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill);

		etSum = (EditText) findViewById(R.id.etSum);
		etSum.setText(billDto.cash);
		
		tvDate = (TextView) findViewById(R.id.tvDate);
		SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy");
		tvDate.setText(ft.format(billDto.payDate));
		
		sKind = (Spinner) findViewById(R.id.sExpType);
		for (int i = 0; i<expType.length; i++){
			String s = expType[i];
			if (billDto.kind.equals(s)) {
				sKind.setSelection(i);
				break;
			}
		}
		
		inputDesc = (EditText) findViewById(R.id.inputDesc);
		inputDesc.setText(billDto.description);		
		
		tvLastBill = (TextView) findViewById(R.id.tvLastBill);
		
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(this);
		// адаптер
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, expType);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner spinner = (Spinner) findViewById(R.id.sExpType);
		spinner.setAdapter(adapter);
		// заголовок
		spinner.setPrompt("Вид затрат");
		// выделяем элемент 
		spinner.setSelection(0);
		// устанавливаем обработчик нажатия
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// показываем позиция нажатого элемента
				Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

	}
	@SuppressWarnings("deprecation")
	public void onclick(View view) {
		showDialog(DIALOG_DATE);
	}


	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_DATE) {
			DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
			return tpd;
		}
		return super.onCreateDialog(id);
	}

	OnDateSetListener myCallBack = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
			billDto.payDate = calendar.getTime();
			myYear = year;
			myMonth = monthOfYear+1;
			myDay = dayOfMonth;
			tvDate.setText("" + myDay + "/" + myMonth + "/" + myYear);
		}
	};


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnSave:
			billDto.cash = etSum.getText().toString();
			billDto.kind = expType[sKind.getSelectedItemPosition()];
			billDto.description = inputDesc.getText().toString();
			// Начало проверки
			float summa;
			summa = Float.parseFloat(billDto.cash);
			if (summa > -0.01 && summa < 0.01) {
				Toast toast = Toast.makeText(this, "сумма не должна равняться нулю", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			// Конец проверки
	        Db db = new Db(this);
	        db.open();
	        if (editMode == 0) {
	              	db.insert(billDto);
	        } else {
	        	db.edit(billDto);
	        }
	        
            
            // editMode = 0; //0 - добавление, 1 - редактирование
            db.close();

			tvLastBill.setText("" + billDto.kind + " " + billDto.cash);
			billDto.cash = "0";
			billDto.description = "";
			etSum.setText(billDto.cash);
			inputDesc.setText(billDto.description);
			// кнопка ОК
			break;
		case R.id.btnCancel:
			// кнопка Cancel
			break;
		}
	}
}


