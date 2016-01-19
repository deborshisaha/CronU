package design.semicolon.todo.manager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
    private ToDoManager() {}

    public static ToDoManager getInstance (Context context) {
        instance.ctxt = context;
        return instance;
    }

    public static ToDoManager getInstance () { return instance; }

    private Context ctxt;
    private static List<ToDo> toDoArrayList = new ArrayList<ToDo>();
    private static Map<String, ToDo> toDoHashMap = new HashMap<String, ToDo>();

    /**********************************************************************
     * Public Methods
     **********************************************************************/
    public List<ToDo> listToDos() {


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
        return true;
    }

    public boolean deleteTodoHavingKey(String key) {
        toDoHashMap.remove(key);
        for (ToDo todo: toDoArrayList) {
            if (todo.getUniqueId().equals(key)) {
                toDoArrayList.remove(todo);
                break;
            }
        }
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

    private boolean addItem(ToDo todo) {
        toDoArrayList.add(todo);
        toDoHashMap.put(todo.getUniqueId(), todo);

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

                updateToDo.setReceiver(setAlarm(updateToDo));
                toDoArrayList.add(updateToDo);

                break;
            }
        }

        toDoHashMap.remove(updateToDo.getUniqueId());
        toDoHashMap.put(updateToDo.getUniqueId(), updateToDo);

        return updateToDo;
    }

    private void deactivateAlarm(ToDo todo) {
        this.ctxt.unregisterReceiver(todo.getReceiver());
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
