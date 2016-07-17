package io.github.dantetam.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.dantetam.opstrykontest.Vector4f;

/**
 * Created by Dante on 7/16/2016.
 */
public class ClanFactory {

    public static HashMap<Clan.ClanType, List<Vector4f>> clanTypeColorSchemes;
    public static HashMap<Clan.ClanFaction, List<Vector4f>> clanFactionColorSchemes;

    public static void init() {
        clanTypeColorSchemes = new HashMap<>();

        List<Vector4f> list = new ArrayList<>();
        list.add(new Vector4f(1f,0,0,1f));
        list.add(new Vector4f(0.5f,0,0,1f));
        list.add(new Vector4f(0.5f,0,0.5f,1f));
        list.add(new Vector4f(0, 0, 0, 1f));
        list.add(new Vector4f(0.25f, 0, 0.1f,1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_AGGRESSIVE, list);

        list = new ArrayList<>();
        list.add(new Vector4f(1f,0.85f,0,1f));
        list.add(new Vector4f(0.5f,0.3f,0,1f));
        list.add(new Vector4f(0.75f,0.25f,0,1f));
        list.add(new Vector4f(0.5f, 0.5f, 0.5f, 1f));
        list.add(new Vector4f(0.75f, 0.75f, 0.75f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_INDUSTRIOUS, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,1f,0,1f));
        list.add(new Vector4f(0,0.5f,0.5f,1f));
        list.add(new Vector4f(0, 0.5f, 0, 1f));
        list.add(new Vector4f(0.6f, 0.1f, 0.2f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_SETTLER, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,0,1f,1f));
        list.add(new Vector4f(0.5f,0f,0.5f,1f));
        list.add(new Vector4f(0, 0.25f, 0.25f, 1f));
        list.add(new Vector4f(0f, 0f, 0.25f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_TRADITIONAL, list);

        clanFactionColorSchemes = new HashMap<>();

        list = new ArrayList<>();
        list.add(new Vector4f(0,0,0,1f));
        list.add(new Vector4f(0.2f,0.2f,0.2f,1f));
        list.add(new Vector4f(0.5f, 0.5f, 0.5f, 1f));
        list.add(new Vector4f(0.5f, 0.25f, 0.25f, 1f));
        list.add(new Vector4f(0.25f, 0, 0, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_BARBARIAN, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0.5f,0,0,1f));
        list.add(new Vector4f(0.75f,0.75f,0.75f,1f));
        list.add(new Vector4f(0.8f, 0.8f, 1f, 1f));
        list.add(new Vector4f(0.4f, 0.5f, 0.6f, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_CIVILIZED, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,1f,0,1f));
        list.add(new Vector4f(0, 0.8f, 0f, 1f));
        list.add(new Vector4f(0,0.6f,0.2f,1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_FOREIGNER, list);

        list = new ArrayList<>();
        list.add(new Vector4f(1f, 0, 0, 1f));
        list.add(new Vector4f(1f,0.5f,0.5f,1f));
        list.add(new Vector4f(0.2f,0.3f,0.2f,1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_SAVAGE, list);
    }

    public static Clan randomClan() {
        Clan clan = null;
        while (clan == null) {
            clan = newClan(Clan.ClanType.random(), Clan.ClanFaction.random());
        }
        return clan;
    }

    public static Clan newClan(Clan.ClanType clanType, Clan.ClanFaction clanFaction) {
        /*Clan.ClanType clanType;
        Clan.ClanFaction clanFaction;*/
        List<Vector4f> colors = clanTypeColorSchemes.get(clanType);
        List<Vector4f> secondaryColors = clanFactionColorSchemes.get(clanFaction);
        Vector4f primaryColor, secondaryColor;
        if (colors.size() == 0 || secondaryColors.size() == 0) {
            return null;
        }
        else {
            primaryColor = colors.remove((int)(Math.random()*colors.size())).scaled(255f);
            secondaryColor = secondaryColors.remove((int)(Math.random()*colors.size())).scaled(255f);
        }
        /*switch (type) {
            case 0:
                clanType.
                break;
            default:
                System.err.println("Invalid clan type: " + type);
                return null;
        }*/
        Clan clan = new Clan("Clan" + clanType);
        clan.color = primaryColor;
        clan.reducedColor = primaryColor.scaled(0.7f);
        clan.secondaryColor = secondaryColor;
        clan.reducedSecondaryColor = secondaryColor.scaled(0.7f);
        clan.clanType = clanType;
        clan.clanFaction = clanFaction;
        return clan;
    }

}
