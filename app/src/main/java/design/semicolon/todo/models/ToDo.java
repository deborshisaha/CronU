package design.semicolon.todo.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import design.semicolon.todo.manager.ToDoManager;

/**
 * Created by dsaha on 1/10/16.
 */
public class ToDo {

    private String id;
    private String title;
    private String description;
    private Date dueDate;

    public String getDueDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm a");
        return dateFormatter.format(this.dueDate)+ " at " + timeFormatter.format(this.dueDate);
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getUniqueId() {
        return id;
    }

    public ToDo( String title, String description, Date date) {
        this.id = new ToDoManager.ToDoUniqueIdentifierGenerator().sessionId();
        this.title = title;
        this.description = description;
        this.dueDate = date;
    }

    @Override
    public String toString() {
        return title;
    }
}
