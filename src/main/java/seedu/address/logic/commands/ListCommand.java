package seedu.address.logic.commands;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.model.Model.PREDICATE_SHOW_ONLY_STUDENTS;

import seedu.address.logic.commands.exceptions.CommandException;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.ui.ToggleListEvent;
import seedu.address.logic.commands.exceptions.CommandException;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all ";
    private static final String MESSAGE_INVALID_TYPE = "TYPE is missing or invalid";

    public static final String TYPE_STUDENT = "students";
    public static final String TYPE_CONTACT = "contacts";
    public static final String TYPE_APPOINTMENT = "appointments";
    public static final String TYPE_TASK = "tasks";
  
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists selected type. \n"
            + "Parameter: TYPE\n"
            + "Example: " + COMMAND_WORD + " appointment";

    private final String type;
  
    public ListCommand(String type) {
        this.type = type;
    }

    @Override
    public CommandResult execute() throws CommandException {
        switch (type) {
        case TYPE_CONTACT:
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_CONTACT);

        case TYPE_STUDENT:
            model.updateFilteredPersonList(PREDICATE_SHOW_ONLY_STUDENTS);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_STUDENT);

        case TYPE_APPOINTMENT:
            model.changeCurrentActiveListType(type);
            EventsCenter.getInstance().post(new ToggleListEvent(type));
            return new CommandResult(MESSAGE_SUCCESS + TYPE_APPOINTMENT);
            
        case TYPE_TASK:
            model.changeCurrentActiveListType(type);
            EventsCenter.getInstance().post(new ToggleListEvent(type));
            return new CommandResult(MESSAGE_SUCCESS + TYPE_TASK);
        
        default:
            throw new CommandException(MESSAGE_INVALID_TYPE);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ListCommand // instanceof handles nulls
                && this.item.equals(((ListCommand) other).item)); // state check
    }
}
