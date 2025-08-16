package com.github.tachesimazzoca.examples.adk.llm;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
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
      final String APP_NAME = "adk-examples-llm";
      final String USER_ID = "user-1";
      final String OUTPUT_CONVERSATION = "output_conversation";

      LlmAgent organizer =
          LlmAgent.builder()
              .name("owner")
              .model("gemini-2.5-flash")
              .instruction(
                  """
                   Please give a topic related to the idea given by the user.

                   Rules:
                   1. The format is `Title: <A Capitalized Title>`.
                   1. You can add a brief description after the title.
                   """)
              .outputKey(OUTPUT_CONVERSATION)
              .build();

      final String instruction =
          """
          Please add a line as your opinion to the following conversation text:
          ```
          {{output_conversation}}
          ```

          Rules:
          1. The format is `name > message`.
          2. If your name is `facilitator`, please add a line as your advice only if you
          think it would be better for the other participants to focus on the goal.
          """;

      LlmAgent facilitator =
          LlmAgent.builder()
              .name("facilitator")
              .model("gemini-2.5-flash")
              .instruction(instruction)
              .outputKey(OUTPUT_CONVERSATION)
              .build();

      LlmAgent alice =
          LlmAgent.builder()
              .name("alice")
              .model("gemini-2.5-flash")
              .instruction(instruction)
              .outputKey(OUTPUT_CONVERSATION)
              .build();

      LlmAgent bob =
          LlmAgent.builder()
              .name("bob")
              .model("gemini-2.5-flash")
              .instruction(instruction)
              .outputKey(OUTPUT_CONVERSATION)
              .build();

      LoopAgent conversationLoop =
          LoopAgent.builder()
              .name(APP_NAME)
              .subAgents(facilitator, alice, bob)
              .maxIterations(3)
              .build();

      SequentialAgent rootAgent =
          SequentialAgent.builder().name(APP_NAME).subAgents(organizer, conversationLoop).build();

      InMemoryRunner runner = new InMemoryRunner(rootAgent);
      Session session = runner.sessionService().createSession(APP_NAME, USER_ID).blockingGet();

      // Run the agent
      Content prompt =
          Content.fromParts(
              Part.fromText(
                  "Let:s talk about DDD (Domain-Driven Design) in software development."));
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
