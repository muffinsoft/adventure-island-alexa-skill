package com.muffinsoft.alexa.skills.adventureisland;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.handlers.*;

public class AdventureIslandStreamHandler extends SkillStreamHandler {
    private static Skill getSkill() {
        String amazonSkillId = Constants.props.getProperty("amazon-skill-id");
        return Skills.standard()
                .addRequestHandlers(
                        new CancelAndStopIntentHandler(),
                        new FallbackIntentHandler(),
                        new StartOverIntentHandler(),
                        new YesIntentHandler(),
                        new NoIntentHandler(),
                        new ResumeIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new StartMissionIntent(),
                        new RandomQuestionHandler(),
                        new ActionIntentHandler(),
                        new SessionEndedRequestHandler(),
                        new BuyIntentHandler())
                .withTableName(Constants.props.getProperty("table-name"))
                .withAutoCreateTable(true)
                .build();
    }

    public AdventureIslandStreamHandler() {
        super(getSkill());
    }
}
