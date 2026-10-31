package com.scouting.pathfinder;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

/**
 * Named places an ent can be at, for the overlay. Names, centres and quick teleports come from the
 * Discord bot's predefined locations.
 * <p>
 * Source: <a href="https://github.com/eX-C0n/logbot-forestry">https://github.com/eX-C0n/logbot-forestry</a>
 * (forestryLocations.js, PREDEFINEDLOCATIONS). Keep this enum in sync with it;
 */
public enum CallLocation
{
	// Common Locations
	ARCEUUS("Arceuus", 1683, 3746, "Kharedst's Memoirs 5 / Book of the Dead 5"),
	CHURCH("Church", 2716, 3460, "Seers' Village tele > Diary Cape 5"),
	DRAY("Dray", 3087, 3235, "Diary Cape G > Glory 3"),
	DRAY_OAK("Dray oak", 3101, 3243, "Glory 3 > Diary Cape G"),
	EDGE("Edge", 3088, 3474, "Glory 1"),
	GLADE("Glade", 1751, 3566, "Xeric's Talisman 2 > Con Cape 5"),
	MYTH("Myth", 2444, 2845, "Myth Cape", Quest.DRAGON_SLAYER_II),
	NEMUS_S("Nemus s", 1383, 3274, "Pendant of Ates (6, Nemus Retreat) > Southwest of Fairy Ring - AIS > Quetzal (Quetzacalli Gorge), follow Western Mountain Guide then run South", Quest.CHILDREN_OF_THE_SUN),
	NEMUS("Nemus", 1371, 3308, "Pendant of Ates (6, Nemus Retreat) run North > Quetzal (Quetzacalli Gorge), follow Western Mountain Guide > West of Fairy Ring - AIS", Quest.CHILDREN_OF_THE_SUN),
	NSEER("Nseer", 2729, 3502, "Seers' Village tele > Diary Cape 5"),
	NSORC("Nsorc", 2693, 3424, "Kandarin Headgear 4 > Combat Bracelet 4"),
	NWSEER("Nwseer", 2702, 3511, "Seers' Village tele"),
	OASIS("Oasis/AJP e", 1697, 2990, "Fairy ring AJP east", Quest.CHILDREN_OF_THE_SUN),
	PMAGE("Pmage", 3232, 6099, "Con Cape A", Quest.SONG_OF_THE_ELVES),
	PTEAK("Pteak", 3307, 6125, "Spirit Tree 6", Quest.SONG_OF_THE_ELVES),
	SEER("Seer", 2725, 3480, "Seers' Village tele"),
	SOULS_BJP_W("Souls/BJP w", 2189, 2991, "Fairy Ring BJP"),
	YAK("Yak", 2322, 3789, "Enchanted Lyre 3 + Elite Fremennik diary", Quest.THE_FREMENNIK_TRIALS),
	ZALC("Zalc", 3291, 6065, "Eternal Teleport Crystal Prifddinas > Con Cape A", Quest.SONG_OF_THE_ELVES),

