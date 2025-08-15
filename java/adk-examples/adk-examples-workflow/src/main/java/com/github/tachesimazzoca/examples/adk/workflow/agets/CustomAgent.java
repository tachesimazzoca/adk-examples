package com.github.tachesimazzoca.examples.adk.workflow.agets;

import com.github.tachesimazzoca.examples.adk.workflow.flows.CustomFlow;
import com.github.tachesimazzoca.examples.adk.workflow.flows.CustomFlowProcessor;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAgent extends BaseAgent {

  private final CustomFlowProcessor processor;

  public CustomAgent(String name) {
    this(name, "");
  }

  public CustomAgent(String name, String description) {
    this(name, description, defaultProcessor(), null, null);
  }

  public CustomAgent(String name, String description, CustomFlowProcessor processor) {
    this(name, description, processor, null, null);
  }

  public CustomAgent(
      String name,
      String description,
      CustomFlowProcessor processor,
      List<Callbacks.BeforeAgentCallback> beforeAgentCallback,
      List<Callbacks.AfterAgentCallback> afterAgentCallback) {
    super(name, description, null, beforeAgentCallback, afterAgentCallback);
    this.processor = processor;
  }

  public static CustomFlowProcessor defaultProcessor() {
    return context -> {
      String agentName = context.agent().name();
      String message = String.format("Nothing to do for %s", agentName);
      Event event =
          Event.builder()
              .author(agentName)
              .id(Event.generateEventId())
              .content(Content.fromParts(Part.fromText(message)))
              .build();
      return Single.just(event);
    };
  }

  @Override
  protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
    BaseAgent agent = invocationContext.agent();
    log.info("I am {} as a sub agent of {}.", agent.name(), agent.parentAgent());
    CustomFlow flow = new CustomFlow(processor);
    return flow.run(invocationContext);
  }

  @Override
  protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
    return Flowable.error(new UnsupportedOperationException("runLive is not defined yet."));
  }
}
