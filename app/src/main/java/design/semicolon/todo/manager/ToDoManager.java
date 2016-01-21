package design.semicolon.todo.manager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import design.semicolon.todo.activity.TodoListActivity;
import design.semicolon.todo.classes.AlarmNotificationReceiver;
import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.models.ToDo;

public class ToDoManager {

    private static ToDoManager instance = new ToDoManager();
    private AlarmNotificationReceiver mAlarmNotificationReceiver;

    public static ToDoManager getInstance (Context context) {
        instance.ctxt = context;

        // Populate data structures
        return instance;
    }

    public static ToDoManager getInstance () {
        return instance;
    }

    private ToDoManager () { }

    private Context ctxt;
    private static List<ToDo> toDoArrayList = null;
    private static Map<String, ToDo> toDoHashMap = null;

    /**********************************************************************
     * Public Methods
     **********************************************************************/
    public List<ToDo> listToDos() {

        if (toDoArrayList == null) {
            List<ToDo> temp = new Select().from(ToDo.class).orderBy("due_date ASC").execute();
            toDoArrayList = new ArrayList<ToDo>(temp);
        }

        if (toDoHashMap == null) {

            toDoHashMap = new HashMap<String, ToDo>();

            for (ToDo todo:toDoArrayList) {
                toDoHashMap.put(todo.getUniqueId(),todo);
                setAlarm(todo);
            }
        }

        return toDoArrayList;
    }

    public void markAsDone(ToDo todoItem) {

        ToDo todo = getToDoItemById(todoItem.getUniqueId());
        todo.setDone(true);
        ToDoManager.instance.updateTodo(todo);
    }

    public void markAsUndone(ToDo todoItem) {

        ToDo todo = getToDoItemById(todoItem.getUniqueId());
        todo.setDone(false);
        ToDoManager.instance.updateTodo(todo);
    }

    public boolean deleteAll() {
        toDoArrayList.clear();
        toDoHashMap.clear();

        new Delete().from(ToDo.class).execute();

        return true;
    }

    public ToDo persistToDo(ToDo updateToDo) {

        if (updateToDo.getUniqueId() == null) {
            updateToDo.assignUniqueId();
            ToDoManager.instance.createTodo(updateToDo);
        } else {
            ToDoManager.instance.updateTodo(updateToDo);
        }

        return updateToDo;
    }

    public boolean deleteTodo(ToDo deleteTodo) {

        deleteTodoHavingKey(deleteTodo.getUniqueId());

        deleteTodo.delete();

        return true;
    }

    public ToDo getToDoItemById(Object key) {
        return toDoHashMap.get(key);
    }

    public static final class ToDoUniqueIdentifierGenerator {
        private SecureRandom random = new SecureRandom();
        public String sessionId() {
            return new BigInteger(100, random).toString(32);
        }
    }

    /**********************************************************************
     * Private
     **********************************************************************/

    private boolean deleteTodoHavingKey(String key) {
        toDoHashMap.remove(key);
        for (ToDo todo: toDoArrayList) {
            if (todo.getUniqueId().equals(key)) {
                toDoArrayList.remove(todo);
                break;
            }
        }
        return true;
    }

    private boolean addItem(ToDo todo) {

        // In memory for fast retrieval
        toDoArrayList.add(todo);
        toDoHashMap.put(todo.getUniqueId(), todo);

        // Persist
        todo.save();

        return true;
    }

    private boolean createTodo(ToDo createdToDo) {

        if (toDoHashMap.get(createdToDo.getUniqueId())!= null) {
            return false;
        }

        setAlarm(createdToDo);
        return addItem(createdToDo);
    }

    private ToDo updateTodo(ToDo updateToDo) {

        for (ToDo todo:toDoArrayList) {
            if (todo.getUniqueId().equals(updateToDo.getUniqueId())){

                deactivateAlarm(todo);
                toDoArrayList.remove(todo);
                todo.delete();

                updateToDo.setReceiver(setAlarm(updateToDo));
                toDoArrayList.add(updateToDo);
                updateToDo.save();

                break;
            }
        }

        toDoHashMap.remove(updateToDo.getUniqueId());
        toDoHashMap.put(updateToDo.getUniqueId(), updateToDo);

        return updateToDo;
    }

    private void deactivateAlarm(ToDo todo) {

        AlarmNotificationReceiver alarmNotificationReceiver = todo.getReceiver();

        if (alarmNotificationReceiver != null) {
            this.ctxt.unregisterReceiver(todo.getReceiver());
        }
    }

    private AlarmNotificationReceiver setAlarm(final ToDo todo) {

        Calendar targetCal = new GregorianCalendar();
        targetCal.setTime(todo.getDueDate());

        Intent intent = new Intent("design.semicolon.todo.AlarmNotificationReceiver."+todo.getUniqueId());
        intent.putExtra(TodoDetailFragment.TODO_ITEM_ID, todo);

        IntentFilter filter = new IntentFilter("design.semicolon.todo.AlarmNotificationReceiver."+todo.getUniqueId());
        AlarmNotificationReceiver receiver = new AlarmNotificationReceiver((TodoListActivity)this.ctxt);
        this.ctxt.registerReceiver(receiver, filter);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.ctxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) this.ctxt.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, targetCal.getTimeInMillis(), pendingIntent);

        return receiver;
    }
}
