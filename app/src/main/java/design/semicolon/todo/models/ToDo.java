package design.semicolon.todo.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import design.semicolon.todo.classes.AlarmNotificationReceiver;
import design.semicolon.todo.manager.ToDoManager;

/**
 * Created by dsaha on 1/10/16.
 */
public class ToDo implements Serializable {

    private String id;
    private String title;
    private String description;
    private Date dueDate;
    private boolean done;

    public AlarmNotificationReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(AlarmNotificationReceiver receiver) {
        this.receiver = receiver;
    }

    private AlarmNotificationReceiver receiver;

    public String getDueDateReadableFormat() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm a");
        return dateFormatter.format(this.dueDate)+ " at " + timeFormatter.format(this.dueDate);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setDate (Date date) { this.dueDate = date; }

    public boolean markedAsDone() { return this.done; }

    public Date getDueDate() { return dueDate; }

    public String getDescription() {
        return description;
    }

    public String getTitle() { return title; }
    
    public String getUniqueId() {
        return id;
    }

    public ToDo( String title, String description, Date date) {
        this.title = title;
        this.description = description;
        this.dueDate = date;
    }

    public void assignUniqueId() {
        this.id = new ToDoManager.ToDoUniqueIdentifierGenerator().sessionId();
    }

    @Override
    public String toString() {
        return title;
    }
}
