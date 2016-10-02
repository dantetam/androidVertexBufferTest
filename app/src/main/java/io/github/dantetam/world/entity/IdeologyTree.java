package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.world.action.Ability;

/**
 * Created by Dante on 9/25/2016.
 */
public class IdeologyTree {

    public static HashMap<String, Ideology> globalMap;
    public HashMap<String, Ideology> clanIdeologyMap;

    public Clan clan;

    public IdeologyTree(Clan c) {
        clan = c;
        clanIdeologyMap = new HashMap<>();
    }

    public class Ideology {
        public Tenet root;
        public HashMap<String, Tenet> tenets;

        public Ideology(Tenet r, HashMap<String, Tenet> tenetsMap) {
            root = r;
            tenets = tenetsMap;
        }

        public boolean unlocked = false;
        public void unlock(String tenetName) {
            if (tenets.containsKey(tenetName)) {
                tenets.get(tenetName).activated = true;
            }
            for (Tenet entry: tenets.values()) {
                if (entry.activated) continue; else return;
            }
            unlocked = true;
        }
    }

    public class Tenet {
        public String name, iconName;
        public boolean activated = false;
        public Ability ability;
        public List<Tenet> parent, children;

        public Tenet(String n, String icon, Ability a) {
            name = n;
            iconName = icon;
            ability = a;
            parent = new ArrayList<>();
            children = new ArrayList<>();
        }
    }

}