	// Uncommon Locations
	AIS_E("Ais e", 1455, 3314, "East of Fairy Ring - AIS > Southeast of Quetzal (Auburnvale) across bridge", Quest.CHILDREN_OF_THE_SUN),
	AIS_SW("Ais sw", 1400, 3305, "Southwest of Fairy Ring - AIS > Pendant of Ates (6,Nemus) run East across bridge", Quest.CHILDREN_OF_THE_SUN),
	APE("Ape", 2774, 2699, "Ape Atoll Dungeon tele", Quest.MONKEY_MADNESS_I),
	BARB("Barb", 2519, 3568, "Games Necklace 5"),
	BKP_W("BKP w", 2335, 3047, "Fairy Ring BKP"),
	CAMELOT("Camelot", 2751, 3467, "Camelot tele > Diary Cape 5"),
	CIR_W_MAHOG("CIR W Mahog", 1230, 3767, "CIR West > Skills Necklace 6"),
	CIR_W_MAPLE("CIR W Maple", 1293, 3754, "CIR fairy ring"),
	DRAY_EAST("Dray East", 3142, 3251, "Glory Dray east > Diary G east"),
	DRUMSTICK("Drum", 2146, 3548, 120, "Tele to Boat (w/ Greater Teleport) docked at Drumstick", Quest.PANDEMONIUM),
	ECABBAGE("Ecabbage", 3082, 3294, "Explorer's Ring > Spirit Tree 7"),
	FLAX("Flax", 2758, 3431, "Diary Cape 5 > Kandarin Headgear 4"),
	GE("Ge", 3208, 3503, "Ring of Wealth 2 > Spirit Tree 4"),
	GORGE("Gorge", 1499, 3237, "Quetzal > Pendant: Darkfrost", Quest.CHILDREN_OF_THE_SUN),
	HEART("Heart", 1628, 3673, "Xeric's talisman 4 / Kourend tele"),
	HEART_E("Heart E/Kingstown", 1672, 3659, "Xeric's talisman 4 / Kourend tele"),
	KASTORI("Kastori", 1327, 3026, "Quetzal (Kastori) > Pendant of Ates (5, Kastori) run sw", Quest.CHILDREN_OF_THE_SUN),
	LOOKOUT("Lookout", 1554, 3549, "Xeric's Talisman 1"),
	MIST_MAPLE_YEW("Mist Maple/Yew", 1380, 2876, "Quetzal > Aldarin POH > CKQ Fairy Ring", Quest.CHILDREN_OF_THE_SUN),
	MTA_E("Mta e", 3370, 3309, "Ring of the Elements 4 > Ring of Dueling 1"),
	MTA_W("Mta w", 3355, 3315, "Ring of the Elements 4 > Ring of Dueling 1"),
	NCONCH("NConch", 3200, 2479, "Fairy CJQ / Charter ship to Summer Shore / Tele to boat docked at Summer Shore", Quest.THE_RED_REEF),
	NE_GLADE("Ne glade", 1798, 3601, "Xeric's Talisman 2"),
	NEMUS_E("Nemus e", 1390, 3305, "Pendant of Ates (6, Nemus Retreat) run East > Fairy ring - AIS run Southwest across bridge", Quest.CHILDREN_OF_THE_SUN),
	NEMUS_SW("Nemus sw", 1349, 3280, "Pendant of Ates (6, Nemus Retreat) > Quetzal (Gorge) follow western Mountain Guide then run South", Quest.CHILDREN_OF_THE_SUN),
	NIEVE("Nieve", 2433, 3423, "Slayer Ring 1 > Diary Cape F > Spirit Tree 2"),
	NWALD_MAGIC("Nwald Magic", 1323, 2974, "CKQ Fairy Ring > Pendant of Ates > Aldarin POH", Quest.CHILDREN_OF_THE_SUN),
	RADA_E("Rada e", 1610, 3442, "Rada Blessing Kourend Woodland"),
	RADA_SE("Rada se", 1594, 3423, "Rada Blessing Kourend Woodland"),
	RADA_W("Rada w", 1529, 3449, "Rada Blessing Kourend Woodland"),
	// loghunters has both
	RELLEKKA_SW("Rellekka SW", 2606, 3627, "Rellekka POH > Enchanted lyre 1"),
	RELEKKA_SW("Relekka SW", 2596, 3634, "Frem POH west > Lyre Relekka west"),
	SUNBLEAK("SB", 2206, 2320, 120, "Tele to Boat (w/ Greater Teleport) docked at Sunbleak", Quest.PANDEMONIUM),
	BOTD3_SHAY("Botd3 (Shay)", 1498, 3582, "Kharedst's Memoirs 3 / Book of the Dead 3"),
	SORC("Sorc", 2702, 3398, "Kandarin Headgear 4"),
	SW_CONCH("SW Conch", 3110, 2412, "Fairy CJQ -> use shortcut -> run south-west / Charter ship to Summer Shore / Tele to boat (w/Greater Teleport) docked at Summer Shore", Quest.TROUBLED_TORTUGANS),
	TEKLAN("Teklan", 1246, 3076, "Quetzal (Tal Teklan) run East then South > Pendant of Ates (5, Kastori) run West", Quest.CHILDREN_OF_THE_SUN),
	VALE("Vale", 1412, 3366, "Quetzal (Auburnvale) > Northwest of Fairy Ring - AIS, at the Quetz.", Quest.CHILDREN_OF_THE_SUN),
	VALE_E("Vale e", 1442, 3355, "Quetzal (Auburnvale) run East > North of Fairy Ring - AIS", Quest.CHILDREN_OF_THE_SUN),
	VALE_S("Vale s", 1400, 3337, "Quetzal (Auburnvale) run South > Northwest of Fairy Ring - AIS, South of Quetz", Quest.CHILDREN_OF_THE_SUN),
	VALE_W("Vale w", 1367, 3355, "Quetzal (Auburnvale) run West > Northwest of Fairy Ring - AIS, West of Quetz", Quest.CHILDREN_OF_THE_SUN),

