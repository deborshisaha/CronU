package design.semicolon.todo.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.opengl.Visibility;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import design.semicolon.todo.R;
import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.manager.ToDoManager;
import design.semicolon.todo.models.ToDo;

public class TodoFormActivity extends AppCompatActivity {

    // UI references.
    private EditText mTodoTitleEditText;
    private EditText mTodoDescriptionEditText;
    private EditText mTodoDateTimeEditText;
    private Button mSaveToDoButton;
    private MenuItem mEditTodoMenuItem;
    private ToDo targetTodo;
    private Date placeholderDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.form_menu, menu);

        mEditTodoMenuItem = (MenuItem) menu.findItem(R.id.edit_button);

        if (targetTodo != null) {
            mEditTodoMenuItem.setVisible(true);
        } else {
            mEditTodoMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_todo_form);
        mTodoTitleEditText = (EditText) findViewById(R.id.todo_name);
        mTodoDescriptionEditText = (EditText) findViewById(R.id.todo_description);
        mTodoDateTimeEditText  = (EditText) findViewById(R.id.todo_date_time);
        mSaveToDoButton = (Button) findViewById(R.id.save_todo_button);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            targetTodo = (ToDo) getIntent().getSerializableExtra(TodoDetailFragment.TODO_ITEM_ID);
        }

        if (targetTodo != null) {

            mTodoTitleEditText.setText(targetTodo.getName());
            mTodoDescriptionEditText.setText(targetTodo.getDescription());
            mTodoDateTimeEditText.setText(targetTodo.getDueDateReadableFormat());

            makeFormReadonly();
        }

        mTodoDateTimeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    final Calendar c = Calendar.getInstance();

                    DatePickerDialog datePickerDialog = new DatePickerDialog(TodoFormActivity.this,

                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker datePickerView, final int year,
                                                      final int monthOfYear, final int dayOfMonth) {

                                    if (!datePickerView.isShown()) {
                                        return;
                                    }

                                    TimePickerDialog timePickerDialog = new TimePickerDialog(TodoFormActivity.this,
                                            new TimePickerDialog.OnTimeSetListener() {

                                                @Override
                                                public void onTimeSet(TimePicker timePickerView, final int hourOfDay, final int minute) {

                                                    if (!timePickerView.isShown()) {
                                                        return;
                                                    }

                                                    Calendar alarmDateTime = new GregorianCalendar(year, monthOfYear, dayOfMonth, hourOfDay, minute, 0);
                                                    placeholderDate = alarmDateTime.getTime();

                                                    mTodoDateTimeEditText.setText(getDueDateReadableFormat(placeholderDate));
                                                    mTodoDateTimeEditText.clearFocus();
                                                }
                                            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);

                                    timePickerDialog.show();

                                }
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                    datePickerDialog.show();
                }
            }

        });


        mSaveToDoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTodo();
            }
        });

    }

    private String getDueDateReadableFormat (Date date) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        return dateFormatter.format(date)+ " at " + timeFormatter.format(date);
    }

    private void saveTodo() {

        // Boolean flag
        boolean error = false;

        // Reset errors.
        mTodoTitleEditText.setError(null);
        mTodoDescriptionEditText.setError(null);

        // Store values
        String todo_name = mTodoTitleEditText.getText().toString();
        String todo_description = mTodoDescriptionEditText.getText().toString();

        if (TextUtils.isEmpty(todo_name)) {
            error = true;
            mTodoTitleEditText.setError(getString(R.string.empty_todo_name));
        }

        if (error) return;

        if (targetTodo != null) {
            // Update the object
            targetTodo.setName(todo_name);
            targetTodo.setDescription(todo_description);

            if (placeholderDate != null) {
                targetTodo.setDate(placeholderDate);
            }

            ToDoManager.getInstance().update(targetTodo);
        } else {
            ToDo toBeCreated = new ToDo(todo_name, todo_description, placeholderDate);
            ToDoManager.getInstance().create(toBeCreated);
        }

        finish();
    }

    private void makeFormReadonly() {

        setTitle("View");

        mTodoDescriptionEditText.setFocusable(false);
        mTodoTitleEditText.setFocusable(false);
        mTodoDateTimeEditText.setFocusable(false);

        mSaveToDoButton.setVisibility(View.GONE);

        if (mEditTodoMenuItem != null) {
            mEditTodoMenuItem.setVisible(true);
        }
    }

    private void makeFormEditable() {

        setTitle("Editing");

        mTodoDateTimeEditText.setFocusable(true);
        mTodoDateTimeEditText.setFocusableInTouchMode(true);

        mTodoDescriptionEditText.setFocusable(true);
        mTodoDescriptionEditText.setFocusableInTouchMode(true);

        mTodoTitleEditText.setFocusable(true);
        mTodoTitleEditText.setFocusableInTouchMode(true);

        mSaveToDoButton.setVisibility(View.VISIBLE);

        if (mEditTodoMenuItem != null) {
            mEditTodoMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit_button:
                makeFormEditable();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

