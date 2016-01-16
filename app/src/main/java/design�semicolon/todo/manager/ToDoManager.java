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

import design.semicolon.todo.AlarmNotificationReceiver;
import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.models.ToDo;

public class ToDoManager {

    private static ToDoManager instance = new ToDoManager();
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
                toDoArrayList.remove(todo);
                removeAlarm(todo);

                toDoArrayList.add(updateToDo);
                setAlarm(updateToDo);

                break;
            }
        }

        toDoHashMap.remove(updateToDo.getUniqueId());
        toDoHashMap.put(updateToDo.getUniqueId(), updateToDo);

        return updateToDo;
    }

    private void removeAlarm(ToDo todo) {

//        Calendar targetCal = new GregorianCalendar();
//        targetCal.setTime(todo.getDueDate());
//
//        AlarmManager alarmManager = (AlarmManager) this.ctxt.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);

    }

    private void setAlarm(final ToDo todo) {

        Calendar targetCal = new GregorianCalendar();
        targetCal.setTime(todo.getDueDate());

        Intent intent = new Intent(this.ctxt, AlarmNotificationReceiver.class);
        intent.putExtra(TodoDetailFragment.TODO_ITEM_ID, todo);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.ctxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) this.ctxt.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void fireNotification(ToDo todo) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.ctxt);

        mBuilder.setContentTitle(todo.getTitle());
        mBuilder.setContentText(todo.getDescription());

        NotificationManager todoNotificationManager = (NotificationManager)this.ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
        todoNotificationManager.notify(todo.getUniqueId(), 0, mBuilder.build());
    }
}
