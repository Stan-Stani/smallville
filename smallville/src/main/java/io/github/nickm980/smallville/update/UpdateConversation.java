package io.github.nickm980.smallville.update;

import java.util.List;

import io.github.nickm980.smallville.World;
import io.github.nickm980.smallville.math.SentenceTokenizer;
import io.github.nickm980.smallville.models.Agent;
import io.github.nickm980.smallville.models.Conversation;
import io.github.nickm980.smallville.models.Dialog;

public class UpdateConversation extends AgentUpdate {

    private String observation;
    private static final SentenceTokenizer TOKENIZER = new SentenceTokenizer();

    public UpdateConversation(String observation) {
	this.observation = observation;
    }

    public UpdateConversation() {

    }

    @Override
    public boolean update(ChatService service, World world, Agent agent) {
	LOG.info("[Updater / Conversation] Checking for any conversations and initating dialog");

	if (observation == null) {
	    observation = agent.getCurrentActivity();
	}

	String subject = TOKENIZER.extractName(observation);

	if (agent.getFullName().equals(subject)) {
	    LOG.warn("[Updater / Conversation] Agent attempted to have a conversation with themself.");
	    return false;
	}

	if (subject == null) {
	    LOG.info("[Updater / Conversation] No conversation detected");
	    return true;
	}

	Agent other = world.getAgent(subject).orElse(null);

	if (other == null) {
	    LOG
		.error("[Updater / Conversation] A conversation was implied but the target {" + subject
			+ "} does not exist.");
	    return false;
	}

	Conversation conversation = service.getConversationIfExists(agent, other);

	List<String> memories = conversation.getDialog().stream().map(Dialog::asNaturalLanguage).toList();
	agent.getMemoryStream().addObservations(memories);
	other.getMemoryStream().addObservations(memories);

	world.save(conversation);

	return next(service, world, agent);
    }
}
