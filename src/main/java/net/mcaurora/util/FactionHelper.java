package net.mcaurora.util;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class FactionHelper {
    /**
     * Get the faction a player is in.
     *
     * @param player Bukkit Player
     * @return Faction Object
     */
    public Faction getFaction(Player player) {
        return FPlayers.getInstance().getByPlayer(player).getFaction();
    }

    /**
     * Check if two players are in the same faction.
     *
     * @param player  Bukkit Player 1
     * @param player2 Bukkit Player 2
     * @return true if player 1 and 2 are in the same faction
     */
    public boolean isInSameFaction(Player player, Player player2) {
        return getFaction(player).getId().equals(getFaction(player2).getId());
    }

    /**
     * Check if a player has a faction
     *
     * @param player Bukkit Player
     * @return true if the player is not part of wilderness
     */
    public boolean hasFaction(Player player) {
        return !getFaction(player).getId().equals(Factions.getInstance().getWilderness().getId());
    }
}
