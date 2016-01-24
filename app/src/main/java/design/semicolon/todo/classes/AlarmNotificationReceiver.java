package design.semicolon.todo.classes;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import design.semicolon.todo.fragment.TodoDetailFragment;
import design.semicolon.todo.models.ToDo;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    AlarmNotifier alarmNotifier;

    public AlarmNotificationReceiver(AlarmNotifier alarmNotifier) {
        this.alarmNotifier = alarmNotifier;
    }

    public AlarmNotificationReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if (extras != null) {
            ToDo targetTodo = (ToDo) intent.getSerializableExtra(TodoDetailFragment.TODO_ITEM_ID);
            this.alarmNotifier.fireNotification(targetTodo);
        }
    }
}