package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.data.Controller;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.types.MenuSize;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public abstract class WaygateAccessMenu extends Menu {

    List<Gate> accessList;
    Gate[]     optionWaygates;

    WaygateAccessMenu(MenuManager mm, Player p, Controller currentController, List<Gate> accessList, boolean loadGate) {
        super(mm, p, currentController, loadGate);
        this.accessList = accessList;
    }

    WaygateAccessMenu(MenuManager mm, Player p, Gate currentWaygate, List<Gate> accessList) {
        super(mm, p, currentWaygate);
        this.accessList = accessList;
    }

    @Override
    protected void destroy() {
        super.destroy();
        accessList = null;
        optionWaygates = null;
    }

    @Override
    public int getDesiredMenuSize() {
        return MenuSize.getAppropriateMenuSize(accessList.size());
    }

    void buildGates() {
        optionWaygates = new Gate[size];

        for (int slot = 0; slot < (size - MenuSize.STEP_SIZE); slot++) {
            int index = ((page - 1) * MenuSize.STEP_SIZE) + slot;

            if (index > accessList.size() - 1)
                break;

            Gate gate = accessList.get(index);
            setOption(slot, gate);
        }
    }

}
