package com.github.tachesimazzoca.examples.adk.workflow.flows;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.adk.agents.InvocationContext;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CustomFlowTest {
  @Test
  public void testRunWithErrors() {
    CustomFlow flow = new CustomFlow(context -> Single.error(new IllegalStateException()));
    InvocationContext context = Mockito.mock(InvocationContext.class);
    assertThrows(
        IllegalStateException.class,
        () -> {
          flow.run(context).blockingForEach(event -> {});
        });
  }
}
