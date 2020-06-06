package com.example;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 *
 * @author manfred.dohmen@gmail.com
 */
@Component( property= EventConstants.EVENT_TOPIC + "=some/topic")
public class MyEventListener implements EventHandler {

    public void handleEvent(Event event) {
        System.out.println(event.toString());
    }

}
