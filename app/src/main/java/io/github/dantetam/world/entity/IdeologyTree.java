package io.github.dantetam.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.dantetam.world.action.Ability;

/**
 * Created by Dante on 9/25/2016.
 */
public class IdeologyTree {

    public static HashMap<String, Ideology> globalMap;
    public HashMap<String, Ideology> clanIdeologyMap;

    public Clan clan;
    public Ideology primaryIdeology;
    public int numIdeologiesAndTenetsUnlocked() {
        int count = 0;
        for (Map.Entry<String, Ideology> entry: clanIdeologyMap.entrySet()) {
            if (entry.getValue().opened) {
                count++;
            }
            for (Tenet tenet: entry.getValue().tenets.values()) {
                if (tenet.activated) {
                    count++;
                }
            }
        }
        return count;
    }

    public IdeologyTree(Clan c) {
        clan = c;
        clanIdeologyMap = new HashMap<>();
    }

    public class Ideology {
        public Tenet root;
        public HashMap<String, Tenet> tenets;

        public String name;

        public Ideology(String n, Tenet r, HashMap<String, Tenet> tenetsMap) {
            name = n;
            root = r;
            tenets = tenetsMap;
        }

        public boolean opened = false;
        public boolean fullyUnlocked = false;

        public void openIdeology() {
            unlock("");
        }

        public void unlock(String tenetName) {
            opened = true;
            if (tenets.containsKey(tenetName)) {
                tenets.get(tenetName).activated = true;
            }
            for (Tenet entry: tenets.values()) {
                if (entry.activated) continue; else return;
            }
            fullyUnlocked = true;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Ideology)) return false;
            return name.equals(((Ideology) o).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
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
