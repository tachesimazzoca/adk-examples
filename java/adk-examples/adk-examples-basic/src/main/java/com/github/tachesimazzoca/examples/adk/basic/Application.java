package com.github.tachesimazzoca.examples.adk.basic;

import com.github.tachesimazzoca.examples.adk.basic.agents.EchoAgent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
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
      final String APP_NAME = "adk-examples-basic";
      final String USER_ID = "user-1";

      BaseAgent rootAgent = new EchoAgent(APP_NAME, "This is a custom agent.");
      InMemoryRunner runner = new InMemoryRunner(rootAgent);
      Session session = runner.sessionService().createSession(APP_NAME, USER_ID).blockingGet();

      try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
        while (true) {
          System.out.print("\nYou > ");
          String userInput = scanner.nextLine();

          if ("quit".equalsIgnoreCase(userInput)) {
            break;
          }

          Content userMsg = Content.fromParts(Part.fromText(userInput));
          Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

          System.out.print("\nAgent > ");
          events.blockingForEach(event -> System.out.println(event.stringifyContent()));
        }
      }
    };
  }
}
