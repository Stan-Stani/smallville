package io.github.nickm980.smallville.prompts;

import java.util.ArrayList;
import java.util.List;

import io.github.nickm980.smallville.models.Agent;
import io.github.nickm980.smallville.models.Conversation;
import io.github.nickm980.smallville.models.SimulatedLocation;

public class PromptData {

    private Agent agent;
    private List<SimulatedLocation> locations;
    private Conversation conversation;
    private String question;
    private TimePhrase phrase;
    
    public PromptData() {
	this.locations = new ArrayList<SimulatedLocation>();
	this.locations = List.of();
	this.question = "";
	this.phrase = TimePhrase.DAY;
    }

    public Agent getAgent() {
	return agent;
    }

    public void setAgent(Agent agent) {
	this.agent = agent;
    }

    public List<SimulatedLocation> getLocations() {
	return locations;
    }

    public void setLocations(List<SimulatedLocation> locations) {
	this.locations = locations;
    }

    public void setConversation(Conversation conversation) {
	this.conversation = conversation;
    }

    public Conversation getConversation() {
	return conversation;
    }

    public void setQuestion(String question) {
	this.question = question;
    }

    public String getQuestion() {
	return question;
    }

    public void setTimePhrase(TimePhrase phrase) {
	this.phrase = phrase;
    }
    
    public TimePhrase getTimePhrase() {
	return phrase;
    }

}