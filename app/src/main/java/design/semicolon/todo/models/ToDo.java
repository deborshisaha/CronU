package design.semicolon.todo.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import design.semicolon.todo.classes.AlarmNotificationReceiver;
import design.semicolon.todo.manager.ToDoManager;

/**
 * Created by dsaha on 1/10/16.
 */
@Table(name = "ToDos", id = "_id")
public class ToDo extends Model implements Serializable {

    @Column(name = "id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "done")
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

    public String getName() { return name; }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public void setName(String t) {
        this.name = t;
    }

    public void setDate (Date date) { this.dueDate = date; }

    public boolean markedAsDone() { return this.done; }

    public Date getDueDate() { return dueDate; }

    public String getDescription() {
        return description;
    }
    
    public String getUniqueId() { return id; }

    public ToDo( String title, String description, Date date) {
        this.name = title;
        this.description = description;
        this.dueDate = date;
    }

    public ToDo() {}

    public void assignUniqueId() {
        this.id = new ToDoManager.ToDoUniqueIdentifierGenerator().sessionId();
    }

    @Override
    public String toString() {
        return name;
    }

}
