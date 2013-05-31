package ru.kirill.checksfirstpage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.DialogInterface;
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

	static String[] expType = {"����", "������", "���", "������"};
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
	public static int editMode = 0; //0 - ����������, 1 - ��������������
	public static BillDto billDto;
	static {
		billDto = new BillDto();
		billDto.cash = "";
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
        datePayDisplay();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, expType);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		sKind = (Spinner) findViewById(R.id.sExpType);
        sKind.setPrompt("��� ������");
        sKind.setAdapter(adapter);
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
		// �������

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
			billDto.kind = expType[sKind.getSelectedItemPosition()];
			billDto.description = inputDesc.getText().toString();
			// ������ ��������
			float summa=0;
            try {
                if( ! billDto.cash.isEmpty()) {
                    summa = Float.parseFloat(billDto.cash);
                }
            } catch (NumberFormatException e) {
                // �����������丸� ������ �������������� ������ � ����� �� ������ ����� ����� ����� ����
                billDto.cash = "";
                etSum.setText(billDto.cash);
            }
			if (summa > -0.01 && summa < 0.01) {
				Toast toast = Toast.makeText(this, "����� �� ������ ��������� ����", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
				toast.show();
                etSum.requestFocus(); // ��������� ����� ����� � ���� �����
				return;
			}
			// ����� ��������
	        Db db = new Db(this);
	        db.open();
            // editMode = 0; //0 - ����������, 1 - ��������������
	        if (editMode == 0) {
                // ������ ��
	            db.insert(billDto);
                tvLastBill.setText("" + billDto.kind + " " + billDto.cash);
                billDto.cash = "";
                billDto.description = "";
                etSum.setText(billDto.cash);
                inputDesc.setText(billDto.description);
                etSum.requestFocus();  // ��������� ����� ����� � ���� �����
	        } else {
	        	db.edit(billDto);
                finish(); // ������� ������� activity
	        }

            db.close();

			break;
		}
	}
}


