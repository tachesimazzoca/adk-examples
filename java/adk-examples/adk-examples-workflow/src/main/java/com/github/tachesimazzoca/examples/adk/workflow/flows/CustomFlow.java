package com.github.tachesimazzoca.examples.adk.workflow.flows;

import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.adk.flows.BaseFlow;
import com.google.common.collect.Iterables;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFlow implements BaseFlow {

  private static final Logger logger = LoggerFactory.getLogger(CustomFlow.class);

  private final CustomFlowProcessor processor;

  public CustomFlow(CustomFlowProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Flowable<Event> run(InvocationContext invocationContext) {
    Flowable<Event> currentStepEvents = runOneStep(invocationContext).cache();

    return currentStepEvents.concatWith(
        currentStepEvents
            .toList()
            .flatMapPublisher(
                eventList -> {
                  if (eventList.isEmpty()
                      || Iterables.getLast(eventList).finalResponse()
                      || Iterables.getLast(eventList).actions().endInvocation().orElse(false)) {
                    logger.debug(
                        "Ending flow execution based on final response, endInvocation action or"
                            + " empty event list.");
                    return Flowable.empty();
                  } else {
                    logger.debug("Continuing to next step of the flow.");
                    return Flowable.defer(() -> run(invocationContext));
                  }
                }));
  }

  private Flowable<Event> runOneStep(InvocationContext context) {
    return processor.process(context).toFlowable();
  }
}
