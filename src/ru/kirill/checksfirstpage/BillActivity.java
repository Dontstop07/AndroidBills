package ru.kirill.checksfirstpage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.DialogInterface;
import android.database.Cursor;
import android.view.Gravity;
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
    String[] kinds;
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
    private Db db;
    Cursor cursor;
	public static int editMode = 0; //0 - добавление, 1 - редактирование
	public static BillDto billDto;
	static {
		billDto = new BillDto();
		billDto.cash = "";
		billDto.payDate = new Date();
		billDto.kind = "";
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bill);

		etSum = (EditText) findViewById(R.id.etSum);
		etSum.setText(billDto.cash);
		
		tvDate = (TextView) findViewById(R.id.tvDate);
        datePayDisplay();

        db = new Db(this);
        db.open();


        cursor = db.getAllKindData();
        kinds = new String[cursor.getCount()];
        cursor.moveToFirst();

            int nameColIndex = cursor.getColumnIndex("name");
            int index = 0;
            do {
                kinds[index] = cursor.getString(nameColIndex);
                index++;
            } while (cursor.moveToNext());

        int selection = -1;
		for (int i = 0; i<kinds.length; i++){
			String s = kinds[i];
			if (billDto.kind.equals(s)) {
                selection = i;
				break;
			}
		}

        if(selection == -1 && billDto.kind != null && ! billDto.kind.trim().equals("")) {
            String[] expandedKinds = Arrays.copyOf(kinds, kinds.length+1, String[].class);
            expandedKinds[expandedKinds.length-1] = billDto.kind;
            selection = expandedKinds.length-1;
            kinds = expandedKinds;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, kinds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		sKind = (Spinner) findViewById(R.id.sExpType);
        sKind.setPrompt("Вид затрат");
        sKind.setAdapter(adapter);
        sKind.setSelection(selection);

		inputDesc = (EditText) findViewById(R.id.inputDesc);
		inputDesc.setText(billDto.description);		
		
		tvLastBill = (TextView) findViewById(R.id.tvLastBill);
		
		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(this);
		// адаптер

	}

    private void datePayDisplay() {
        SimpleDateFormat ft = new SimpleDateFormat ("dd.MM.yyyy");
        tvDate.setText(ft.format(billDto.payDate));
    }

    DatePickerDialog tpd;
    boolean myCallBackDatePicked;

    @SuppressWarnings("deprecation")
	public void onclick(View view) {
        myCallBackDatePicked = false;
        if(tpd != null) {
            setDatePayInDateDialog();
        }
		showDialog(DIALOG_DATE);
	}


	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_DATE) {
            tpd = new DatePickerDialog(this, myCallBack, 1, 0, 1) {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    myCallBackDatePicked = true;
                    super.onClick(dialog, which);
                    myCallBackDatePicked = false;
                }
            };
            setDatePayInDateDialog();
			return tpd;
		}
		return super.onCreateDialog(id);
	}

    private void setDatePayInDateDialog() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(billDto.payDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day  = calendar.get(Calendar.DAY_OF_MONTH);
        tpd.updateDate(year, month, day);
    }

    OnDateSetListener myCallBack = new OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
            if(! myCallBackDatePicked ) {
                return;
            }
			Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
			billDto.payDate = calendar.getTime();
//			myYear = year;
//			myMonth = monthOfYear+1;
//			myDay = dayOfMonth;
            datePayDisplay();
//			tvDate.setText("" + myDay + "/" + myMonth + "/" + myYear);
		}
	};


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnSave:
			billDto.cash = etSum.getText().toString();
			billDto.kind = kinds[sKind.getSelectedItemPosition()];
			billDto.description = inputDesc.getText().toString();
			// Начало проверки
			float summa=0;
            try {
                if( ! billDto.cash.isEmpty()) {
                    summa = Float.parseFloat(billDto.cash);
                }
            } catch (NumberFormatException e) {
                // Еслипроизойдёёт ошибка преобразования строки в число то значит сумма будет равна нулю
                billDto.cash = "";
                etSum.setText(billDto.cash);
            }
			if (summa > -0.01 && summa < 0.01) {
				Toast toast = Toast.makeText(this, "Сумма не должна равняться нулю", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
				toast.show();
                etSum.requestFocus(); // Установим фокус ввода в поле суммы
				return;
			}
			// Конец проверки

            // editMode = 0; //0 - добавление, 1 - редактирование
	        if (editMode == 0) {
                // кнопка ОК
                billDto.inputDate = null;
                billDto.uuid = null;
	            db.insert(billDto);
                tvLastBill.setText("" + billDto.kind + " " + billDto.cash);
                billDto.cash = "";
                billDto.description = "";
                etSum.setText(billDto.cash);
                inputDesc.setText(billDto.description);
                etSum.requestFocus();  // Установим фокус ввода в поле суммы
	        } else {
                billDto.inputDate = new Date(); // Изменим дату ввода чека на текущую
	        	db.edit(billDto);
                finish(); // закроем текущую activity
	        }

			break;
		}

	}

}


