package com.github.tachesimazzoca.examples.adk.llm.tools;

import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.ToolContext;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ConversationTools provides tools for managing conversations in an iterative process. */
public class ConversationTools {

  private static final Logger logger = LoggerFactory.getLogger(ConversationTools.class);

  @Schema(
      description =
          "Call this function ONLY when the facilitator concludes the conversation,"
              + " signaling the iterative process should end.")
  public static Map<String, Object> exitLoop(
      @Schema(name = "toolContext") ToolContext toolContext) {
    logger.info("ConversationTools.exitLoop triggered by {}", toolContext.agentName());
    toolContext.actions().setEscalate(true);
    return Map.of();
  }
}
