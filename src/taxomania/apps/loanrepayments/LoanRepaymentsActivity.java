package taxomania.apps.loanrepayments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class LoanRepaymentsActivity extends Activity
{
    private EditText loan, interest, payments;
    private TextView repayment, total, charge;
    private static boolean[] decComp = new boolean[3], decimal = new boolean[3];

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        loan = (EditText) findViewById(R.id.loan);
        interest = (EditText) findViewById(R.id.interest);
        payments = (EditText) findViewById(R.id.num);
        repayment = (TextView) findViewById(R.id.repayment);
        total = (TextView) findViewById(R.id.total);
        charge = (TextView) findViewById(R.id.charge);
        setListeners(loan, true, 8, 0);
        setListeners(interest, false, 3, 1);
        setListeners(payments, false, 4, 2);
    }

    public void calc(final View view)
    {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if (!hasFields()) return;
        final List<String> list = calculate(Double.parseDouble(loan.getText().toString()),
                Double.parseDouble(interest.getText().toString()),
                Integer.parseInt(payments.getText().toString()));
        if (list == null) {
            resetResults();
            return;
        }
        repayment.setText(list.get(0));
        total.setText(list.get(1));
        charge.setText(list.get(2));
    }

    private static final double MONTHLY_CONVERSION = 1200.0;

    private static List<String> calculate(final double loan, double interest, final int term)
    {
        if (term == 0) return null;
        double amount = loan;
        if (interest != 0)
        {
            interest = interest / MONTHLY_CONVERSION;
            amount *= (interest + (interest / (Math.pow((1 + interest), term) - 1)));
        }
        final NumberFormat nf = NumberFormat.getCurrencyInstance();
        final List<String> list = new ArrayList<String>();
        list.add(nf.format(amount));
        final double total = amount * term;
        list.add(nf.format(total));
        list.add(nf.format(total - loan));
        return list;
    }

    private boolean hasFields() {
        if (TextUtils.isEmpty(loan.getText().toString())) return false;
        if (TextUtils.isEmpty(interest.getText().toString())) return false;
        if (TextUtils.isEmpty(payments.getText().toString())) return false;
        return true;
    }

    public void reset(final View view) {
        loan.setText("");
        interest.setText("");
        payments.setText("");
        resetResults();
    }
    private void resetResults(){
        repayment.setText("");
        total.setText("");
        charge.setText("");
    }

    private void setListeners(EditText et, final boolean doesEdit,
            final int len, final int index) {
        TextView.OnEditorActionListener editListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL) {
                    if (doesEdit) {
                        if (v.getText().toString().equals("")) v.setText("0.00");
                        else if (!decimal[index]) v.setText(v.getText().toString() + ".00");
                        else if (!decComp[index]) v.setText(v.getText().toString() + "0");
                    }
                    else {
                        if (v.getText().toString().equals("")) v.setText("0");
                    }
                }
                if (hasFields()) calc(v);
                else {
                    if (v.getId() == R.id.loan) interest.requestFocus();
                    else if (v.getId() == R.id.interest) payments.requestFocus();
                }
                return true;
            }
        };
        et.setOnEditorActionListener(editListener);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after){ }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains(".")) decimal[index] = true;
                else decimal[index] = false;
                if (!decimal[index]) {
                    if (str.length() == len) {
                        if (str.charAt(str.length() - 1) == '.') return;
                        s.clear();
                        final String st = str.substring(0, str.length() - 1);
                        s.append(st);
                    }
                }
                else {
                    String a = str.substring(str.indexOf(".") + 1);
                    if (a.contains(".")) {
                        a.replace(".", "");
                        str = str.substring(0, str.length() - 1);
                        str.concat(a);
                        s.clear();
                        s.append(str);
                    }
                    if (a.length() != 2) decComp[index] = false;
                    if (a.length() == 3) {
                        a = a.substring(0, 2);
                        str = str.substring(0, str.length() - 1);
                        str.concat(a);
                        s.clear();
                        s.append(str);
                        decComp[index] = true;
                    }
                    if (str.length() == (len + 3)) {
                        s.clear();
                        final String st = str.substring(0, str.length() - 1);
                        s.append(st);
                    }
                }
            }
        };
        et.addTextChangedListener(watcher);
    }
}