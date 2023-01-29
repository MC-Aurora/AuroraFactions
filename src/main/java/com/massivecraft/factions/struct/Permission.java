package com.massivecraft.factions.struct;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.command.CommandSender;

public class Permission {
    public static Permission MANAGE_SAFE_ZONE = new Permission("managesafezone");
    public static Permission MANAGE_WAR_ZONE = new Permission("managewarzone");
    public static Permission OWNERSHIP_BYPASS = new Permission("ownershipbypass");
    public static Permission ADMIN = new Permission("admin");
    public static Permission ADMIN_ANY = new Permission("admin.any");
    public static Permission AHOME = new Permission("ahome");
    public static Permission ANNOUNCE = new Permission("announce");
    public static Permission AUTOCLAIM = new Permission("autoclaim");
    public static Permission AUTO_LEAVE_BYPASS = new Permission("autoleavebypass");
    public static Permission BAN = new Permission("ban");
    public static Permission BYPASS = new Permission("bypass");
    public static Permission CHAT = new Permission("chat");
    public static Permission CHATSPY = new Permission("chatspy");
    public static Permission CLAIM = new Permission("claim");
    public static Permission CLAIMAT = new Permission("claimat");
    public static Permission CLAIM_FILL = new Permission("claim.fill");
    public static Permission CLAIM_LINE = new Permission("claim.line");
    public static Permission CLAIM_RADIUS = new Permission("claim.radius");
    public static Permission COLEADER = new Permission("coleader");
    public static Permission COLEADER_ANY = new Permission("coleader.any");
    public static Permission COORDS = new Permission("coords");
    public static Permission CREATE = new Permission("create");
    public static Permission DEFAULTRANK = new Permission("defaultrank");
    public static Permission DEINVITE = new Permission("deinvite");
    public static Permission DELHOME = new Permission("delhome");
    public static Permission DESCRIPTION = new Permission("description");
    public static Permission DISBAND = new Permission("disband");
    public static Permission DISBAND_ANY = new Permission("disband.any");
    public static Permission DTR = new Permission("dtr");
    public static Permission DTR_ANY = new Permission("dtr.any");
    public static Permission FLY = new Permission("fly");
    public static Permission FLY_AUTO = new Permission("fly.auto");
    public static Permission FLY_WILDERNESS = new Permission("fly.wilderness");
    public static Permission FLY_SAFEZONE = new Permission("fly.safezone");
    public static Permission FLY_WARZONE = new Permission("fly.warzone");
    public static Permission FLY_TRAILS = new Permission("fly.trails");
    public static Permission HELP = new Permission("help");
    public static Permission HOME = new Permission("home");
    public static Permission INVITE = new Permission("invite");
    public static Permission JOIN = new Permission("join");
    public static Permission JOIN_ANY = new Permission("join.any");
    public static Permission JOIN_OTHERS = new Permission("join.others");
    public static Permission KICK = new Permission("kick");
    public static Permission KICK_ANY = new Permission("kick.any");
    public static Permission LEAVE = new Permission("leave");
    public static Permission LINK = new Permission("link");
    public static Permission LIST = new Permission("list");
    public static Permission LISTCLAIMS = new Permission("listclaims");
    public static Permission LISTCLAIMS_OTHER = new Permission("listclaims.other");
    public static Permission LOCK = new Permission("lock");
    public static Permission MAP = new Permission("map");
    public static Permission MAPHEIGHT = new Permission("mapheight");
    public static Permission MOD = new Permission("mod");
    public static Permission MOD_ANY = new Permission("mod.any");
    public static Permission MODIFY_DTR = new Permission("modifydtr");
    public static Permission MODIFY_POWER = new Permission("modifypower");
    public static Permission MONEY_BALANCE = new Permission("money.balance");
    public static Permission MONEY_BALANCE_ANY = new Permission("money.balance.any");
    public static Permission MONEY_DEPOSIT = new Permission("money.deposit");
    public static Permission MONEY_WITHDRAW = new Permission("money.withdraw");
    public static Permission MONEY_WITHDRAW_ANY = new Permission("money.withdraw.any");
    public static Permission MONEY_F2F = new Permission("money.f2f");
    public static Permission MONEY_F2P = new Permission("money.f2p");
    public static Permission MONEY_P2F = new Permission("money.p2f");
    public static Permission MONEY_MODIFY = new Permission("money.modify");
    public static Permission MONITOR_LOGINS = new Permission("monitorlogins");
    public static Permission NO_BOOM = new Permission("noboom");
    public static Permission OPEN = new Permission("open");
    public static Permission OWNER = new Permission("owner");
    public static Permission OWNERLIST = new Permission("ownerlist");
    public static Permission SET_PEACEFUL = new Permission("setpeaceful");
    public static Permission SET_PERMANENT = new Permission("setpermanent");
    public static Permission SET_PERMANENTPOWER = new Permission("setpermanentpower");
    public static Permission SHOW_INVITES = new Permission("showinvites");
    public static Permission PERMISSIONS = new Permission("permissions");
    public static Permission POWERBOOST = new Permission("powerboost");
    public static Permission POWER = new Permission("power");
    public static Permission POWER_ANY = new Permission("power.any");
    public static Permission PROMOTE = new Permission("promote");
    public static Permission RELATION = new Permission("relation");
    public static Permission RELOAD = new Permission("reload");
    public static Permission SAVE = new Permission("save");
    public static Permission SETHOME = new Permission("sethome");
    public static Permission SETHOME_ANY = new Permission("sethome.any");
    public static Permission SHOW = new Permission("show");
    public static Permission SHOW_BYPASS_EXEMPT = new Permission("show.bypassexempt");
    public static Permission STATUS = new Permission("status");
    public static Permission STUCK = new Permission("stuck");
    public static Permission TAG = new Permission("tag");
    public static Permission TITLE = new Permission("title");
    public static Permission TITLE_COLOR = new Permission("title.color");
    public static Permission TNT_INFO = new Permission("tnt.info");
    public static Permission TNT_DEPOSIT = new Permission("tnt.deposit");
    public static Permission TNT_WITHDRAW = new Permission("tnt.withdraw");
    public static Permission TNT_FILL = new Permission("tnt.fill");
    public static Permission TNT_SIPHON = new Permission("tnt.siphon");
    public static Permission TOGGLE_ALLIANCE_CHAT = new Permission("togglealliancechat");
    public static Permission UNCLAIM = new Permission("unclaim");
    public static Permission UNCLAIM_FILL = new Permission("unclaim.fill");
    public static Permission UNCLAIM_ALL = new Permission("unclaimall");
    public static Permission VERSION = new Permission("version");
    public static Permission SCOREBOARD = new Permission("scoreboard");
    public static Permission SEECHUNK = new Permission("seechunk");
    public static Permission SETWARP = new Permission("setwarp");
    public static Permission TOP = new Permission("top");
    public static Permission VAULT = new Permission("vault");
    public static Permission SETMAXVAULTS = new Permission("setmaxvaults");
    public static Permission NEAR = new Permission("near");
    public static Permission WARP = new Permission("warp");
    public static Permission UPDATES = new Permission("updates");
    public static Permission DEBUG = new Permission("debug");

    public final String node;

    public Permission(final String node) {
        this.node = "factions." + node;
    }

    @Override
    public String toString() {
        return this.node;
    }

    public boolean has(CommandSender sender, boolean informSenderIfNot) {
        return FactionsPlugin.getInstance().getPermUtil().has(sender, this.node, informSenderIfNot);
    }

    public boolean has(CommandSender sender) {
        return has(sender, false);
    }
}
