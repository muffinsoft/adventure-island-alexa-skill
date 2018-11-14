package com.muffinsoft.alexa.skills.adventureisland;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.handlers.*;

public class AdventureIslandStreamHandler extends SkillStreamHandler {
    private static Skill getSkill() {
        String amazonSkillId = System.getProperty("amazon-skill-id");
        return Skills.standard()
                .addRequestHandlers(
                        new StopIntentHandler(),
                        new CancelIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new ActionIntentHandler(),
                        new SessionEndedRequestHandler())
                .withSkillId(amazonSkillId)
                .withTableName(Constants.TABLE_NAME)
                .withAutoCreateTable(true)
                .build();
    }

    public AdventureIslandStreamHandler() {
        super(getSkill());
    }
}
