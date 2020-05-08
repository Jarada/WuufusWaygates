package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.util.Util;
import org.bukkit.command.CommandSender;

import java.util.IllegalFormatException;
import java.util.logging.Level;

public enum Msg {

    // TODO Capture phrases around plugin, add and cleanup

    /* WAYGATES */
    GATE_ACCESS_DENIED("&cError: &fAccess denied, gate is private."),
    GATE_ALREADY_EXISTS("&cError: &fThere is already a gate there, perhaps use a Waygate Key?"),
    GATE_CHANGE_OWNER("&6Enter name of new owner for Waygate:"),
    GATE_CHANGE_OWNER_FAILED("&cError: &fUnable to find an online player matching that name"),
    GATE_CHANGE_OWNER_SUCCESS("&aSuccess: &fWaygate %s is now owned by %s!"),
    GATE_CREATED("&aSuccess: &fGate online and can be used and edited using a Waygate Key"),
    GATE_DESTROYED("&cGate Disruption: &fWaygate %s has been disrupted and can no longer function"),
    GATE_MUST_CONTAIN("&cError: &fThe gate frame must contain %s!"),
    GATE_MUST_CONTAIN_GROUPED("required blocks, please contact server operators for full list"),
    GATE_NO_FRAME("&cError: &fThere is no frame for the gate, or it is too big."),
    GATE_OWNER("&dOwner: %s"),
    GATE_SET_NAME("&6Enter new name for Waygate (max %d characters):"),
    LORE_CONSTRUCTOR_NAME("&aWaygate Constructor"),
    LORE_CONSTRUCTOR_1("&fManipulates space/time"),
    LORE_CONSTRUCTOR_2("&fto coalesce the energies"),
    LORE_CONSTRUCTOR_3("&fneeded to construct a"),
    LORE_CONSTRUCTOR_4("&fWaygate."),
    LORE_KEY_NAME("&aWaygate Key"),
    LORE_KEY_1("&fAllows you to access and"),
    LORE_KEY_2("&fmanipulate Waygates, to"),
    LORE_KEY_3("&fuse and modify them."),
    LORE_KEY_4(""),
    MAX_LENGTH_EXCEEDED("&cMax length (&f%s&c) exceeded."),
    MENU_COLOR_GATE("&6"),
    MENU_COLOR_NETWORK("&6"),
    MENU_COLOR_SYSTEM_NETWORK("&5"),
    MENU_GATE_HIDDEN("&7[Hidden]"),
    MENU_GATE_PRIVATE("&c[Private]"),
    MENU_GATE_RESTRICTED("&7[Owner Only]"),
    MENU_LORE_HIDDEN_1("&fIf hidden, only Owner or"),
    MENU_LORE_HIDDEN_2("&fAdmins can see and travel to"),
    MENU_LORE_HIDDEN_3("&fWaygate in network."),
    MENU_LORE_PRIVATE_1("&fIf private, only Owner or"),
    MENU_LORE_PRIVATE_2("&fAdmins can activate or"),
    MENU_LORE_PRIVATE_3("&fenter Waygate."),
    MENU_TEXT_EDITABLE("&9%s"),
    MENU_TEXT_DESTINATION_SET("&9%s"),
    MENU_TEXT_DESTINATION_UNSET("&7No Destination"),
    MENU_TEXT_HIDDEN_SET("&7Is Hidden"),
    MENU_TEXT_HIDDEN_UNSET("&2Is Visible"),
    MENU_TEXT_PRIVATE_SET("&4Is Private"),
    MENU_TEXT_PRIVATE_UNSET("&2Is Open"),
    MENU_TEXT_STANDARD("&f%s"),
    MENU_TITLE_CANCEL("&aCancel"),
    MENU_TITLE_CLOSE("&aClose"),
    MENU_TITLE_DESTINATION("&aFixed Destination"),
    MENU_TITLE_DESTINATION_CLEAR("&aClear Destination"),
    MENU_TITLE_HIDDEN("&aHidden"),
    MENU_TITLE_NAME("&aName"),
    MENU_TITLE_NETWORK("&aNetwork"),
    MENU_TITLE_NETWORK_CREATE("&aCreate Network"),
    MENU_TITLE_NETWORK_MANAGE("&aManage Network"),
    MENU_TITLE_NETWORK_INVITE_ADD("&aAdd Player"),
    MENU_TITLE_NETWORK_INVITE_EXISTING("&aActive Player"),
    MENU_TITLE_NETWORK_OWNER("&aNetwork Owner"),
    MENU_TITLE_NETWORK_TYPE_GLOBAL("&aGlobal Network"),
    MENU_TITLE_NETWORK_TYPE_INVITE("&aInvite Network"),
    MENU_TITLE_NETWORK_TYPE_PRIVATE("&aPrivate Network"),
    MENU_TITLE_NEXT("&aNext Page"),
    MENU_TITLE_PAGE("&aPage"),
    MENU_TITLE_PREVIOUS("&aPrevious Page"),
    MENU_TITLE_PRIVATE("&aPrivate"),
    MENU_TITLE_SETTINGS("&aSettings"),
    MENU_TITLE_GATE_OWNER("&aWaygate Owner"),
    NETWORK_CHANGE_INVITE_ADD("&6Enter name of new invitee to Network %s:"),
    NETWORK_CHANGE_INVITE_DUPE("&cError: &f%s is already part of Network %s!"),
    NETWORK_CHANGE_INVITE_FAILED("&cError: &fUnable to find an online player matching that name"),
    NETWORK_CHANGE_INVITE_SUCCESS("&aSuccess: &f%s invited to Network %s!"),
    NETWORK_CHANGE_OWNER("&6Enter name of new owner for Network %s:"),
    NETWORK_CHANGE_OWNER_FAILED("&cError: &fUnable to find an online player matching that name"),
    NETWORK_CHANGE_OWNER_SUCCESS("&aSuccess: &fNetwork %s is now owned by %s!"),
    NETWORK_CREATE_SET_NAME("&6Enter name for new Network (max %d characters):"),
    NETWORK_CREATE_SET_TYPE("&aSuccess: &6To complete select Network type."),
    NETWORK_CREATE_GLOBAL_UNIQUE("&cError: &fGlobal Network names must be unique, please retry with a new name."),
    NETWORK_GLOBAL("&dGlobal Network"),
    NETWORK_INVITE("&dInvite Network"),
    NETWORK_PRIVATE("&dPrivate Network"),
    NETWORK_SYSTEM("&9System Network"),
    NETWORK_SYSTEM_NAME_NETHER("&9Nether"),
    NETWORK_SYSTEM_NAME_OCEAN("&9Ocean"),
    NETWORK_SYSTEM_NAME_OVERWORLD("&9Overworld"),
    NETWORK_SYSTEM_NAME_THE_END("&9The End"),
    NETWORK_SYSTEM_NAME_UNDERWORLD("&9Underworld"),
    NETWORK_SYSTEM_NAME_VOID("&9Void"),
    NETWORK_SYSTEM_VOID("&9System Void Network"),
    RENAMED("&6%s &frenamed to &6%s&f."),
    WORD_EITHER("either"),
    WORD_GATES("gate(s)"),
    WORD_OR("or");

