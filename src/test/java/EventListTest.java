import org.junit.jupiter.api.Test;
import sim.Event;
import sim.EventList;

import static org.junit.jupiter.api.Assertions.*;

public class EventListTest {

    // A simple event for testing
    static class TestEvent extends Event {
        public TestEvent(double t) { super(t); }
        @Override public void makeItHappen() {}
    }

    @Test
    void earliestEventComesOutFirst() {
        EventList list = new EventList();
        list.insert(new TestEvent(8.0));
        list.insert(new TestEvent(3.0));
        list.insert(new TestEvent(5.0));

        // The first event to happen should be the one at time 3
        assertEquals(3.0, list.takeNextEvent().getTime());
    }

    @Test
    void returnsNullWhenEmpty() {
        EventList list = new EventList();
        assertNull(list.takeNextEvent(), "Empty event list should return null");
    }
}
