package com.massivecraft.factions.data;

import com.massivecraft.factions.*;
import com.massivecraft.factions.config.file.PermissionsConfig;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionNewAdminEvent;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.iface.RelationParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.LWC;
import com.massivecraft.factions.landraidcontrol.DTRControl;
import com.massivecraft.factions.landraidcontrol.LandRaidControl;
import com.massivecraft.factions.perms.*;
import com.massivecraft.factions.struct.BanInfo;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.LazyLocation;
import com.massivecraft.factions.util.MiscUtil;
import com.massivecraft.factions.util.RelationUtil;
import com.massivecraft.factions.util.TL;
import net.mcaurora.serialization.DeepObjectSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class MemoryFaction implements Faction, EconomyParticipator {
    protected String id = null;
    protected boolean peacefulExplosionsEnabled;
    protected boolean permanent;
    protected String tag;
    protected String description;
    protected String link;
    protected boolean open;
    protected boolean peaceful;
    protected Integer permanentPower;
    protected LazyLocation home;
    protected long foundedDate;
    protected transient long lastPlayerLoggedOffTime;
    protected double powerBoost;
    protected Map<String, Relation> relationWish = new HashMap<>();
    protected Map<FLocation, Set<String>> claimOwnership = new ConcurrentHashMap<>();
    protected transient Set<FPlayer> fplayers = new HashSet<>();
    protected Set<String> invites = new HashSet<>();
    protected HashMap<String, List<String>> announcements = new HashMap<>();
    protected ConcurrentHashMap<String, LazyLocation> warps = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String, String> warpPasswords = new ConcurrentHashMap<>();
    private long lastDeath;
    protected int maxVaults;
    protected Role defaultRole;
    protected LinkedHashMap<PermSelector, Map<String, Boolean>> permissions = new LinkedHashMap<>();
    protected Set<BanInfo> bans = new HashSet<>();
    protected double dtr;
    protected long lastDTRUpdateTime;
    protected long frozenDTRUntilTime;
    protected int tntBank;
    protected HashMap<String, Object> addonData = new HashMap<>();
    protected int memberLimit = 9;
    protected int tntLimit = 1_000_000;
    protected int warpLimit = 2;
    protected transient OfflinePlayer offlinePlayer;

    public HashMap<String, List<String>> getAnnouncements() {
        return this.announcements;
    }

    public void addAnnouncement(FPlayer fPlayer, String msg) {
        List<String> list = announcements.containsKey(fPlayer.getId()) ? announcements.get(fPlayer.getId()) : new ArrayList<>();
        list.add(msg);
        announcements.put(fPlayer.getId(), list);
    }

    public void sendUnreadAnnouncements(FPlayer fPlayer) {
        if (!announcements.containsKey(fPlayer.getId())) {
            return;
        }
        fPlayer.msg(TL.FACTIONS_ANNOUNCEMENT_TOP);
        for (String s : announcements.get(fPlayer.getPlayer().getUniqueId().toString())) {
            fPlayer.sendMessage(s);
        }
        fPlayer.msg(TL.FACTIONS_ANNOUNCEMENT_BOTTOM);
        announcements.remove(fPlayer.getId());
    }

    public void removeAnnouncements(FPlayer fPlayer) {
        announcements.remove(fPlayer.getId());
    }

    public ConcurrentHashMap<String, LazyLocation> getWarps() {
        return this.warps;
    }

    public LazyLocation getWarp(String name) {
        return this.warps.get(name);
    }

    public void setWarp(String name, LazyLocation loc) {
        this.warps.put(name, loc);
    }

    public boolean isWarp(String name) {
        return this.warps.containsKey(name);
    }

    public boolean removeWarp(String name) {
        warpPasswords.remove(name); // remove password no matter what.
        return warps.remove(name) != null;
    }

    public boolean isWarpPassword(String warp, String password) {
        return hasWarpPassword(warp) && warpPasswords.get(warp.toLowerCase()).equals(password);
    }

    public boolean hasWarpPassword(String warp) {
        return warpPasswords.containsKey(warp.toLowerCase());
    }

    public void setWarpPassword(String warp, String password) {
        warpPasswords.put(warp.toLowerCase(), password);
    }

    public void clearWarps() {
        warps.clear();
    }

    public int getMaxVaults() {
        return this.maxVaults;
    }

    public void setMaxVaults(int value) {
        this.maxVaults = value;
    }

    public Set<String> getInvites() {
        return invites;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.offlinePlayer = null;
    }

    public void invite(FPlayer fplayer) {
        this.invites.add(fplayer.getId());
    }

    public void deinvite(FPlayer fplayer) {
        this.invites.remove(fplayer.getId());
    }

    public boolean isInvited(FPlayer fplayer) {
        return this.invites.contains(fplayer.getId());
    }

    public void ban(FPlayer target, FPlayer banner) {
        BanInfo info = new BanInfo(banner.getId(), target.getId(), System.currentTimeMillis());
        this.bans.add(info);
    }

    public void unban(FPlayer player) {
        bans.removeIf(banInfo -> banInfo.getBanned().equalsIgnoreCase(player.getId()));
    }

    public boolean isBanned(FPlayer player) {
        for (BanInfo info : bans) {
            if (info.getBanned().equalsIgnoreCase(player.getId())) {
                return true;
            }
        }

        return false;
    }

    public Set<BanInfo> getBannedPlayers() {
        return this.bans;
    }

    public boolean getOpen() {
        return open;
    }

    public void setOpen(boolean isOpen) {
        open = isOpen;
    }

    public boolean isPeaceful() {
        return this.peaceful;
    }

    public void setPeaceful(boolean isPeaceful) {
        this.peaceful = isPeaceful;
    }

    public void setPeacefulExplosionsEnabled(boolean val) {
        peacefulExplosionsEnabled = val;
    }

    public boolean getPeacefulExplosionsEnabled() {
        return this.peacefulExplosionsEnabled;
    }

    public boolean noExplosionsInTerritory() {
        return this.peaceful && !peacefulExplosionsEnabled;
    }

    public boolean isPermanent() {
        return permanent || !this.isNormal();
    }

    public void setPermanent(boolean isPermanent) {
        permanent = isPermanent;
    }

    public String getTag() {
        return this.tag;
    }

    public String getTag(String prefix) {
        return prefix + this.tag;
    }

    public String getTag(Faction otherFaction) {
        if (otherFaction == null) {
            return getTag();
        }
        return this.getTag(this.getColorTo(otherFaction).toString());
    }

    public String getTag(FPlayer otherFplayer) {
        if (otherFplayer == null) {
            return getTag();
        }
        return this.getTag(this.getColorTo(otherFplayer).toString());
    }

    public void setTag(String str) {
        if (FactionsPlugin.getInstance().conf().factions().other().isTagForceUpperCase()) {
            str = str.toUpperCase();
        }
        this.tag = str;
    }

    public String getComparisonTag() {
        return MiscUtil.getComparisonString(this.tag);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getLink() {
        if (this.link == null) {
            this.link = FactionsPlugin.getInstance().conf().commands().link().getDefaultURL();
        }
        return this.link;
    }

    public void setLink(String value) {
        this.link = value;
    }

    public void setHome(Location home) {
        this.home = new LazyLocation(home);
    }

    public void delHome() {
        this.home = null;
    }

    public boolean hasHome() {
        return this.getHome() != null;
    }

    public Location getHome() {
        confirmValidHome();
        return (this.home != null) ? this.home.getLocation() : null;
    }

    public long getFoundedDate() {
        if (this.foundedDate == 0) {
            setFoundedDate(System.currentTimeMillis());
        }
        return this.foundedDate;
    }

    public void setFoundedDate(long newDate) {
        this.foundedDate = newDate;
    }

    public void confirmValidHome() {
        if (!FactionsPlugin.getInstance().conf().factions().homes().isMustBeInClaimedTerritory() || this.home == null || (this.home.getLocation() != null && Board.getInstance().getFactionAt(new FLocation(this.home.getLocation())) == this)) {
            return;
        }

        msg("<b>Your faction home has been un-set since it is no longer in your territory.");
        this.home = null;
    }

    public String getAccountId() {
        return "faction-" + this.getId();
    }

    public OfflinePlayer getOfflinePlayer() {
        if (this.offlinePlayer == null) {
            this.offlinePlayer = FactionsPlugin.getInstance().getFactionOfflinePlayer(this.getAccountId());
        }
        return this.offlinePlayer;
    }

    public void setLastDeath(long time) {
        this.lastDeath = time;
    }

    public long getLastDeath() {
        return this.lastDeath;
    }

    public int getKills() {
        int kills = 0;
        for (FPlayer fp : getFPlayers()) {
            kills += fp.getKills();
        }

        return kills;
    }

    public int getDeaths() {
        int deaths = 0;
        for (FPlayer fp : getFPlayers()) {
            deaths += fp.getDeaths();
        }

        return deaths;
    }

    // -------------------------------------------- //
    // F Permissions stuff
    // -------------------------------------------- //

    public boolean hasAccess(Selectable selectable, PermissibleAction permissibleAction, FLocation location) {
        if (selectable == null || permissibleAction == null) {
            return false; // Fail in a safe way
        }

        if (selectable == Role.ADMIN || (selectable instanceof FPlayer && ((FPlayer) selectable).getFaction() == this && ((FPlayer) selectable).getRole() == Role.ADMIN)) {
            return true;
        }

        PermissionsConfig permConf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        List<PermSelector> priority = permConf.getOverridePermissionsOrder().stream().filter(s -> s.test(selectable, this)).collect(Collectors.toList());
        for (PermSelector selector : priority) {
            Boolean bool = permConf.getOverridePermissions().get(selector).get(permissibleAction.getName());
            if (bool != null) {
                //FactionsPlugin.getInstance().debug("Permissions for " + selectable.getClass().getSimpleName() + ' ' + (selectable instanceof FPlayer ? ((FPlayer) selectable).getName() : (selectable instanceof Faction) ? ((Faction) selectable).getTag() : "") + " override " + selector.serialize() + ": " + bool);
                return bool;
            }
        }

        if (location != null) {
            // TODO migrate ownership stuffs
        }

        for (Map.Entry<PermSelector, Map<String, Boolean>> entry : permissions.entrySet()) {
            if (entry.getKey().test(selectable, this)) {
                Boolean bool = entry.getValue().get(permissibleAction.getName());
                if (bool != null) {
                    //FactionsPlugin.getInstance().debug("Permissions for " + selectable.getClass().getSimpleName() + ' ' + (selectable instanceof FPlayer ? ((FPlayer) selectable).getName() : (selectable instanceof Faction) ? ((Faction) selectable).getTag() : "") + " override " + entry.getKey().serialize() + ": " + bool);
                    return bool;
                }
            }
        }

        return false;
    }

    public LinkedHashMap<PermSelector, Map<String, Boolean>> getPermissions() {
        return permissions;
    }

    public void setPermissions(LinkedHashMap<PermSelector, Map<String, Boolean>> permissions) {
        this.permissions = permissions;
    }

    public void checkPerms() {
        if (this.permissions == null || this.permissions.isEmpty()) {
            this.resetPerms();
        }
    }

    public void resetPerms() {
        if (permissions == null) {
            permissions = new LinkedHashMap<>();
        } else {
            permissions.clear();
        }

        PermissionsConfig permConf = FactionsPlugin.getInstance().getConfigManager().getPermissionsConfig();
        for (PermSelector selector : permConf.getDefaultPermissionsOrder()) {
            Map<String, Boolean> map = new LinkedHashMap<>(permConf.getDefaultPermissions().get(selector));
            permissions.put(selector, map);
        }
    }

    public Role getDefaultRole() {
        return this.defaultRole;
    }

    public void setDefaultRole(Role role) {
        this.defaultRole = role;
    }

    // -------------------------------------------- //
    // Construct
    // -------------------------------------------- //
    protected MemoryFaction() {
    }

    public MemoryFaction(String id) {
        this.id = id;
        this.open = FactionsPlugin.getInstance().conf().factions().other().isNewFactionsDefaultOpen();
        this.tag = "???";
        this.description = TL.GENERIC_DEFAULTDESCRIPTION.toString();
        this.lastPlayerLoggedOffTime = 0;
        this.peaceful = FactionsPlugin.getInstance().conf().factions().other().isNewFactionsDefaultPeaceful();
        this.peacefulExplosionsEnabled = false;
        this.permanent = false;
        this.powerBoost = 0.0;
        this.foundedDate = System.currentTimeMillis();
        this.maxVaults = FactionsPlugin.getInstance().conf().playerVaults().getDefaultMaxVaults();
        this.defaultRole = FactionsPlugin.getInstance().conf().factions().other().getDefaultRole();
        this.dtr = FactionsPlugin.getInstance().conf().factions().landRaidControl().dtr().getStartingDTR();

        resetPerms(); // Reset on new Faction so it has default values.
    }

    public MemoryFaction(MemoryFaction old) {
        id = old.id;
        peacefulExplosionsEnabled = old.peacefulExplosionsEnabled;
        permanent = old.permanent;
        tag = old.tag;
        description = old.description;
        open = old.open;
        foundedDate = old.foundedDate;
        peaceful = old.peaceful;
        permanentPower = old.permanentPower;
        home = old.home;
        lastPlayerLoggedOffTime = old.lastPlayerLoggedOffTime;
        powerBoost = old.powerBoost;
        relationWish = old.relationWish;
        claimOwnership = old.claimOwnership;
        fplayers = new HashSet<>();
        invites = old.invites;
        announcements = old.announcements;
        this.defaultRole = old.defaultRole;
        this.dtr = old.dtr;

        resetPerms(); // Reset on new Faction so it has default values.
    }

    // -------------------------------------------- //
    // Extra Getters And Setters
    // -------------------------------------------- //
    public boolean noPvPInTerritory() {
        return isSafeZone() || (peaceful && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisablePVP());
    }

    public boolean noMonstersInTerritory() {
        return isSafeZone() ||
                (peaceful && FactionsPlugin.getInstance().conf().factions().specialCase().isPeacefulTerritoryDisableMonsters());
    }

    // -------------------------------
    // Understand the type
    // -------------------------------

    public boolean isNormal() {
        return !(this.isWilderness() || this.isSafeZone() || this.isWarZone());
    }

    public boolean isWilderness() {
        return this.getId().equals("0");
    }

    public boolean isSafeZone() {
        return this.getId().equals("-1");
    }

    public boolean isWarZone() {
        return this.getId().equals("-2");
    }

    public boolean isPlayerFreeType() {
        return this.isSafeZone() || this.isWarZone();
    }

    // -------------------------------
    // Relation and relation colors
    // -------------------------------

    @Override
    public String describeTo(RelationParticipator that, boolean ucfirst) {
        return RelationUtil.describeThatToMe(this, that, ucfirst);
    }

    @Override
    public String describeTo(RelationParticipator that) {
        return RelationUtil.describeThatToMe(this, that);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp) {
        return RelationUtil.getRelationTo(this, rp);
    }

    @Override
    public Relation getRelationTo(RelationParticipator rp, boolean ignorePeaceful) {
        return RelationUtil.getRelationTo(this, rp, ignorePeaceful);
    }

    @Override
    public ChatColor getColorTo(RelationParticipator rp) {
        return RelationUtil.getColorOfThatToMe(this, rp);
    }

    public Relation getRelationWish(Faction otherFaction) {
        if (this.relationWish.containsKey(otherFaction.getId())) {
            return this.relationWish.get(otherFaction.getId());
        }
        return Relation.fromString(FactionsPlugin.getInstance().conf().factions().other().getDefaultRelation()); // Always default to old behavior.
    }

    public void setRelationWish(Faction otherFaction, Relation relation) {
        if (this.relationWish.containsKey(otherFaction.getId()) && relation.equals(Relation.NEUTRAL)) {
            this.relationWish.remove(otherFaction.getId());
        } else {
            this.relationWish.put(otherFaction.getId(), relation);
        }
    }

    public int getRelationCount(Relation relation) {
        int count = 0;
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.getRelationTo(this) == relation) {
                count++;
            }
        }
        return count;
    }

    // ----------------------------------------------//
    // DTR
    // ----------------------------------------------//

    @Override
    public double getDTR() {
        LandRaidControl lrc = FactionsPlugin.getInstance().getLandRaidControl();
        if (lrc instanceof DTRControl) {
            ((DTRControl) lrc).updateDTR(this);
        }
        return this.dtr;
    }

    @Override
    public double getDTRWithoutUpdate() {
        return this.dtr;
    }

    @Override
    public void setDTR(double dtr) {
        this.dtr = dtr;
        this.lastDTRUpdateTime = System.currentTimeMillis();
    }

    @Override
    public long getLastDTRUpdateTime() {
        return this.lastDTRUpdateTime;
    }

    @Override
    public long getFrozenDTRUntilTime() {
        return this.frozenDTRUntilTime;
    }

    @Override
    public void setFrozenDTR(long time) {
        this.frozenDTRUntilTime = time;
    }

    @Override
    public boolean isFrozenDTR() {
        return System.currentTimeMillis() < this.frozenDTRUntilTime;
    }

    // ----------------------------------------------//
    // Power
    // ----------------------------------------------//
    @Deprecated
    public double getPower() {
        if (this.hasPermanentPower()) {
            return this.getPermanentPower();
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.getPower();
        }
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    @Deprecated
    public double getPowerMax() {
        if (this.hasPermanentPower()) {
            return this.getPermanentPower();
        }

        double ret = 0;
        for (FPlayer fplayer : fplayers) {
            ret += fplayer.getPowerMax();
        }
        if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax() > 0 && ret > FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax()) {
            ret = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getFactionMax();
        }
        return ret + this.powerBoost;
    }

    public int getPowerRounded() {
        return (int) Math.round(this.getPower());
    }

    public int getPowerMaxRounded() {
        return (int) Math.round(this.getPowerMax());
    }

    public boolean hasLandInflation() {
        return FactionsPlugin.getInstance().getLandRaidControl().hasLandInflation(this);
    }

    public Integer getPermanentPower() {
        return this.permanentPower;
    }

    public void setPermanentPower(Integer permanentPower) {
        this.permanentPower = permanentPower;
    }

    public boolean hasPermanentPower() {
        return this.permanentPower != null;
    }

    public double getPowerBoost() {
        return this.powerBoost;
    }

    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    public boolean isPowerFrozen() {
        int freezeSeconds = FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getPowerFreeze();
        return freezeSeconds != 0 && System.currentTimeMillis() - lastDeath < freezeSeconds * 1000L;
    }

    public int getLandRounded() {
        return Board.getInstance().getFactionCoordCount(this);
    }

    public int getLandRoundedInWorld(String worldName) {
        return Board.getInstance().getFactionCoordCountInWorld(this, worldName);
    }

    public int getTNTBank() {
        return this.tntBank;
    }

    public void setTNTBank(int amount) {
        this.tntBank = amount;
    }

    public Boolean hasAddonItem(String key) {
        return this.addonData.containsKey(key);
    }

    public Object getAddonItem(String key) {
        return this.addonData.get(key);
    }

    public <T> T getAddonItemAsObject(String key, Class<T> type) {
        return DeepObjectSerializer.loadObject(this, key, type);
    }

    public <T> List<T> getAddonItemAsList(String key, Class<T> type) {
        return DeepObjectSerializer.loadList(this, key, type);
    }

    public void setAddonItem(String key, Object value) {
        this.addonData.put(key, value);
    }

    public void setAddonItemAsObject(String key, Object value) {
        DeepObjectSerializer.putObject(this, value, key);
    }

    public void setAddonItemAsList(String key, List<?> objects) {
        DeepObjectSerializer.putList(this, objects, key);
    }

    public int getMemberLimit() {
        return this.memberLimit;
    }

    public void setMemberLimit(int limit) {
        this.memberLimit = limit;
    }

    public int getTNTLimit() {
        return this.tntLimit;
    }

    public void setTNTLimit(int limit) {
        this.tntLimit = limit;
    }

    public int getWarpLimit() {
        return this.warpLimit;
    }

    public void setWarpLimit(int limit) {
        this.warpLimit = limit;
    }

    // -------------------------------
    // FPlayers
    // -------------------------------

    // maintain the reference list of FPlayers in this faction
    public void refreshFPlayers() {
        fplayers.clear();
        if (this.isPlayerFreeType()) {
            return;
        }

        for (FPlayer fplayer : FPlayers.getInstance().getAllFPlayers()) {
            if (fplayer.getFactionId().equalsIgnoreCase(id)) {
                fplayers.add(fplayer);
            }
        }
    }

    public boolean addFPlayer(FPlayer fplayer) {
        return !this.isPlayerFreeType() && fplayers.add(fplayer);
    }

    public boolean removeFPlayer(FPlayer fplayer) {
        return !this.isPlayerFreeType() && fplayers.remove(fplayer);
    }

    public int getSize() {
        return fplayers.size();
    }

    public Set<FPlayer> getFPlayers() {
        // return a shallow copy of the FPlayer list, to prevent tampering and
        // concurrency issues
        return new HashSet<>(fplayers);
    }

    public Set<FPlayer> getFPlayersWhereOnline(boolean online) {
        Set<FPlayer> ret = new HashSet<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.isOnline() == online) {
                ret.add(fplayer);
            }
        }

        return ret;
    }

    public Set<FPlayer> getFPlayersWhereOnline(boolean online, FPlayer viewer) {
        if (viewer == null) {
            return getFPlayersWhereOnline(online);
        }
        Set<FPlayer> ret = new HashSet<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer viewed : fplayers) {
            // Add if their online status is what we want
            if (viewed.isOnline() == online) {
                // If we want online, check to see if we are able to see this player
                // This checks if they are in vanish.
                if (online
                        && viewed.getPlayer() != null
                        && viewer.getPlayer() != null
                        && viewer.getPlayer().canSee(viewed.getPlayer())) {
                    ret.add(viewed);
                    // If we want offline, just add them.
                    // Prob a better way to do this but idk.
                } else if (!online) {
                    ret.add(viewed);
                }
            }
        }

        return ret;
    }

    public FPlayer getFPlayerAdmin() {
        if (!this.isNormal()) {
            return null;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.getRole() == Role.ADMIN) {
                return fplayer;
            }
        }
        return null;
    }

    public ArrayList<FPlayer> getFPlayersWhereRole(Role role) {
        ArrayList<FPlayer> ret = new ArrayList<>();
        if (!this.isNormal()) {
            return ret;
        }

        for (FPlayer fplayer : fplayers) {
            if (fplayer.getRole() == role) {
                ret.add(fplayer);
            }
        }

        return ret;
    }

    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        if (this.isPlayerFreeType()) {
            return ret;
        }

        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
            if (fplayer.getFaction() == this) {
                ret.add(player);
            }
        }

        return ret;
    }

    // slightly faster check than getOnlinePlayers() if you just want to see if
    // there are any players online
    public boolean hasPlayersOnline() {
        // only real factions can have players online, not safe zone / war zone
        if (this.isPlayerFreeType()) {
            return false;
        }

        for (Player player : FactionsPlugin.getInstance().getServer().getOnlinePlayers()) {
            FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
            if (fplayer != null && fplayer.getFaction() == this) {
                return true;
            }
        }

        // even if all players are technically logged off, maybe someone was on
        // recently enough to not consider them officially offline yet
        return FactionsPlugin.getInstance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() > 0 && System.currentTimeMillis() < lastPlayerLoggedOffTime + (FactionsPlugin.getInstance().conf().factions().other().getConsiderFactionsReallyOfflineAfterXMinutes() * 60000);
    }

    public void memberLoggedOff() {
        if (this.isNormal()) {
            lastPlayerLoggedOffTime = System.currentTimeMillis();
        }
    }

    // used when current leader is about to be removed from the faction;
    // promotes new leader, or disbands faction if no other members left
    public void promoteNewLeader() {
        if (!this.isNormal()) {
            return;
        }
        if (this.isPermanent() && FactionsPlugin.getInstance().conf().factions().specialCase().isPermanentFactionsDisableLeaderPromotion()) {
            return;
        }

        FPlayer oldLeader = this.getFPlayerAdmin();

        // get list of coleaders, or mods, or list of normal members if there are no moderators
        ArrayList<FPlayer> replacements = this.getFPlayersWhereRole(Role.COLEADER);
        if (replacements == null || replacements.isEmpty()) {
            replacements = this.getFPlayersWhereRole(Role.MODERATOR);
        }

        if (replacements == null || replacements.isEmpty()) {
            replacements = this.getFPlayersWhereRole(Role.NORMAL);
        }

        if (replacements == null || replacements.isEmpty()) { // faction admin  is the only  member; one-man  faction
            if (this.isPermanent()) {
                if (oldLeader != null) {
                    oldLeader.setRole(Role.NORMAL);
                }
                return;
            }

            // no members left and faction isn't permanent, so disband it
            if (FactionsPlugin.getInstance().conf().logging().isFactionDisband()) {
                FactionsPlugin.getInstance().log("The faction " + this.getTag() + " (" + this.getId() + ") has been disbanded since it has no members left.");
            }

            for (FPlayer fplayer : FPlayers.getInstance().getOnlinePlayers()) {
                fplayer.msg(TL.LEAVE_DISBANDED, this.getTag(fplayer));
            }

            FactionsPlugin.getInstance().getServer().getPluginManager().callEvent(new FactionAutoDisbandEvent(this));

            Factions.getInstance().removeFaction(getId());
        } else { // promote new faction admin
            Bukkit.getServer().getPluginManager().callEvent(new FactionNewAdminEvent(replacements.get(0), this));

            if (oldLeader != null) {
                oldLeader.setRole(Role.COLEADER);
            }
            replacements.get(0).setRole(Role.ADMIN);
            //TODO:TL
            this.msg("<i>Faction admin <h>%s<i> has been removed. %s<i> has been promoted as the new faction admin.", oldLeader == null ? "" : oldLeader.getName(), replacements.get(0).getName());
            FactionsPlugin.getInstance().log("Faction " + this.getTag() + " (" + this.getId() + ") admin was removed. Replacement admin: " + replacements.get(0).getName());
        }
    }

    // ----------------------------------------------//
    // Messages
    // ----------------------------------------------//
    public void msg(String message, Object... args) {
        message = FactionsPlugin.getInstance().txt().parse(message, args);

        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    public void msg(TL translation, Object... args) {
        msg(translation.toString(), args);
    }

    public void sendMessage(String message) {
        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(message);
        }
    }

    public void sendMessage(List<String> messages) {
        for (FPlayer fplayer : this.getFPlayersWhereOnline(true)) {
            fplayer.sendMessage(messages);
        }
    }

    // ----------------------------------------------//
    // Ownership of specific claims
    // ----------------------------------------------//

    public Map<FLocation, Set<String>> getClaimOwnership() {
        return claimOwnership;
    }

    public void clearAllClaimOwnership() {
        claimOwnership.clear();
    }

    public void clearClaimOwnership(FLocation loc) {
        if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
            LWC.clearAllLocks(loc);
        }
        claimOwnership.remove(loc);
    }

    public void clearClaimOwnership(FPlayer player) {
        if (id == null || id.isEmpty()) {
            return;
        }

        Set<String> ownerData;

        for (Entry<FLocation, Set<String>> entry : claimOwnership.entrySet()) {
            ownerData = entry.getValue();

            if (ownerData == null) {
                continue;
            }

            ownerData.removeIf(s -> s.equals(player.getId()));

            if (ownerData.isEmpty()) {
                if (LWC.getEnabled() && FactionsPlugin.getInstance().conf().lwc().isResetLocksOnUnclaim()) {
                    LWC.clearAllLocks(entry.getKey());
                }
                claimOwnership.remove(entry.getKey());
            }
        }
    }

    public int getCountOfClaimsWithOwners() {
        return claimOwnership.isEmpty() ? 0 : claimOwnership.size();
    }

    public boolean doesLocationHaveOwnersSet(FLocation loc) {
        if (claimOwnership.isEmpty() || !claimOwnership.containsKey(loc)) {
            return false;
        }

        Set<String> ownerData = claimOwnership.get(loc);
        return ownerData != null && !ownerData.isEmpty();
    }

    public boolean isPlayerInOwnerList(FPlayer player, FLocation loc) {
        if (claimOwnership.isEmpty()) {
            return false;
        }
        Set<String> ownerData = claimOwnership.get(loc);
        return ownerData != null && ownerData.contains(player.getId());
    }

    public void setPlayerAsOwner(FPlayer player, FLocation loc) {
        Set<String> ownerData = claimOwnership.get(loc);
        if (ownerData == null) {
            ownerData = new HashSet<>();
        }
        ownerData.add(player.getId());
        claimOwnership.put(loc, ownerData);
    }

    public void removePlayerAsOwner(FPlayer player, FLocation loc) {
        Set<String> ownerData = claimOwnership.get(loc);
        if (ownerData == null) {
            return;
        }
        ownerData.remove(player.getId());
        claimOwnership.put(loc, ownerData);
    }

    public Set<String> getOwnerList(FLocation loc) {
        return claimOwnership.get(loc);
    }

    public String getOwnerListString(FLocation loc) {
        Set<String> ownerData = claimOwnership.get(loc);
        if (ownerData == null || ownerData.isEmpty()) {
            return "";
        }

        StringBuilder ownerList = new StringBuilder();

        for (String anOwnerData : ownerData) {
            if (ownerList.length() > 0) {
                ownerList.append(", ");
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(anOwnerData));
            //TODO:TL
            ownerList.append(offlinePlayer != null ? offlinePlayer.getName() : "null player");
        }
        return ownerList.toString();
    }

    public boolean playerHasOwnershipRights(FPlayer fplayer, FLocation loc) {
        // in own faction, with sufficient role or permission to bypass
        // ownership?
        if (fplayer.getFaction() == this && (fplayer.getRole().isAtLeast(FactionsPlugin.getInstance().conf().factions().ownedArea().isModeratorsBypass() ? Role.MODERATOR : Role.ADMIN) || Permission.OWNERSHIP_BYPASS.has(fplayer.getPlayer()))) {
            return true;
        }

        // make sure claimOwnership is initialized
        if (claimOwnership.isEmpty()) {
            return true;
        }

        // need to check the ownership list, then
        Set<String> ownerData = claimOwnership.get(loc);

        // if no owner list, owner list is empty, or player is in owner list,
        // they're allowed
        return ownerData == null || ownerData.isEmpty() || ownerData.contains(fplayer.getId());
    }

    // ----------------------------------------------//
    // Persistance and entity management
    // ----------------------------------------------//
    public void remove() {
        if (Econ.shouldBeUsed() && FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            Econ.setBalance(this, 0);
        }

        // Clean the board
        ((MemoryBoard) Board.getInstance()).clean(id);

        for (FPlayer fPlayer : fplayers) {
            fPlayer.resetFactionData(false);
        }
    }

    public Set<FLocation> getAllClaims() {
        return Board.getInstance().getAllClaims(this);
    }
}
