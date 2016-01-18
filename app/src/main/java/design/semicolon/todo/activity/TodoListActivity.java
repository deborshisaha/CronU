package design.semicolon.todo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import design.semicolon.todo.R;
import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.manager.ToDoManager;
import design.semicolon.todo.models.ToDo;

public class TodoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onResume(){
        super.onResume();
        ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton addTodoFab = (FloatingActionButton) findViewById(R.id.add);
        addTodoFab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TodoFormActivity.class);
                context.startActivity(intent);
            }
        });

        /*
        addTodoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TodoListActivity.this);
                builder.setTitle("New to-do");

                final EditText toDoItemInput = new EditText(TodoListActivity.this);
                toDoItemInput.requestFocus();
                toDoItemInput.setInputType(InputType.TYPE_CLASS_TEXT |  InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(toDoItemInput, InputMethodManager.SHOW_FORCED);

                builder.setView(toDoItemInput);

                builder.setPositiveButton("Set time", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final Calendar c = Calendar.getInstance();

                        DatePickerDialog datePickerDialog = new DatePickerDialog(TodoListActivity.this,

                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker datePickerView, final int year,
                                                          final int monthOfYear, final int dayOfMonth) {

                                        if (!datePickerView.isShown()) {
                                            return;
                                        }

                                        TimePickerDialog timePickerDialog = new TimePickerDialog(TodoListActivity.this,
                                                new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker timePickerView, final int hourOfDay, final int minute) {

                                                        if (timePickerView.isShown()) {
                                                            return;
                                                        }

                                                        Calendar alarmDateTime = new GregorianCalendar(year,monthOfYear,dayOfMonth,hourOfDay,minute,0);

                                                        ToDo createdTodo = new ToDo(toDoItemInput.getText().toString(), "Saha", alarmDateTime.getTime() );
                                                        ToDoManager.createTodo(createdTodo);

                                                        ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).notifyDataSetChanged();

                                                    }
                                                }, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false);

                                        timePickerDialog.show();

                                    }
                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                        datePickerDialog.show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        */
        FloatingActionButton deleteAllTodosFab = (FloatingActionButton) findViewById(R.id.delete_all);
        deleteAllTodosFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TodoListActivity.this);
                alertDialogBuilder.setMessage("Are you sure you want to delete all your Todos?");
                alertDialogBuilder.setPositiveButton("No", null);
                alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (ToDoManager.getInstance(TodoListActivity.this).deleteAll()) {
                            ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).notifyDataSetChanged();
                            Toast.makeText(TodoListActivity.this, "All Todos were erased", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.todo_list);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(ToDoManager.getInstance(TodoListActivity.this).listToDos()));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<ToDo> todoItems;

        public SimpleItemRecyclerViewAdapter(List<ToDo> todoItems) {
            this.todoItems = todoItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.todo_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            // The ToDo Item of importance
            holder.toDoItem = this.todoItems.get(position);

            // Setting the content
            holder.mTitleView.setText(this.todoItems.get(position).getTitle());
            holder.mSubtitleView.setText(this.todoItems.get(position).getDueDateReadableFormat());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, TodoFormActivity.class);
                    intent.putExtra(TodoDetailFragment.TODO_ITEM_ID, holder.toDoItem);
                    context.startActivity(intent);
                }
            });

            if (holder.toDoItem.markedAsDone()) {
                holder.mMarkTodoButton.setBackgroundResource(R.drawable.checked_checkbox);
            } else {
                holder.mMarkTodoButton.setBackgroundResource(R.drawable.unchecked_checkbox);
            }
        }

        @Override
        public int getItemCount() {
            return this.todoItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mSubtitleView;
            public final TextView mTitleView;
            public final ImageButton mTrashTodoButton;
            public final ImageButton mMarkTodoButton;

            public ToDo toDoItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.title);
                mSubtitleView = (TextView) view.findViewById(R.id.subtitle);
                //mTodoIcon = (ImageView) view.findViewById(R.id.image);

                mTrashTodoButton = (ImageButton) view.findViewById(R.id.delete);
                mTrashTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToDoManager.getInstance().deleteTodoHavingKey(toDoItem.getUniqueId());
                        ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).notifyDataSetChanged();
                    }
                });

                mMarkTodoButton = (ImageButton) view.findViewById(R.id.mark_done);
                mMarkTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ToDoManager.getInstance().deleteTodoHavingKey(toDoItem.getUniqueId());
                        if (toDoItem.markedAsDone()) {
                            ToDoManager.getInstance().markAsUndone(toDoItem);
                        } else {
                            ToDoManager.getInstance().markAsDone(toDoItem);
                        }

                        // Refresh
                        ((SimpleItemRecyclerViewAdapter) recyclerView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }
}
