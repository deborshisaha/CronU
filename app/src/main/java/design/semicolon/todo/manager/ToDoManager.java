package design.semicolon.todo.manager;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import design.semicolon.todo.models.ToDo;

public class ToDoManager {

    private static final List<ToDo> toDoArrayList = new ArrayList<ToDo>();
    private static final Map<String, ToDo> toDoHashMap = new HashMap<String, ToDo>();

    public static List<ToDo> listToDos() {
        return toDoArrayList;
    }

    public static boolean deleteAll() {
        toDoArrayList.clear();
        toDoHashMap.clear();
        return true;
    }

    public static ToDo updateTodo(ToDo updateToDo) {
        return new ToDo("Title","Description", new Date());
    }

    public static boolean deleteTodo(ToDo deleteTodo) {
        return true;
    }

    public static boolean createTodo(ToDo createdToDo) {
        return addItem(createdToDo);
    }

    public static ToDo getToDoItemById(Object key) {
        return toDoHashMap.get(key);
    }

    public static final class ToDoUniqueIdentifierGenerator {
        private SecureRandom random = new SecureRandom();
        public String sessionId() {
            return new BigInteger(130, random).toString(32);
        }
    }

    private static boolean addItem(ToDo todo) {
        toDoArrayList.add(todo);
        toDoHashMap.put(todo.getUniqueId(), todo);
        return true;
    }
}