    /* WAYPOINTS */
//    BLOCKED_CANCEL("&cWaypoint location blocked, aborting quantization procedure."),
//    BORDER("&2------------------------------"),
//    CMD_NO_CONSOLE("&cError: &fCommand unavailable to CONSOLE."),
//    COMMAND_CANCEL("&cUnable to act while undergoing quantization."),
//    DAMAGE_CANCEL("&cTransmission interrupted by kinetic interference."),
//    DISCOVERED_WAYPOINT("Discovered &6%s&f! Directory updated with new entry."),
//    DISCOVERY_MODE_DISABLED("&6%s: &fDiscovery mode disabled."),
//    DISCOVERY_MODE_ENABLED_SERVER("&6%s: &fDiscovery mode set to &aServer-wide&f."),
//    DISCOVERY_MODE_ENABLED_WORLD("&6%s: &fDiscovery mode set to &aWorld-specific&f."),
//    HOME_WP_ALREADY_HERE("&cError: &fHome waypoint &6%s &falready lies at this location."),
//    HOME_WP_CREATED("&aSuccess: &fCoordinates received for &6%s&f."),
//    HOME_WP_LOCATION_UPDATED("&aSuccess: &fCoordinates updated for &6%s&f."),
//    HOME_WP_REPLACED("&aSuccess: &fOverflow; &6%s &freplaced with coordinates to &6%s&f."),
//    INSUFFICIENT_POWER("&cInsufficient power."),
//    INVALD_DURABILITY("&cError: &fInvalid durability value."),
//    INVALID_MATERIAL("&cError: &fInvalid material type."),
//    LIST_HOME_WAYPOINTS("&aHome Waypoints: &6%s"),
//    LORE_LINE(" &5&o%s"),
//    NO_PERMS("&cError: &fAccess denied."),
//    NO_WAYPOINTS("&fNone"),
//    ONLY_SERVER_DEFINED("&cError: &fThis command only applies to server-defined waypoints."),
//    OPEN_WP_MENU("&6%s: &fOpening waypoint directory."),
//    PORT_TASK_1("&aPlotting course for &6%s&a. Scanning &6%s&a..."),
//    PORT_TASK_2("&a ...scan complete. Course locked in."),
//    PORT_TASK_3("&aSynchronizing qubits; standby for quantization."),
//    PORT_TASK_4("&9Energizing..."),
//    RELOADED("&aWaypoints &freloaded."),
//    REMOTELY_ACCESSED("&aWaypoint Beacon: &fEstablished remote connection to waypoint directory."),
//    RESPAWN_BLOCKED("&6%s: &cWaypoint registered for death-induced reintegration blocked."),
//    RESPAWN_NO_POWER("&6%s: &fInsufficient power levels may have resulted in reintegration at a closer waypoint."),
//    RESPAWN_NOT_FOUND("&cWaypoint registered for death-induced reintegration is no longer functional."),
//    SELECTED_1(" &6%s &f&o(%s)"),
//    SELECTED_2(" &aX: &f%d  &aPitch: &f%d"),
//    SELECTED_3(" &aY: &f%d  &aYaw: &f%d"),
//    SELECTED_4(" &aZ: &f%d"),
//    SELECTED_DISCOVER(" &aDiscovery: &f%s"),
//    SETHOME_DEFAULT_DESC("User-defined"),
//    SET_PLAYER_SPAWN("&6%s: &fWaypoint registered as death-induced reintegration point."),
//    SET_SPAWN("&fSpawn location updated for world &6%s&f."),
//    USAGE_SETICON("&cUsage: &f/wp icon <Material>"),
//    USAGE_SETHOME("&cUsage: &f/sethome <name ...>"),
//    USAGE_WP_RENAME("&cUsage: &f/wp rename <name ...>"),
//    USAGE_WP_ADD("&cUsage: &f/wp add <name ...>"),
//    WAYPOINT_DISABLED("&6%s: &fWaypoint disabled."),
//    WAYPOINT_ENABLED("&6%S: &fWaypoint enabled."),
//    WP_ALREADY_HERE("&cError: &fCoordinates overlap with &6%s&f."),
//    WP_DESC_CLEARED("&6%s: &fDescription cleared."),
//    WP_DESC_UPDATED("&6%s: &fDescription updated."),
//    WP_DESC_UPDATED_BOOK("&6%s: &fScanned &a%s&f. Directory data updated."),
//    WP_DUPLICATE_NAME("&cError: &fA duplicate entry for \"%s\" was found."),
//    WP_LIST("&aWaypoints: &6"),
//    WP_LOCATION_UPDATED("&6%s: &fLocation updated."),
//    WP_NO_DESC(" &oNo description."),
//    WP_NOT_EXIST("&cError: &fWaypoint \"%s\" not found."),
//    WP_NOT_SELECTED_ERROR("&cError: &fNo waypoint selected."),
//    WP_NOT_SELECTED_ERROR_USAGE("&cUse \"&f/wp select <name>&c\" to select a waypoint."),
//    WP_REMOVED("&6%s &fremoved from directory."),
//    WP_RENAMED("&6%s &frenamed to &6%s&f."),
//    WP_SELECTED("&fWaypoint &6%s &fselected."),
//    WP_SETICON("&6%s: &fDirectory icon set to &a%s&f:&a%s&f.");

    private final String defaultMsg;

    Msg(String defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    public String getDefaultMsg() {
        return defaultMsg;
    }

    @Override
    public String toString() {
        return DataManager.getManager().getMsg(this);
    }

    public String toString(Object... args) {
        String msg;

        try {
            msg = String.format(toString(), args);
        } catch (IllegalFormatException e) {
            msg = String.format(defaultMsg, args);
            PluginMain
                    .getPluginInstance()
                    .getLogger()
                    .log(Level.WARNING,
                            String.format("\"Waypoints.Messages.%s\" is misconfigured in plugin.yml.", this.name()));
        }
        
        return msg;
    }

    public void sendTo(CommandSender sender) {
        sender.sendMessage(Util.color(toString()));
    }

    public void sendTo(CommandSender sender, Object... args) {
        String msg;

        try {
            msg = String.format(toString(), args);
        } catch (IllegalFormatException e) {
            msg = String.format(defaultMsg, args);
            PluginMain
                    .getPluginInstance()
                    .getLogger()
                    .log(Level.WARNING,
                            String.format("\"Waypoints.Messages.%s\" is misconfigured in plugin.yml.", this.name()));
        }

        sender.sendMessage(Util.color(msg));
    }

}