	// Obscure locations
	AJP("AJP", 1647, 3007, "Fairy ring AJP", Quest.CHILDREN_OF_THE_SUN),
	APE_W("Ape w", 2721, 2722, "Ape Atoll Dungeon tele", Quest.MONKEY_MADNESS_I),
	ARDY_MONASTRY("Ardy Monastry", 2620, 3201, "Ardougne cloak"),
	BATTLEFRONT("Battlefront", 1353, 3740, "Battlefront tele > Fairy Ring CIR"),
	CATHERBY("Catherby", 2791, 3449, "Catherby teleport > Camelot teleport south east"),
	CHAMP("Champ", 3201, 3360, "Chronicle > Combat Bracelet"),
	CHAMP_E("Champ e", 3251, 3364, "Chronicle east > Combat bracelet 3 east"),
	CORSAIR("Corsair", 2475, 2887, "Spirit Tree 5", Quest.THE_CORSAIR_CURSE),
	CRAFT("Craft", 2937, 3301, "Skills Necklace 3"),
	CW_EAST("CW east", 2462, 3094, "Dueling ring east"),
	FALLY_GATE("Fally gate", 3006, 3312, "Ring of the Elements 1 > Ring of Wealth 3"),
	FALO("Falo", 2683, 3555, "Music cape > Seers north"),
	FORTIS_POND_N("Fortis Pond N", 1731, 3120, "Quetzal Colosseum > Civitas illa Fortis teleport > Quetzal S Fortis Cothon", Quest.CHILDREN_OF_THE_SUN),
	FORTIS_POND_E("Fortis Pond E", 1750, 3106, "Quetzal Colosseum > Civitas illa Fortis teleport > Quetzal S Fortis Cothon", Quest.CHILDREN_OF_THE_SUN),
	GE_FOUNTAIN("Ge water fountain", 3193, 3473, "Grand Exchange Teleport east and south"),
	GROTTO("Grotto", 2499, 3491, "Max Cape Otto's Grotto / Fishing Cape Otto's Grotto"),
	GWENNITH("Gwennith", 2210, 3426, "Spirit Tree 6", Quest.SONG_OF_THE_ELVES),
	HSPIRIT("Hspirit", 1692, 3546, "Spirit Tree A > Skills Necklace 5 NE"),
	KASTORI_N("Kastori n", 1328, 3065, "North of Quetzal (Kastori) > Pendant of Ates (5, Kastori) run w", Quest.CHILDREN_OF_THE_SUN),
	KASTORI_W("Kastori w", 1261, 3003, "West of Quetzal (Kastori) > Pendant of Ates (5, Kastori) run far sw", Quest.CHILDREN_OF_THE_SUN),
	LGRAVE("Lgrave", 3249, 3201, "Diary Cape B > Lumbridge tele"),
	LLETYA("Lletya", 2286, 3141, "Crystal Teleport Seed", Quest.MOURNINGS_END_PART_I),
	LUM_TREE_PATCH("Lum tree patch", 3185, 3229, "Lumbridge tele > Diary Cape B"),
	MOS_LEHARM("Mos LeHarm", 3812, 3059, "No teleports", Quest.CABIN_FEVER),
	N_GLADE("N Glade", 1755, 3602, "Glade Xeric talisman"),
	N_TITHE_WILLOW("N Tithe Farm Willow", 1805, 3528, "AKR Fairy Ring SW > Glade Xeric SE > Minigame Tele(slow)"),
	N_YAK("North Yak", 2320, 3816, "Enchanted Lyre(i) Neitiznot", Quest.THE_FREMENNIK_TRIALS),
	NALD_YEW("Nald Yew", 1412, 2993, "Pendant of Ates > Aldarin POH > CKQ Fairy Ring", Quest.CHILDREN_OF_THE_SUN),
	NCABBAGE("Ncabbage", 3040, 3320, "Explorer's Ring > Spirit Tree 7"),
	NEMUS_NW("Nemus nw", 1337, 3340, "Pendant of Ates (6, Nemus Retreat) run North > Quetzal (Quetzacalli Gorge) follow Western Mountain Guide then run Northwest", Quest.CHILDREN_OF_THE_SUN),
	PHAS("Phas", 3676, 3446, "Ectophial", Quest.PRIEST_IN_PERIL),
	PISC("Pisc", 2319, 3610, "Fairy Ring AKQ"),
	RALOS_MAGICS("Ralos Magics", 1398, 3170, "Quetzal (Cam Torum) run South off mountain then far Northwest", Quest.CHILDREN_OF_THE_SUN),
	RIM("Rim", 2938, 3231, "Con Cape 2"),
	RIMM_SE("Rimm SE", 2971, 3202, "Rim POH south east"),
	SARIM("Sarim", 3060, 3254, "Spirit Tree 7 > Explorer's Ring > Glory 3"),
	SCABBAGE("Scabbage", 3056, 3271, "Explorer's Ring > Spirit Tree 7"),
	SE_GNOME("Se gnome", 2490, 3407, "Slayer Ring 1 > Diary Cape F > Spirit Tree 2"),
	SE_PRIF("Se prif", 3301, 6031, "Eternal Teleport Crystal Prifddinas > Con Cape A", Quest.SONG_OF_THE_ELVES),
	SHERLOCK("Sherlock", 2734, 3416, "Kandarin headgear"),
	// loghunters has both
	SINC("Sinc/Sinclair Mansion", 2720, 3551, "Fairy ring CJR > Music cape"),
	SINCLAIR("Sinclair Mansion", 2745, 3564, "Fairy ring CJR > Music cape"),
	SLEGEND("Slegend", 2733, 3334, "Quest Cape > Fairy Ring BLR"),
	SLEPE("Slepe", 3675, 3380, "Drakan's Slepe / Ver Sinhaza > Ectophial south", Quest.PRIEST_IN_PERIL),
	SLUMBER("Slumber", 3303, 3470, "Ring of the Elements 3 > Lumberyard tele > Digsite Pendant 1"),
	SW_FARM_GUILD("Sw farm guild", 1223, 3699, "Skills Necklace 6"),
	SW_HOS("Sw hos", 1712, 3488, "Con Cape 5 > Xeric's Talisman 2"),
	SWLUMBER("Swlumber", 3270, 3473, "Ring of the Elements 3 > Lumberyard tele > Digsite Pendant 1"),
	TBW_GROVE("TBW Hardwood Grove", 2829, 3086, "Tai Bwo Wannai Teleport + 100 Trading Sticks"),
	TLATI_N("Tlati n", 1324, 3171, "Pendant of Ates (5, Kastori) run North across bridge > Quetzal (Tal Teklan) run North then East", Quest.CHILDREN_OF_THE_SUN),
	TLATI_NE("Tlati ne", 1365, 3139, "Pendant of Ates (5, Kastori) North across bridge then East > Quetzal (Tal Teklan) run North then far East", Quest.CHILDREN_OF_THE_SUN),
	UZER("Uzer", 3511, 3076, "Fairy ring DLQ > Necklace of passage 3"),
	VALE_N("Vale n", 1388, 3383, "Quetzal (Auburnvale) run North > Northwest of Fairy Ring - AIS, North of Quetz", Quest.CHILDREN_OF_THE_SUN),
	VARROCK_W_BANK("Varrock W Bank", 3168, 3423, "GE / Varrock Tele > Skull Sceptre"),
	WATSON("Watson", 1651, 3594, "Watson scroll > botd1/xeric's heart"),
	WC_TUTOR("WC tutor", 3230, 3244, "Lumbridge teleport north"),
	WCG("Wcg", 1641, 3531, "Skills Necklace 5 NW> Spirit Tree A"),
	WLEGEND("Wlegend", 2708, 3367, "Quest Cape > Kandarin Headgear 4"),
	WLUMBER("Wlumber", 3268, 3496, "Ring of the Elements 3 > Lumberyard tele > Digsite Pendant 1"),
	WOODLAND_NW("Woodland NW", 1531, 3475, "Rada woodland north/west"),
	ZANARIS("Zanaris", 2415, 4463, "Fairy Ring-Zanaris / Fairy Ring BK", Quest.LOST_CITY);

