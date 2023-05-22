package io.github.nickm980.smallville.update;

import io.github.nickm980.smallville.World;
import io.github.nickm980.smallville.models.Agent;
import io.github.nickm980.smallville.prompts.response.ObjectChangeResponse;

public class UpdateLocations extends AgentUpdate {

    @Override
    public boolean update(ChatService converter, World world, Agent agent) {
	LOG.info("[Locations] Updating location states");

	ObjectChangeResponse[] objects = converter.getObjectsChangedBy(agent);

	if (objects.length > 0) {
	    for (ObjectChangeResponse response : objects) {
		if (response != null) {
		    world.changeObject(response.getObject(), response.getState());
		}
	    }
	}

	LOG.info("[Locations] Location states updated");
	return next(converter, world, agent);
    }
}
