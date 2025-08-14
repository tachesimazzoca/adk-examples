package com.github.tachesimazzoca.examples.adk.workflow.agets;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAgent extends BaseAgent {

  private final Function<InvocationContext, Flowable<Event>> func;

  public CustomAgent(String name) {
    this(name, "");
  }

  public CustomAgent(String name, String description) {
    this(name, description, defaultFunction(), null, null);
  }

  public CustomAgent(
      String name, String description, Function<InvocationContext, Flowable<Event>> func) {
    this(name, description, func, null, null);
  }

  public CustomAgent(
      String name,
      String description,
      Function<InvocationContext, Flowable<Event>> func,
      List<Callbacks.BeforeAgentCallback> beforeAgentCallback,
      List<Callbacks.AfterAgentCallback> afterAgentCallback) {
    super(name, description, null, beforeAgentCallback, afterAgentCallback);
    this.func = func;
  }

  public static Function<InvocationContext, Flowable<Event>> defaultFunction() {
    return context -> {
      return Flowable.empty();
    };
  }

  @Override
  protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
    BaseAgent agent = invocationContext.agent();
    log.info("I am {} as a sub agent of {}.", agent.name(), agent.parentAgent());
    return func.apply(invocationContext);
  }

  @Override
  protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
    return Flowable.error(new UnsupportedOperationException("runLive is not defined yet."));
  }
}
