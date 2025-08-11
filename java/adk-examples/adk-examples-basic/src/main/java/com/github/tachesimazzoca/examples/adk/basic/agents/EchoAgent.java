package com.github.tachesimazzoca.examples.adk.basic.agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.adk.events.EventActions;
import com.google.genai.JsonSerializable;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoAgent extends BaseAgent {

  public EchoAgent(String name, String description) {
    this(name, description, null, null, null);
  }

  public EchoAgent(
      String name,
      String description,
      List<? extends BaseAgent> subAgents,
      List<Callbacks.BeforeAgentCallback> beforeAgentCallback,
      List<Callbacks.AfterAgentCallback> afterAgentCallback) {
    super(name, description, subAgents, beforeAgentCallback, afterAgentCallback);
  }

  @Override
  protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
    log.info("appName: {}", invocationContext.appName());
    log.info("session: {}", invocationContext.session().toJson());
    log.info(
        "userContent: {}",
        invocationContext.userContent().map(JsonSerializable::toJson).orElse(""));
    // Pick up the text from userContent.
    Optional<String> givenText =
        invocationContext
            .userContent()
            .flatMap(Content::parts)
            .flatMap(
                parts -> {
                  if (parts.isEmpty()) {
                    return Optional.empty();
                  } else {
                    return parts.getFirst().text();
                  }
                });
    String text = givenText.map(s -> String.format("You said, '%s'.", s)).orElse("");
    Event event =
        Event.builder()
            .actions(EventActions.builder().build())
            .author("agent")
            .id(Event.generateEventId())
            .content(Content.fromParts(Part.fromText(text)))
            .build();
    return Flowable.just(event);
  }

  @Override
  protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
    return Flowable.error(new UnsupportedOperationException("runLive is not defined yet."));
  }
}
