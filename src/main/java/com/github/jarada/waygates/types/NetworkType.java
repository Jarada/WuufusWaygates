package com.github.jarada.waygates.types;

// TODO Create Network Type for Auto-Hide Gates
// Only Admin can unhide gates
// Allows for connecting to a network where you can
// only travel to certain destinations
// All gates are hidden except admins can unhide certain gates
// If there is only one unhidden gate, that gate is a destination
// for other gates on the network.
// Players cannot set destination or hidden status when on
// Fixed Networks, these are ignored unless set by admin
// ~ Gates are hidden if owned by player not invited to Network
// ~ Gates can be hidden or unhidden if owned by player invited to Network
public enum NetworkType {

    SYSTEM_VOID,
    SYSTEM,
    GLOBAL,
    FIXED,
    INVITE,
    PRIVATE

}
