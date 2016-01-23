package design.semicolon.todo.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import design.semicolon.todo.R;
import design.semicolon.todo.classes.AlarmNotifier;
import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.manager.ToDoManager;
import design.semicolon.todo.models.ToDo;

public class TodoListActivity extends AppCompatActivity implements AlarmNotifier {

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
        addTodoFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, TodoFormActivity.class);
                context.startActivity(intent);
            }
        });

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

                        ToDoManager.getInstance(TodoListActivity.this).deleteAll();
                        ((SimpleItemRecyclerViewAdapter) recyclerView.getAdapter()).notifyDataSetChanged();
                        Toast.makeText(TodoListActivity.this, "All Todos were erased", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        }

        recyclerView = (RecyclerView) findViewById(R.id.todo_list);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(ToDoManager.getInstance(TodoListActivity.this).listToDos()));
    }

    public void fireNotification(ToDo todo) {

        String title = todo.getName();
        String subTitle = todo.getDescription();

        if (subTitle == null || subTitle.length() == 0) {
            subTitle = "Due on "+ todo.getDueDateReadableFormat();
        }

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_btn_radio_to_on_mtrl_000)
                        .setContentTitle(title)
                        .setContentText(subTitle);

        Intent notificationIntent = new Intent(this, TodoFormActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(0, notification);
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
            holder.mTitleView.setText(this.todoItems.get(position).getName());
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

                mTrashTodoButton = (ImageButton) view.findViewById(R.id.delete);
                mTrashTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToDoManager.getInstance().deleteTodo(toDoItem);
                        ((SimpleItemRecyclerViewAdapter)recyclerView.getAdapter()).notifyDataSetChanged();
                    }
                });

                mMarkTodoButton = (ImageButton) view.findViewById(R.id.mark_done);
                mMarkTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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
