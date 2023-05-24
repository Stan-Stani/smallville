package io.github.nickm980.smallville.update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import io.github.nickm980.smallville.Smallville;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.nickm980.smallville.World;
import io.github.nickm980.smallville.config.Config;
import io.github.nickm980.smallville.llm.LLM;
import io.github.nickm980.smallville.models.Agent;
import io.github.nickm980.smallville.models.Conversation;
import io.github.nickm980.smallville.models.Dialog;
import io.github.nickm980.smallville.models.memory.Memory;
import io.github.nickm980.smallville.models.memory.Observation;
import io.github.nickm980.smallville.models.memory.Plan;
import io.github.nickm980.smallville.nlp.LocalNLP;
import io.github.nickm980.smallville.nlp.NLPCoreUtils;
import io.github.nickm980.smallville.prompts.Prompt;
import io.github.nickm980.smallville.prompts.PromptBuilder;
import io.github.nickm980.smallville.prompts.response.CurrentActivity;
import io.github.nickm980.smallville.prompts.response.ObjectChangeResponse;
import io.github.nickm980.smallville.prompts.response.Reaction;

public class ChatService implements IChatService {

    private final LLM chat;
    private final static Logger LOG = LoggerFactory.getLogger(UpdateService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final World world;

    public ChatService(World world, LLM chat) {
	this.chat = chat;
	this.world = world;
    }
    
    @Override
    public int[] getWeights(Agent agent) {
	Prompt prompt = new PromptBuilder()
	    .withAgent(agent)
	    .withPrompt(Config.getPrompts().getCreateMemoryRankPrompt())
	    .createMemoryRankPrompt()
	    .build();

	String response = chat.sendChat(prompt, .1);

	ObjectMapper objectMapper = new ObjectMapper();
	int[] result = new int[0];

	if (!response.contains("[")) {
	    result = new int[1];
	    result[0] = Integer.parseInt(response);
	    return result;
	}

	try {
	    result = objectMapper.readValue(response, int[].class);
	} catch (JsonProcessingException e) {
	    LOG.error("Failed to parse json for memory ranking. Continuing anyways...");
	}

	return result;
    }

    @Override
    public Reaction getReaction(Agent agent, String observation) {
	Prompt prompt = new PromptBuilder()
	    .withAgent(agent)
	    .withLocations(world.getLocations())
	    .withPrompt(Config.getPrompts().getCreateReactionSuggestion())
	    .createReactionSuggestion(observation)
	    .build();

	String response = chat.sendChat(prompt, 1);
	ObjectMapper mapper = new ObjectMapper();
	Reaction reaction = new Reaction();

	try {
	    JsonNode json = mapper.readTree(response);
	    boolean willReact = json.get("react").asBoolean();

	    if (willReact) {
		String currentActivity = json.get("reaction").asText();
		String emoji = json.get("emoji").asText();

		reaction.setEmoji(emoji);
		reaction.setCurrentActivity(currentActivity);
	    }

	    reaction.setReact(willReact);
	} catch (JsonProcessingException e) {
	    LOG.error("Failed to parse json for memory ranking. Continuing anyways...");
	}

	return reaction;
    }

    @Override
    public String ask(Agent agent, String question) {
	Prompt prompt = new PromptBuilder()
	    .withAgent(agent)
	    .withLocations(world.getLocations())
	    .withPrompt(Config.getPrompts().getCreateAskQuestionPrompt())
	    .createAskQuestionPrompt(question)
	    .build();

	return chat.sendChat(prompt, .9);
    }

    @Override
    public List<Plan> getPlans(Agent agent) {
	Prompt prompt = new PromptBuilder()
	    .withLocations(world.getLocations())
	    .withAgent(agent)
	    .withPrompt(Config.getPrompts().getCreateFuturePlansPrompt())
	    .build();

	String response = chat.sendChat(prompt, .4);

	return parsePlans(response);
    }

    @Override
    public List<Plan> getShortTermPlans(Agent agent) {
	Prompt prompt = new PromptBuilder()
	    .withLocations(world.getLocations())
	    .withAgent(agent)
	    .withPrompt(Config.getPrompts().getCreateShortTermPlans())
	    .build();

	String response = chat.sendChat(prompt, .7);

	return parsePlans(response);
    }

    @Override
    public CurrentActivity getCurrentPlan(Agent agent) {
	CurrentActivity result = new CurrentActivity();
	Prompt prompt = new PromptBuilder()
	    .withAgent(agent)
	    .withLocations(world.getLocations())
	    .withPrompt(Config.getPrompts().getCreateCurrentPlanPrompt())
	    .build();
	NLPCoreUtils nlp = new LocalNLP();
	
	String response = chat.sendChat(prompt, .7);// higher value provides better results for emojis
	response = response.substring(response.indexOf("{"));

	JsonNode json = null;

	try {
	    json = objectMapper.readTree(response);
	} catch (JsonProcessingException e) {
	    LOG.error("Returning empty current plan because could not parse the result");
	    return result;
	}

	result.setCurrentActivity(json.get("activity").asText());
	result.setEmoji(json.get("emoji").asText());
	result.setLastActivity(nlp.convertToPastTense(agent.getCurrentActivity()));
	result.setLocation(json.get("location").asText());

	LOG.info("[Activity]" + result.getCurrentActivity() + " location: " + agent.getLocation().getName());

	return result;
    }

    @Override
    public Conversation getConversationIfExists(Agent agent, Agent other) {
	Prompt prompt = new PromptBuilder()
	    .withAgent(agent)
	    .withPrompt(Config.getPrompts().getCreateConversationWith())
	    .createConversationWith(other)
	    .build();

	String response = chat.sendChat(prompt, .7);
	String[] lines = response.split("\\r?\\n");

	List<Dialog> dialogs = new ArrayList<>();
	for (String line : lines) {
	    String[] parts = line.split(":\\s+", 2);
	    if (parts.length == 2) { // ignores all lines before the conversation
		dialogs.add(new Dialog(parts[0], parts[1]));
	    }
	}

	Conversation conversation = new Conversation(agent.getFullName(), other.getFullName(), dialogs);
	return conversation;
    }

    @Override
    public List<Plan> parsePlans(String input) {
	List<Plan> plans = new ArrayList<>();

	String[] lines = input.split("\n");

	for (String line : lines) {
	    LocalDateTime start = null;

	    try {
		start = parseTime(input, line);
	    } catch (Exception e) {
		LOG.error("Could not parse time");
		continue;
	    }

	    if (start == null) {
		continue;
	    }

	    Plan plan = new Plan(line, start);
	    plans.add(plan);
	}

	return plans;
    }

    private LocalDateTime parseTime(String input, String line) throws DateTimeParseException {
	String[] splitPlan = line.split("\\d+", 2); // split after first number

	if (line.isBlank()) {
	    return null;
	}

	if (splitPlan.length == 1) {
	    LOG.warn("Temporal memory possibly missing a time. " + line);
	    return null;
	}

	int index = input.indexOf(splitPlan[1]) - 2;

	if (index == -1) {
	    LOG.warn("Temporal memory possibly missing a time. " + line);
	    return null;
	}

	String time = input.substring(index, index + 8).trim().replace("(", "");
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
	return LocalDateTime.of(Smallville.getServer().getSimulationService().getTimekeeper().getSimulationTime().toLocalDate(), LocalTime.parse(time, formatter));
    }

    @Override
    public ObjectChangeResponse[] getObjectsChangedBy(Agent agent) {
	Prompt tensesPrompt = new PromptBuilder()
	    .withAgent(agent)
	    .withPrompt(Config.getPrompts().getCreatePastAndPresent())
	    .build();

	String tenses = chat.sendChat(tensesPrompt, .1);

	Prompt changedPrompt = new PromptBuilder()
	    .withAgent(agent)
	    .withLocations(world.getLocations())
	    .withPrompt(Config.getPrompts().getCreateObjectUpdates())
	    .createObjectUpdates(tenses)
	    .build();

	String response = chat.sendChat(changedPrompt, .3);

	String[] lines = response.split("\n");
	ObjectChangeResponse[] objects = new ObjectChangeResponse[lines.length];

	for (int i = 0; i < lines.length; i++) {
	    String line = lines[i];
	    String[] parts = line.split(":");
	    String item = parts[0].trim();
	    String value = parts[1].trim();
	    LOG.debug("Trying to change " + item + " to " + value);

	    if (item != null && value != null && !value.equalsIgnoreCase("Unchanged")) {
		objects[i] = new ObjectChangeResponse(item, value);
	    }
	}

	if (objects.length == 0) {
	    LOG.warn("No objects were updated");
	}

	return objects;
    }

    @Override
    public String getExactLocation(Agent agent) {
	Prompt prompt = new PromptBuilder().withAgent(agent).withPrompt(Config.getPrompts().getPickLocation()).build();
	return chat.sendChat(prompt, 0);
    }

    @Override
    public List<Memory> convertFuturePlansToMemories(List<Plan> plans) {
	if (plans.isEmpty()) {
	    return new ArrayList<Memory>();
	}
	LocalNLP nlp = new LocalNLP();
	
	List<Memory> result = new ArrayList<Memory>();

	for (Plan plan : plans) {
	    String pastTense = nlp.convertToPastTense(plan.getDescription());
	    
	    result.add(new Observation(pastTense, plan.getTime(), (int) plan.getImportance()));
	}

	return result;
    }
}
