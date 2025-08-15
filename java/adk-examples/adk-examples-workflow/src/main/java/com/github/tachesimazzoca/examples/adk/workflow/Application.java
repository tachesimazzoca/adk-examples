package com.github.tachesimazzoca.examples.adk.workflow;

import com.github.tachesimazzoca.examples.adk.workflow.agets.CustomAgent;
import com.github.tachesimazzoca.examples.adk.workflow.flows.CustomFlowProcessor;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.ParallelAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
  }

  @Bean
  public ApplicationRunner applicationRunner() {
    return args -> {
      final String APP_NAME = "adk-examples-workflow";
      final String USER_ID = "user-1";

      CustomFlowProcessor researchProcessor =
          context -> {
            try {
              // Simulate some research work
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              return Single.error(e);
            }
            String agentName = context.agent().name();
            String branchName = context.branch().orElse("none");
            String message =
                String.format(
                    "This is a research result from %s at the branch %s.", agentName, branchName);
            Event event =
                Event.builder()
                    .author(agentName)
                    .id(Event.generateEventId())
                    .content(Content.fromParts(Part.fromText(message)))
                    .build();

            return Single.just(event);
          };
      CustomAgent researcher1 = new CustomAgent("researcher-1", "", researchProcessor);
      CustomAgent researcher2 = new CustomAgent("researcher-2", "", researchProcessor);
      CustomAgent researcher3 = new CustomAgent("researcher-3", "", researchProcessor);
      ParallelAgent researchStage =
          ParallelAgent.builder()
              .name("research-stage")
              .subAgents(researcher1, researcher2, researcher3)
              .build();

      CustomAgent composer = new CustomAgent("composer");

      CustomAgent reviewer = new CustomAgent("reviewer");
      CustomAgent refiner = new CustomAgent("refiner");
      LoopAgent reviewStage =
          LoopAgent.builder()
              .name("review-stage")
              .subAgents(reviewer, refiner)
              .maxIterations(3)
              .build();

      SequentialAgent rootAgent =
          SequentialAgent.builder()
              .name(APP_NAME)
              .subAgents(researchStage, composer, reviewStage)
              .build();

      InMemoryRunner runner = new InMemoryRunner(rootAgent);
      Session session = runner.sessionService().createSession(APP_NAME, USER_ID).blockingGet();

      // Run the agent
      Content prompt = Content.fromParts(Part.fromText(""));
      Flowable<Event> eventStream = runner.runAsync(USER_ID, session.id(), prompt);

      // Stream event response
      eventStream.blockingForEach(
          event -> {
            if (event.finalResponse()) {
              System.out.println(event.stringifyContent());
            }
          });
    };
  }
}
