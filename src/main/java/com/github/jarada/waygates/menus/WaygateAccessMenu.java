package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.data.Gate;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class WaygateAccessMenu extends Menu {

    List<Gate> accessList;
    Gate[]     optionWaygates;

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

    void buildGates() {
        optionWaygates = new Gate[size];

        for (int slot = 0; slot < 9; slot++) {
            int index = ((page - 1) * 9) + slot;

            if (index > accessList.size() - 1)
                break;

            Gate gate = accessList.get(index);
            setOption(slot, gate);
        }
    }

}
