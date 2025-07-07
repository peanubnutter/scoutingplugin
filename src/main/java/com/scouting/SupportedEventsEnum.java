package com.scouting;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum SupportedEventsEnum {

    ENT,
    FOX,
    PHEASANT,
    BEEHIVE,
    RITUAL,
    LEPRECHAUN,
    ROOTS,
    SAPLING,
    FLOWERS,
    IMP
    ;

    private static final Map<Integer, SupportedEventsEnum> npcIdToEventMap;
    private static final Map<Integer, SupportedEventsEnum> objectIdToEventMap;

    static {
        npcIdToEventMap = new HashMap<>();
        objectIdToEventMap = new HashMap<>();

        // Omitting PRUNED_ENTLING_12544 because it only signals the end of an event
        npcIdToEventMap.put(NpcID.ENTLING, ENT);

        npcIdToEventMap.put(NpcID.FOX_TRAP, FOX);
        npcIdToEventMap.put(NpcID.POACHER, FOX);
        npcIdToEventMap.put(NpcID.POACHER_12554, FOX);
        npcIdToEventMap.put(NpcID.POACHER_12555, FOX);
        npcIdToEventMap.put(NpcID.POACHER_12556, FOX);
        npcIdToEventMap.put(NpcID.POACHER_12557, FOX);
        npcIdToEventMap.put(NpcID.POACHER_12558, FOX);
        npcIdToEventMap.put(NpcID.FRIGHTENED_FOX, FOX);
        npcIdToEventMap.put(NpcID.FRIGHTENED_FOX_12560, FOX);

        // PHEASANT_12547 and PHEASANT_12549 are pets, not events!
        npcIdToEventMap.put(NpcID.FREAKY_FORESTER_12536, PHEASANT);
        npcIdToEventMap.put(NpcID.PHEASANT_12537, PHEASANT);
        objectIdToEventMap.put(ObjectID.PHEASANT_NEST, PHEASANT);
        objectIdToEventMap.put(ObjectID.PHEASANT_NEST_49937, PHEASANT);

        // Omitting FRIENDLY_BEES which also spawn during flowering bush event
        npcIdToEventMap.put(NpcID.BEE_KEEPER_12514, BEEHIVE);
        npcIdToEventMap.put(NpcID.UNFINISHED_BEEHIVE, BEEHIVE);
        npcIdToEventMap.put(NpcID.UNFINISHED_BEEHIVE_12516, BEEHIVE);
        npcIdToEventMap.put(NpcID.COMPLETE_BEEHIVE, BEEHIVE);
        npcIdToEventMap.put(NpcID.WILD_BEEHIVE, BEEHIVE);

        npcIdToEventMap.put(NpcID.DRYAD_12519, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_GREEN, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_YELLOW, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_BLUE, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_RED, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_GREEN_12524, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_YELLOW_12525, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_BLUE_12526, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_RED_12527, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_GREEN_12528, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_YELLOW_12529, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_BLUE_12530, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_RED_12531, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_GREEN_12532, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_YELLOW_12533, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_BLUE_12534, RITUAL);
        npcIdToEventMap.put(NpcID.RITUAL_CIRCLE_RED_12535, RITUAL);

        npcIdToEventMap.put(NpcID.WOODCUTTING_LEPRECHAUN, LEPRECHAUN);

        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_LILAC, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_PINK, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_RED, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_ORANGE, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_YELLOW, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_WHITE , FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_GREEN, FLOWERS);
        npcIdToEventMap.put(NpcID.FLOWERING_BUSH_BLUE, FLOWERS);

        //Add support for implings minus puro puro and the long list of IDs for crystal implings
        //Not sure what all the variations of crystal implings are about
        npcIdToEventMap.put(NpcID.BABY_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.YOUNG_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.GOURMET_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.EARTH_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.ESSENCE_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.ECLECTIC_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.NATURE_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.MAGPIE_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.NINJA_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.CRYSTAL_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.DRAGON_IMPLING, IMP);
        npcIdToEventMap.put(NpcID.LUCKY_IMPLING, IMP);

        objectIdToEventMap.put(ObjectID.TREE_ROOTS, ROOTS);
        objectIdToEventMap.put(ObjectID.ANIMAINFUSED_TREE_ROOTS, ROOTS);

        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING, SAPLING);
        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING_47485, SAPLING);
        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING_47487, SAPLING);
        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING_47488, SAPLING);
        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING_47490, SAPLING);
        objectIdToEventMap.put(ObjectID.STRUGGLING_SAPLING_47491, SAPLING);
        objectIdToEventMap.put(ObjectID.ROTTING_LEAVES, SAPLING);
        objectIdToEventMap.put(ObjectID.GREEN_LEAVES, SAPLING);
        objectIdToEventMap.put(ObjectID.DROPPINGS, SAPLING);
        objectIdToEventMap.put(ObjectID.WILD_MUSHROOMS, SAPLING);
        objectIdToEventMap.put(ObjectID.WILD_MUSHROOMS_47497, SAPLING);
        objectIdToEventMap.put(ObjectID.WILD_MUSHROOMS_47498, SAPLING);
        objectIdToEventMap.put(ObjectID.SPLINTERED_BARK, SAPLING);
    }

    public static SupportedEventsEnum findByNpcId(int npcId) {
        return npcIdToEventMap.get(npcId);
    }

    public static SupportedEventsEnum findByObjectId(int objectId) {
        return objectIdToEventMap.get(objectId);
    }

}
