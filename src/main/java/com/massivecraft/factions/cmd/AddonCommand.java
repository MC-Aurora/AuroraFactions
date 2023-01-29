package com.massivecraft.factions.cmd;

import com.massivecraft.factions.util.TL;

import java.util.Arrays;
import java.util.List;

public abstract class AddonCommand extends FCommand {

    public AddonCommand(List<String> aliases) {
        super();
        this.aliases.addAll(aliases);
    }

    public AddonCommand(String name) {
        super();
        this.aliases.add(name);
    }

    public abstract String getHelpShort();

    @Override
    public TL getUsageTranslation() {
        return null;
    }

}
