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
    private HashMap<String, AlarmNotificationReceiver> todoToAlarm = new HashMap<String, AlarmNotificationReceiver>();

    public static ToDoManager getInstance (Context context) {
        instance.ctxt = context;
        return instance;
    }

    public static ToDoManager getInstance () {
        return instance;
    }

    private ToDoManager () { }

    private Context ctxt;

    /**********************************************************************
     * Public Methods
     **********************************************************************/
    public List<ToDo> listToDos() {
        List<ToDo> list = new Select().from(ToDo.class).orderBy("due_date ASC").execute();

        for (ToDo todo:list) {
            activateAlarm(todo);
        }

        return list;
    }

    public ToDo find(String todoUniqueId) {
        List<ToDo> todos = new Select().from(ToDo.class).where("id = ?", todoUniqueId).execute();

        if (todos == null) {
            return null;
        }

        if (todos.size() == 1) {
            return (ToDo)todos.get(0);
        } else {
            return null;
        }
    }

    public void markAsDone(ToDo todoItem) {

        todoItem.setDone(true);

        // deactivate alarm
        deactivateAlarm(todoItem);

        todoItem.save();
    }

    public void markAsUndone(ToDo todoItem) {

        todoItem.setDone(false);

        activateAlarm(todoItem);

        todoItem.save();

    }

    public void deleteAll() {

        List<ToDo> list = new Select().from(ToDo.class).execute();
        deactivateAlarmsOfTodosInList(list);

        new Delete().from(ToDo.class).execute();
    }

    public void deleteExpiredOnes() {

        List<ToDo> list = new Select().from(ToDo.class).where("due_date < ?", System.currentTimeMillis()).execute();
        deactivateAlarmsOfTodosInList(list);

        new Delete().from(ToDo.class).where("due_date < ?", System.currentTimeMillis()).execute();
    }

    public void deleteDoneItems(){
        List<ToDo> list = new Select().from(ToDo.class).where("done = ?", true).execute();
        deactivateAlarmsOfTodosInList(list);

        new Delete().from(ToDo.class).where("done = ?", true).execute();
    }

    public boolean deleteTodo(ToDo deleteTodo) {

        // deactivate alarm
        deactivateAlarm(deleteTodo);
        deleteTodo.delete();

        return true;
    }

    public static final class ToDoUniqueIdentifierGenerator {
        private SecureRandom random = new SecureRandom();
        public String sessionId() {
            return new BigInteger(100, random).toString(32);
        }
    }

    public void create(ToDo createdToDo) {

        if (createdToDo.getUniqueId() == null || createdToDo.getUniqueId().length() == 0) {
            createdToDo.assignUniqueId();
        }

        createdToDo.save();
    }

    public ToDo update(ToDo updateToDo) {

        // deactivate alarm
        deactivateAlarm(updateToDo);

        // activate alarm
        activateAlarm(updateToDo);

        updateToDo.save();

        return updateToDo;
    }

    private void deactivateAlarm(ToDo todo) {

        AlarmNotificationReceiver alarmNotificationReceiver = todoToAlarm.get(todo.getUniqueId());

        if (alarmNotificationReceiver != null) {
            this.ctxt.unregisterReceiver(alarmNotificationReceiver);
            todoToAlarm.remove(todo.getUniqueId());
        }
    }

    private void deactivateAlarmsOfTodosInList(List<ToDo> listOfTodos) {
        for (ToDo todo:listOfTodos) {
            AlarmNotificationReceiver alarmNotificationReceiver = todoToAlarm.get(todo.getUniqueId());
            if (alarmNotificationReceiver != null) {
                this.ctxt.unregisterReceiver(alarmNotificationReceiver);
                todoToAlarm.remove(todo.getUniqueId());
            }
        }
    }

    private void activateAlarm(final ToDo todo) {

        Date rightNow = new Date();

        if (rightNow.after(todo.getDueDate())) {
            return;
        }

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

        todoToAlarm.put(todo.getUniqueId(), receiver);
    }
}
