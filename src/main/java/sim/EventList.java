package sim;

/**
 * EventList: the class for the event list.
 * (There is only one object of this class in the program.)
 */
public class EventList {

    /** ListItem: the class for objects stored in the event list. */
    private static class ListItem {
        public Event data;
        public ListItem next;
    }

    private ListItem firstEvent = null; // head of time-ordered list

    /** insert: add an event e to the event list in the appropriate place, prioritized by time. */
    public void insert(Event e) {
        ListItem item = new ListItem();
        item.data = e;

        final double time = e.getTime();
        if (firstEvent == null || time < firstEvent.data.getTime()) {
            item.next = firstEvent;
            firstEvent = item;
        } else {
            ListItem behind = firstEvent;
            ListItem ahead = firstEvent.next;
            while (ahead != null && ahead.data.getTime() <= time) {
                behind = ahead;
                ahead = ahead.next;
            }
            behind.next = item;
            item.next = ahead;
        }
    }

    /** takeNextEvent: remove the item at the head of the event list and return it. */
    public Event takeNextEvent() {
        if (firstEvent == null) return null;
        Event eventToReturn = firstEvent.data;
        firstEvent = firstEvent.next;
        return eventToReturn;
    }
}