	//TODO: Update radius and method to match loghunters discord bot
	/** Default match radius of a forestry event */
	private static final int DEFAULT_RADIUS_TILES = 30;

	@Getter
	private final String callout;
	@Getter
	private final int centreX;
	@Getter
	private final int centreY;
	private final int radiusTiles;

	/** Quick-teleport instructions from the Discord bot. */
	@Getter
	private final String quickTele;

	/** Quest that must be completed to reach this location, or null if there is none. */
	@Getter
	private final Quest requiredQuest;

	/** True if this place has no quest gate, or the player has finished it. */
	boolean isUnlocked(Client client)
	{
		return requiredQuest == null || requiredQuest.getState(client) == QuestState.FINISHED;
	}

	CallLocation(String callout, int centreX, int centreY, int radiusTiles, String quickTele, Quest requiredQuest)
	{
		this.callout = callout;
		this.centreX = centreX;
		this.centreY = centreY;
		this.radiusTiles = radiusTiles;
		this.quickTele = quickTele;
		this.requiredQuest = requiredQuest;
	}

	CallLocation(String callout, int centreX, int centreY, String quickTele, Quest requiredQuest)
	{
		this(callout, centreX, centreY, DEFAULT_RADIUS_TILES, quickTele, requiredQuest);
	}

	CallLocation(String callout, int centreX, int centreY, String quickTele)
	{
		this(callout, centreX, centreY, DEFAULT_RADIUS_TILES, quickTele, null);
	}

	/** The nearest place to a coordinate, or null if none is close enough. Radii overlap, so nearest wins. */
	static CallLocation nearest(int x, int y)
	{
		CallLocation best = null;
		int bestDistance = Integer.MAX_VALUE;

		for (CallLocation location : values())
		{
			// Game measures Chebyshev distance, but Euclidean distance is still more intuitive for "nearest call location".
			double dx = x - location.centreX;
			double dy = y - location.centreY;
			int distance = (int) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			if (distance <= location.radiusTiles && distance < bestDistance)
			{
				best = location;
				bestDistance = distance;
			}
		}
		return best;
	}
}
