package com.github.tachesimazzoca.examples.adk.workflow.flows;

import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Single;

public interface CustomFlowProcessor {
    /**
     * Processes the given invocation context and returns a single event.
     *
     * @param context the invocation context to process
     * @return a single event resulting from the processing
     */
    Single<Event> process(InvocationContext context);
}
