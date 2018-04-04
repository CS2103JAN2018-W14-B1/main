# Sisyphus25
###### \java\seedu\address\commons\events\model\AppointmentListChangedEvent.java
``` java
/**
 * Indicates the appointment list has changed
 */
public class AppointmentListChangedEvent extends BaseEvent {
    public final ObservableList<Appointment> appointmentList;

    public AppointmentListChangedEvent(ObservableList<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\commons\events\ui\ThemeChangeEvent.java
``` java
/**
 * Indicates a request to change them
 */
public class ThemeChangeEvent extends BaseEvent {
    public final String theme;

    public ThemeChangeEvent(String theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\commons\events\ui\ToggleCalendarViewEvent.java
``` java
/**
 * Indicates a request to toggle Calendar view mode
 */
public class ToggleCalendarViewEvent extends BaseEvent {
    public final Character viewMode;

    public ToggleCalendarViewEvent(Character viewMode) {
        this.viewMode = viewMode;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\commons\events\ui\ToggleListEvent.java
``` java

import seedu.address.commons.events.BaseEvent;

/**
 * Indicates a request to toggle List
 */
public class ToggleListEvent extends BaseEvent {
    public final String list;

    public ToggleListEvent(String list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\address\logic\commands\ChangeThemeCommand.java
``` java
/**
 * Change theme of the GUI.
 */
public class ChangeThemeCommand extends Command {
    public static final String COMMAND_WORD = "theme";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Change the theme of TeachConnect.\n"
            + "Parameters: THEME\n"
            + "Example: " + COMMAND_WORD + " dark";

    public static final String MESSAGE_CHANGE_THEME_SUCCESS = "Theme changed";

    public static final String MESSAGE_INVALID_THEME = "Not a valid theme";

    private final String theme;

    public ChangeThemeCommand(String theme) {
        requireNonNull(theme);
        this.theme = theme;
    }

    @Override
    public CommandResult execute() throws CommandException {
        EventsCenter.getInstance().post(new ThemeChangeEvent(theme));
        return new CommandResult(MESSAGE_CHANGE_THEME_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ChangeThemeCommand // instanceof handles nulls
                && this.theme.equals(((ChangeThemeCommand) other).theme)); // state check
    }
}
```
###### \java\seedu\address\logic\commands\ListCommand.java
``` java
    @Override
    public CommandResult execute() throws CommandException {
        switch (type) {
        case TYPE_CONTACT:
            evokeToggleListEvent(TYPE_CONTACT);
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_CONTACT);

        case TYPE_STUDENT:
            evokeToggleListEvent(TYPE_CONTACT);
            model.updateFilteredPersonList(PREDICATE_SHOW_ONLY_STUDENTS);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_STUDENT);

        case TYPE_APPOINTMENT:
            evokeToggleListEvent(TYPE_APPOINTMENT);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_APPOINTMENT);

        case TYPE_TASK:
            evokeToggleListEvent(TYPE_TASK);
            return new CommandResult(MESSAGE_SUCCESS + TYPE_TASK);

        default:
            throw new CommandException(MESSAGE_INVALID_TYPE);
        }
    }

    private void evokeToggleListEvent(String type) {
        model.changeCurrentActiveListType(type);
        EventsCenter.getInstance().post(new ToggleListEvent(type));
    }

```
###### \java\seedu\address\logic\commands\RemoveCommand.java
``` java
/**
 * Remove an appointment or task identified using its last displayed index from the address book.
 */
public class RemoveCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "remove";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the event identified by the index number used in the last event listing.\n"
            + "Parameters: "
            + " EVENT_TYPE (could be appointment or task)"
            + "INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " appointment " + " 1";

    public static final String MESSAGE_DELETE_EVENT_SUCCESS = "Removed %1$s: %2$s";

    private final Index targetIndex;

    private String eventTypeOfDeletedTarget;

    private Object eventToBeDeleted;

    public RemoveCommand(Index targetIndex, String eventTypeOfDeletedTarget) {
        this.eventTypeOfDeletedTarget = eventTypeOfDeletedTarget;
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult executeUndoableCommand() {
        requireNonNull(eventToBeDeleted);
        try {
            if (eventTypeOfDeletedTarget.equals(LIST_TYPE_APPOINTMENT)) {
                model.deleteAppointment((Appointment) eventToBeDeleted);
            } else if (eventTypeOfDeletedTarget.equals(LIST_TYPE_TASK)) {
                model.deleteTask((Task) eventToBeDeleted);
            }
        } catch (EventNotFoundException ive) {
            throw new AssertionError(String.format("The target %s cannot be missing", eventTypeOfDeletedTarget));
        }
        return new CommandResult(
                String.format(MESSAGE_DELETE_EVENT_SUCCESS, eventTypeOfDeletedTarget, eventToBeDeleted));
    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        if (eventTypeOfDeletedTarget.equals(LIST_TYPE_APPOINTMENT)) {
            List<Appointment> lastShownList = model.getFilteredAppointmentList();
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_EVENT_DISPLAYED_INDEX);
            }
            eventToBeDeleted = lastShownList.get(targetIndex.getZeroBased());
        } else if (eventTypeOfDeletedTarget.equals(LIST_TYPE_TASK)) {
            List<Task> lastShownList = model.getFilteredTaskList();
            if (targetIndex.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_EVENT_DISPLAYED_INDEX);
            }
            eventToBeDeleted = lastShownList.get(targetIndex.getZeroBased());
        }

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof RemoveCommand // instanceof handles nulls
                && this.targetIndex.equals(((RemoveCommand) other).targetIndex) // state check
                && Objects.equals(this.eventToBeDeleted, ((RemoveCommand) other).eventToBeDeleted));
    }
}
```
###### \java\seedu\address\logic\commands\SetAppointmentCommand.java
``` java
/**
 * Adds an appointment with the person at {@code index} in the person list to the address book.
 */
public class SetAppointmentCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "set_appointment";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an appoinment to the address book.\n"
            + "Parameters: "
            + PREFIX_TITLE + "TITLE "
            + PREFIX_START_TIME + "START-DATE START-TIME "
            + PREFIX_END_TIME + "END-DATE END-TIME "
            + PREFIX_PERSON_TO_MEET_INDEX + "PERSON TO MEET\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TITLE + "Meet James "
            + PREFIX_START_TIME + "20/05/2018 10:00 "
            + PREFIX_END_TIME + "20/05/2018 12:00 "
            + PREFIX_PERSON_TO_MEET_INDEX + "3 ";

    public static final String MESSAGE_SUCCESS = "New appointment added: %1$s";
    public static final String MESSAGE_DUPLICATE_APPOINTMENT = "This appointment already exists in the address book";

    private final Appointment baseAppointmentWithoutPerson;
    private final Index index;

    private PersonToMeet personToMeet;

    /**
     * Creates a SetAppointmentCommand without any PersonToMeet
     */
    public SetAppointmentCommand(Appointment baseAppointmentWithoutPerson) {
        this(baseAppointmentWithoutPerson, null);
    }

    /**
     * Creates a SetAppointmentCommand to add the specified {@code Appointment}
     */
    public SetAppointmentCommand(Appointment baseAppointmentWithoutPerson, Index index) {
        requireNonNull(baseAppointmentWithoutPerson);
        this.baseAppointmentWithoutPerson = baseAppointmentWithoutPerson;
        this.index = index;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            Appointment toAdd;
            if (personToMeet != null) {
                toAdd = new Appointment(baseAppointmentWithoutPerson.getTitle(), baseAppointmentWithoutPerson.getTime(),
                        baseAppointmentWithoutPerson.getEndTime(), personToMeet);
            } else {
                toAdd = baseAppointmentWithoutPerson;
            }
            model.addAppointment(toAdd);
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (DuplicateEventException e) {
            throw new CommandException(MESSAGE_DUPLICATE_APPOINTMENT);
        }

    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        if (index != null) {
            List<Person> lastShownList = model.getFilteredPersonList();

            if (index.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }

            Person person = lastShownList.get(index.getZeroBased());
            personToMeet = new PersonToMeet(person.getName().fullName, person.getEmail().value);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SetAppointmentCommand // instanceof handles nulls
                && baseAppointmentWithoutPerson.equals(((SetAppointmentCommand) other).baseAppointmentWithoutPerson));
    }
}
```
###### \java\seedu\address\logic\commands\SetTaskCommand.java
``` java
/**
 * Adds a task to the address book.
 */
public class SetTaskCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "set_task";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a task to the address book.\n"
            + "Parameters: "
            + PREFIX_TITLE + "TITLE "
            + PREFIX_END_TIME + "DATE TIME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TITLE + "Mark papers "
            + PREFIX_END_TIME + "20/05/2018 12:00 ";

    public static final String MESSAGE_SUCCESS = "New task added: %1$s";
    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in the address book";

    private final Task toAdd;

    /**
     * Creates a SetTaskCommand to add the specified {@code Task}
     */
    public SetTaskCommand(Task task) {
        requireNonNull(task);
        toAdd = task;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            model.addTask(toAdd);
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (DuplicateEventException e) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        }

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SetTaskCommand // instanceof handles nulls
                && toAdd.equals(((SetTaskCommand) other).toAdd));
    }
}
```
###### \java\seedu\address\logic\commands\ToggleCalendarViewCommand.java
``` java
/**
 * Command to change calendar view
 */
public class ToggleCalendarViewCommand extends Command {

    public static final String COMMAND_WORD = "calendar";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Toggles calendar view. \n"
            + "Parameter: VIEW_MODE\n"
            + "View mode: Day view: d, Week view: w, Month view: m\n"
            + "Example: " + COMMAND_WORD + " d";

    public static final String MESSAGE_VIEW_TOGGLE_SUCCESS = "View changed.";

    private Character viewMode;

    public ToggleCalendarViewCommand(Character viewMode) {
        requireNonNull(viewMode);
        this.viewMode = viewMode;
    }
    @Override
    public CommandResult execute() throws CommandException {
        EventsCenter.getInstance().post(new ToggleCalendarViewEvent(viewMode));
        return new CommandResult(MESSAGE_VIEW_TOGGLE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ToggleCalendarViewCommand // instanceof handles nulls
                && this.viewMode == ((ToggleCalendarViewCommand) other).viewMode); // state check
    }
}
```
###### \java\seedu\address\logic\parser\ChangeThemeCommandParser.java
``` java
/**
 * Parses input arguments and creates a new ChangeThemeCommand object
 */
public class ChangeThemeCommandParser implements Parser<ChangeThemeCommand> {
    /**
     * Parses the given {@code viewMode} of arguments in the context of the ChangeThemeCommandParser
     * and returns an ChangeThemeCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ChangeThemeCommand parse(String args) throws ParseException {
        String theme = args.trim();
        if (!isValidTheme(theme)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ChangeThemeCommand.MESSAGE_INVALID_THEME));
        }
        return new ChangeThemeCommand(theme);
    }

    /**
     *
     * @param theme
     * @return whether if {@code theme} is a valid theme name
     */
    private boolean isValidTheme(String theme) {
        return !theme.isEmpty() && Arrays.asList(THEME_LIST).contains(theme);
    }
}
```
###### \java\seedu\address\logic\parser\ListCommandParser.java
``` java
/**
 * Parser for ListCommand
 */
public class ListCommandParser implements Parser<ListCommand> {
    /**
     * Parses the given {@code args} of arguments in the context of the ListCommandParser
     * and returns an ListCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ListCommand parse(String args) throws ParseException {
        String item = args.trim();
        if (!isValidItem(item)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ListCommand.MESSAGE_USAGE));
        }
        return new ListCommand(item);
    }

    /**
     * @param str
     * @return whether if the string is a valid view mode or not
     */
    private boolean isValidItem(String str) {
        switch (str) {
        case(ListCommand.TYPE_CONTACT):
        case(ListCommand.TYPE_STUDENT):
        case(ListCommand.TYPE_APPOINTMENT):
        case(ListCommand.TYPE_TASK):
            return true;
        default:
            return false;
        }
    }
}
```
###### \java\seedu\address\logic\parser\ParserUtil.java
``` java
    /**
     * Parses a {@code Optional<String> title} into an {@code Optional<Title>} if {@code title} is present.
     * See header comment of this class regarding the use of {@code Optional} parameters.
     */
    public static Optional<Title> parseTitle(Optional<String> title) throws IllegalValueException {
        requireNonNull(title);
        return title.isPresent() ? Optional.of(parseTitle(title.get())) : Optional.empty();
    }

    /**
     * Parses a {@code String title} into a {@code Title}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws IllegalValueException if the given {@code title} is invalid.
     */
    public static Title parseTitle(String title) throws IllegalValueException {
        requireNonNull(title);
        String trimmedTitle = title.trim();
        if (!Title.isValidTitle(trimmedTitle)) {
            throw new IllegalValueException(Title.MESSAGE_TITLE_CONSTRAINTS);
        }
        return new Title(trimmedTitle);
    }

    /**
     * Parses a {@code Optional<String> eventTime} into an {@code Optional<EventTime>} if {@code eventTime} is present.
     * See header comment of this class regarding the use of {@code Optional} parameters.
     */
    public static Optional<EventTime> parseEventTime(Optional<String> eventTime) throws IllegalArgumentException {
        requireNonNull(eventTime);
        return eventTime.isPresent() ? Optional.of(parseEventTime(eventTime.get())) : Optional.empty();
    }

    /**
     * Parses a {@code String eventTime} into a {@code EventTime}.
     * Leading and trailing whitespaces will be trimmed.
     */
    public static EventTime parseEventTime(String eventTime) throws IllegalArgumentException {
        requireNonNull(eventTime);
        String trimmedEventTime = eventTime.trim();
        return new EventTime(trimmedEventTime);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws IllegalValueException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws IllegalValueException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new IllegalValueException(Tag.MESSAGE_TAG_NAME_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws IllegalValueException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
```
###### \java\seedu\address\logic\parser\RemoveCommandParser.java
``` java
/**
 * Parses input arguments and creates a new RemoveCommand object
 */
public class RemoveCommandParser implements Parser<RemoveCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the RemoveCommand
     * and returns an RemoveCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public RemoveCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        String[] parameterGetterArray = trimmedArgs.split(" ");
        if (trimmedArgs.isEmpty() || parameterGetterArray.length != 2) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemoveCommand.MESSAGE_USAGE));
        }
        try {
            if (!isValidEventType(parameterGetterArray[0])) {
                throw new IllegalValueException("Invalid event type");
            }
            Index index = ParserUtil.parseIndex(parameterGetterArray[1]);
            return new RemoveCommand(index, parameterGetterArray[0]);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemoveCommand.MESSAGE_USAGE));
        }
    }

    private boolean isValidEventType(String type) {
        return type.equals("appointment") || type.equals("task");
    }
}



```
###### \java\seedu\address\logic\parser\ToggleCalendarViewParser.java
``` java
/**
 * Parser for ToggleCalendarViewCommand
 */
public class ToggleCalendarViewParser implements Parser<ToggleCalendarViewCommand> {
    /**
     * Parses the given {@code viewMode} of arguments in the context of the ToggleCalendarViewParser
     * and returns an ToggleCalendarViewCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ToggleCalendarViewCommand parse(String args) throws ParseException {
        String viewMode = args.trim();
        if (viewMode.isEmpty() || !isValidViewMode(viewMode)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ToggleCalendarViewCommand.MESSAGE_USAGE));
        }
        return new ToggleCalendarViewCommand(viewMode.charAt(0));
    }

    /**
     *
     * @param str
     * @return whether if the string is a valid view mode or not
     */
    private boolean isValidViewMode(String str) {
        if (str.length() != 1) {
            return false;
        }
        switch (str.charAt(0)) {
        case('w'):
        case('d'):
        case('m'):
            return true;
        default:
            return false;
        }
    }
}
```
###### \java\seedu\address\model\AddressBook.java
``` java
    //event operations
    /**
     * Adds an appointment to the address book.
     *
     * @throws DuplicateEventException if an equivalent appointment already exists.
     */
    public void addAppointment(Appointment e) throws DuplicateEventException {
        appointments.add(e);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * @throws EventNotFoundException if the {@code key} is not in this {@code AddressBook}.
     */
    public boolean removeAppointment(Appointment key) throws EventNotFoundException {
        if (appointments.remove(key)) {
            return true;
        } else {
            throw new EventNotFoundException();
        }
    }

    /**
     * Adds a task to the address book.
     *
     * @throws DuplicateEventException if an equivalent appointment already exists.
     */
    public void addTask(Task e) throws DuplicateEventException {
        tasks.add(e);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * @throws EventNotFoundException if the {@code key} is not in this {@code AddressBook}.
     */
    public boolean removeTask(Task key) throws EventNotFoundException  {
        if (tasks.remove(key)) {
            return true;
        } else {
            throw new EventNotFoundException();
        }
    }
}
```
###### \java\seedu\address\model\event\Appointment.java
``` java
/**
 * Represent an appointment in the schedule, contains time of the appointment as well as details and personMeet.
 */
public class Appointment {
    public static final String MESSAGE_TIME_PERIOD_CONSTRAINTS = "The end time should be after the start time";

    private final Title title;
    private final EventTime time;
    private final EventTime endTime;
    private final PersonToMeet personToMeet;

    //Every field must be present and not null
    public Appointment(Title title, EventTime startTime, EventTime endTime) {
        this(title, startTime, endTime, null);
    }

    //Every field except personToMeet must be present and not null
    public Appointment(Title title, EventTime startTime, EventTime endTime, PersonToMeet personToMeet) {
        requireAllNonNull(title, startTime, endTime);
        checkArgument(isValidTime(startTime, endTime), MESSAGE_TIME_PERIOD_CONSTRAINTS);
        this.title = title;
        this.time = startTime;
        this.endTime = endTime;
        this.personToMeet = personToMeet;
    }

    public Title getTitle() {
        return title;
    }

    public EventTime getTime() {
        return time;
    }

    public EventTime getEndTime() {
        return endTime;
    }

    public PersonToMeet getPersonToMeet() {
        return personToMeet;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Appointment)) {
            return false;
        }

        Appointment otherAppointment = (Appointment) other;
        return otherAppointment.getTitle().equals(this.getTitle())
                && otherAppointment.getTime().equals(this.getTime())
                && otherAppointment.getEndTime().equals(this.getEndTime());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getTitle())
                .append(", Start Time: ")
                .append(getTime().toString())
                .append(", End Time: ")
                .append(getEndTime().toString());
        if (personToMeet != null) {
            builder.append(", With: ")
                    .append(personToMeet.getName());
        }
        return builder.toString();
    }

    /**
     * Returns true if the given time is valid
     */
    public static boolean isValidTime(EventTime startTime, EventTime endTime) {
        return endTime.value.after(startTime.value);
    }
}
```
###### \java\seedu\address\model\event\Task.java
``` java
/**
 * Represent a Task in the schedule, contains deadline as well as the title
 */
public class Task {
    private Title title;
    private EventTime time;

    //Every field must be present and not null
    public Task(Title title, EventTime deadline) {
        requireAllNonNull(title, deadline);
        this.title = title;
        this.time = deadline;
    }

    public Title getTitle() {
        return title;
    }

    public EventTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Task)) {
            return false;
        }

        Task otherTask = (Task) other;
        return otherTask.getTitle().equals(this.getTitle())
                && otherTask.getTime().equals(this.getTime());
    }

    @Override
    public String toString() {
        return title + ", Deadline: " + time;
    }
}
```
###### \java\seedu\address\storage\XmlAdaptedAppointment.java
``` java
/**
 * JAXB-friendly version of the Person.
 */
public class XmlAdaptedAppointment {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Appointment's %s field is missing!";

    @XmlElement(required = true)
    private String title;
    @XmlElement(required = true)
    private String startTime;
    @XmlElement(required = true)
    private String endTime;
    @XmlElement(required = true)
    private String personToMeet;

    /**
     * Constructs an XmlAdaptedAppointment.
     * This is the no-arg constructor that is required by JAXB.
     */
    public XmlAdaptedAppointment() {}

    public XmlAdaptedAppointment(String title, String startTime, String endTime) {
        this(title, startTime, endTime, null);
    }

    /**
     * Constructs an {@code XmlAdaptedAppointment} with the given appointment details.
     */
    public XmlAdaptedAppointment(String title, String startTime, String endTime, String personToMeet) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        if (personToMeet != null) {
            this.personToMeet = personToMeet;
        }
    }

    /**
     * Converts a given Appointment into this class for JAXB use.
     *
     * @param source future changes to this will not affect the created XmlAdaptedAppointment
     */
    public XmlAdaptedAppointment(Appointment source) {
        title = source.getTitle().toString();
        startTime = source.getTime().toString();
        endTime = source.getEndTime().toString();
        if (source.getPersonToMeet() != null) {
            personToMeet = source.getPersonToMeet().toString();
        }
    }

    /**
     * Converts this jaxb-friendly adapted person object into the model's Appointment object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted appointment
     */
    public Appointment toModelType() throws IllegalValueException {
        if (this.title == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Title.class.getSimpleName()));
        }
        if (!Title.isValidTitle(this.title)) {
            throw new IllegalValueException(Title.MESSAGE_TITLE_CONSTRAINTS);
        }
        final Title title = new Title(this.title);

        if (this.startTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "Start Time"));
        }
        if (this.endTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "End Time"));
        }

        final EventTime startTime = new EventTime(this.startTime);
        final EventTime endTime = new EventTime(this.endTime);

        if (!Appointment.isValidTime(startTime, endTime)) {
            throw new IllegalValueException(Appointment.MESSAGE_TIME_PERIOD_CONSTRAINTS);
        }

        if (this.personToMeet != null) {
            String[] components = this.personToMeet.split(EMAIL_SPLITTER);
            PersonToMeet personToMeet = new PersonToMeet(components[0], components[1]);
            return new Appointment(title, startTime, endTime, personToMeet);
        }

        return new Appointment(title, startTime, endTime);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof XmlAdaptedAppointment)) {
            return false;
        }

        XmlAdaptedAppointment otherAppointment = (XmlAdaptedAppointment) other;
        return Objects.equals(title, otherAppointment.title)
                && Objects.equals(startTime, otherAppointment.startTime)
                && Objects.equals(endTime, otherAppointment.endTime)
                && Objects.equals(personToMeet, otherAppointment.personToMeet);
    }
}
```
###### \java\seedu\address\storage\XmlAdaptedTask.java
``` java
/**
 * JAXB-friendly version of the Person.
 */
public class XmlAdaptedTask {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Task's %s field is missing!";

    @XmlElement(required = true)
    private String title;
    @XmlElement(required = true)
    private String time;

    /**
     * Constructs an XmlAdaptedTask.
     * This is the no-arg constructor that is required by JAXB.
     */
    public XmlAdaptedTask() {}

    /**
     * Constructs an {@code XmlAdaptedTask} with the given task details.
     */
    public XmlAdaptedTask(String title, String time) {
        this.title = title;
        this.time = time;
    }

    /**
     * Converts a given Task into this class for JAXB use.
     *
     * @param source future changes to this will not affect the created XmlAdaptedTask
     */
    public XmlAdaptedTask(Task source) {
        title = source.getTitle().toString();
        time = source.getTime().toString();
    }

    /**
     * Converts this jaxb-friendly adapted person object into the model's Task object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted task
     */
    public Task toModelType() throws IllegalValueException {
        if (this.title == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Title.class.getSimpleName()));
        }
        if (!Title.isValidTitle(this.title)) {
            throw new IllegalValueException(Title.MESSAGE_TITLE_CONSTRAINTS);
        }
        final Title title = new Title(this.title);

        if (this.time == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "Time"));
        }
        final EventTime time = new EventTime(this.time);

        return new Task(title, time);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof XmlAdaptedTask)) {
            return false;
        }

        XmlAdaptedTask otherTask = (XmlAdaptedTask) other;
        return Objects.equals(title, otherTask.title)
                && Objects.equals(time, otherTask.time);
    }
}
```
###### \java\seedu\address\ui\AppointmentCard.java
``` java
/**
 * An UI component that displays information of a {@code Appointment}.
 */
public class AppointmentCard extends UiPart<Region> {

    private static final String FXML = "AppointmentListCard.fxml";
    private static final String DATE_FORMAT = "EEE, MMMMM dd, HH:mm a";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    public final Appointment appointment;

    @FXML
    private HBox cardPane;
    @FXML
    private Label title;
    @FXML
    private Label id;
    @FXML
    private Label time;
    @FXML
    private Label endTime;
    @FXML
    private Label personToMeet;

    public AppointmentCard(Appointment appointment, int displayedIndex) {
        super(FXML);
        this.appointment = appointment;
        id.setText(displayedIndex + ". ");
        title.setText(appointment.getTitle().value);
        time.setText("From: " + DATE_FORMATTER.format(appointment.getTime().value.getTime()));
        endTime.setText("To: " + DATE_FORMATTER.format(appointment.getEndTime().value.getTime()));
        if (appointment.getPersonToMeet() != null) {
            personToMeet.setText("With " + appointment.getPersonToMeet().getName());
        } else {
            personToMeet.setText("");
        }
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AppointmentCard)) {
            return false;
        }

        // state check
        AppointmentCard card = (AppointmentCard) other;
        return id.getText().equals(card.id.getText())
                && appointment.equals(card.appointment);
    }
}
```
###### \java\seedu\address\ui\CalendarPanel.java
``` java
/**
 * The Calendar Panel of the App.
 */
public class CalendarPanel extends UiPart<Region> {
    private static final String FXML = "CalendarPanel.fxml";

    @FXML
    private CalendarView calendarView;
    private Calendar calendar;

    private ObservableList<Appointment> appointmentList;

    public CalendarPanel(ObservableList<Appointment> appointmentObservableList) {
        super(FXML);
        this.appointmentList = appointmentObservableList;

        calendarView = new CalendarView();
        CalendarSource calendarSource = new CalendarSource("My Calendar");
        calendar = new Calendar("Appointments");

        calendarView.setRequestedTime(LocalTime.now());
        calendarView.setToday(LocalDate.now());
        calendarView.setTime(LocalTime.now());

        calendarView.getCalendarSources().add(calendarSource);
        calendarSource.getCalendars().add(calendar);
        calendar.setStyle(Calendar.Style.getStyle(0));
        calendar.setLookAheadDuration(Duration.ofDays(365));

        updateCalendar();
        disableViews();
        registerAsAnEventHandler(this);
    }

    /**
     * Clear the entry list in the CalendarFX calendar and
     * populate it with appointment in the updated appointmentList
     */
    private void updateCalendar() {
        calendar.clear();
        ArrayList<Entry> entries = getEntries();
        for (Entry entry : entries) {
            calendar.addEntry(entry);
        }
    }

    private ArrayList<Entry> getEntries() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (Appointment appointment : appointmentList) {
            entries.add(getEntry(appointment));
        }
        return entries;
    }

    private Entry getEntry(Appointment appointment) {
        LocalDateTime ldtstart = LocalDateTime.ofInstant(
                appointment.getTime().value.getTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime ldtend = LocalDateTime.ofInstant(
                appointment.getEndTime().value.getTime().toInstant(), ZoneId.systemDefault());
        String description = appointment.getTitle().value;
        return new Entry(description, new Interval(ldtstart, ldtend));
    }

    @Subscribe
    private void handleAppointmentListChangedEvent(AppointmentListChangedEvent event) {
        appointmentList = event.appointmentList;
        Platform.runLater(
                this::updateCalendar
        );
    }


```
###### \java\seedu\address\ui\MainWindow.java
``` java
    private void setTheme() {
        setTheme(DEFAULT_THEME);
    }

    private void setTheme(String theme) {
        primaryStage.getScene().getStylesheets().add(EXTENSIONS_STYLESHEET);
        primaryStage.getScene().getStylesheets().add(TAG_COLOUR_STYLESHEET);
        primaryStage.getScene().getStylesheets().add(THEME_LIST.getThemeStyleSheet(theme));
    }

    @Subscribe
    private void handleThemeChangeEvent(ThemeChangeEvent event) {
        theme = event.theme;
        Platform.runLater(
                this::changeTheme
        );
    }

    private void changeTheme() {
        primaryStage.getScene().getStylesheets().clear();
        setTheme(theme);
    }
```
###### \java\seedu\address\ui\TaskCard.java
``` java
/**
 * An UI component that displays information of a {@code Task}.
 */
public class TaskCard extends UiPart<Region> {

    private static final String FXML = "TaskListCard.fxml";
    private static final String DATE_FORMAT = "EEE, MMMMM dd, HH:mm a";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
    private static final Calendar CALENDAR = Calendar.getInstance();

    public final Task task;

    @FXML
    private HBox cardPane;
    @FXML
    private Label title;
    @FXML
    private Label id;
    @FXML
    private Label time;
    @FXML
    private FlowPane tags;

    public TaskCard(Task task, int displayedIndex) {
        super(FXML);
        this.task = task;
        id.setText(displayedIndex + ". ");
        title.setText(task.getTitle().value);
        time.setText("Finish before: " + DATE_FORMATTER.format(task.getTime().value.getTime()));
        if (task.getTime().isExpired()) {
            addExpiredTag();
        }
    }

    /**
     * Add an expired tag to the Task Card
     */
    private void addExpiredTag() {
        Label expiredTask = new Label("Expired");
        expiredTask.getStyleClass().add("red");
        tags.getChildren().add(expiredTask);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskCard)) {
            return false;
        }

        // state check
        TaskCard card = (TaskCard) other;
        return id.getText().equals(card.id.getText())
                && task.equals(card.task);
    }
}
```
###### \java\seedu\address\ui\ThemeList.java
``` java
/**
 * Provide list of themes and respective URL to their CSS stylesheet
 */
public class ThemeList {
    private HashMap<String, String> themeList;

    public ThemeList() {
        themeList = new HashMap<>();
        themeList.put("dark", "view/DarkTheme.css");
        themeList.put("light", "view/LightTheme.css");
        themeList.put("doge", "view/DogeTheme.css");
        themeList.put("galaxy", "view/GalaxyTheme.css");
    }

    public String getThemeStyleSheet(String theme) {
        if (!themeList.containsKey(theme)) {
            return themeList.get("light");
        }
        return themeList.get(theme);
    }
}
```