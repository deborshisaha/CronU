package design.semicolon.todo.classes;

import design.semicolon.todo.models.ToDo;

public interface AlarmNotifier {
    public void fireNotification(ToDo todo);
}
