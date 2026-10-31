package com.scouting.pathfinder;

import static com.scouting.pathfinder.CallLocation.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

/**
 * Black-box tests for {@link CallLocationFinder}, written from the specification and the
 * registration data only. The private registration map is inspected via reflection.
 */
public class CallLocationFinderTest
{
	/** {abbreviation, full name}, lowercase like all input passed to find(). */
	private static final String[][] DIRECTIONS = {
		{"n", "north"},
		{"ne", "northeast"},
		{"e", "east"},
		{"se", "southeast"},
		{"s", "south"},
		{"sw", "southwest"},
		{"w", "west"},
		{"nw", "northwest"},
	};

	// ------------------------------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------------------------------

	private static String describe(String input)
	{
		if (input == null)
		{
			return "null";
		}
		return "\"" + input + "\"";
	}

	private static List<CallLocation> list(String input)
	{
		Set<CallLocation> result = CallLocationFinder.find(input);
		assertNotNull("find(" + describe(input) + ") returned null", result);
		return new ArrayList<>(result);
	}

	/** Asserts the exact result, including iteration order. */
	private static void assertFinds(String input, CallLocation... expected)
	{
		assertEquals("find(" + describe(input) + ")", Arrays.asList(expected), list(input));
	}

	private static void assertEmpty(String input)
	{
		assertTrue("find(" + describe(input) + ") expected empty but was " + list(input), list(input).isEmpty());
	}

	private static void assertExcludes(String input, CallLocation... unexpected)
	{
		List<CallLocation> result = list(input);
		for (CallLocation location : unexpected)
		{
			assertFalse("find(" + describe(input) + ") must not contain " + location + " but was " + result,
				result.contains(location));
		}
	}

	private static final class Reg
	{
		final String base;
		final String direction;
		final List<CallLocation> locations;

		Reg(String base, String direction, List<CallLocation> locations)
		{
			this.base = base;
			this.direction = direction;
			this.locations = locations;
		}

		@Override
		public String toString()
		{
			return "(" + base + ", " + direction + ") -> " + locations;
		}
	}

	private static Object readField(Object target, String name) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	@SuppressWarnings("unchecked")
	private static Map<Object, Set<CallLocation>> rawRegistry() throws Exception
	{
		Field field = CallLocationFinder.class.getDeclaredField("LOCATION_KEYS");
		field.setAccessible(true);
		return (Map<Object, Set<CallLocation>>) field.get(null);
	}

	private static List<Reg> registry() throws Exception
	{
		List<Reg> result = new ArrayList<>();
		for (Map.Entry<Object, Set<CallLocation>> entry : rawRegistry().entrySet())
		{
			Object key = entry.getKey();
			assertNotNull("null registration key", key);
			Object base = readField(key, "base");
			Object direction = readField(key, "direction");
			result.add(new Reg(
				(String) base,
				direction == null ? null : ((Enum<?>) direction).name(),
				entry.getValue() == null ? null : new ArrayList<>(entry.getValue())));
		}
		return result;
	}

	private static List<String> registrySnapshot() throws Exception
	{
		List<String> snapshot = new ArrayList<>();
		for (Reg reg : registry())
		{
			snapshot.add(reg.toString());
		}
		return snapshot;
	}

	private static String[] directionRow(String enumName)
	{
		for (String[] row : DIRECTIONS)
		{
			if (row[0].equals(enumName.toLowerCase(Locale.ROOT)))
			{
				return row;
			}
		}
		throw new IllegalArgumentException(enumName);
	}

	// ------------------------------------------------------------------------------------------
	// Registration integrity and enum coverage (reflection)
	// ------------------------------------------------------------------------------------------

	@Test
	public void everyEnumConstantIsRegistered() throws Exception
	{
		EnumSet<CallLocation> registered = EnumSet.noneOf(CallLocation.class);
		for (Reg reg : registry())
		{
			if (reg.locations != null)
			{
				for (CallLocation location : reg.locations)
				{
					if (location != null)
					{
						registered.add(location);
					}
				}
			}
		}
		EnumSet<CallLocation> missing = EnumSet.allOf(CallLocation.class);
		missing.removeAll(registered);
		assertTrue("Enum constants missing from LOCATION_KEYS: " + missing, missing.isEmpty());
	}

	@Test
	public void registrationIntegrity() throws Exception
	{
		List<Reg> regs = registry();
		assertFalse("registry is empty", regs.isEmpty());
		for (Reg reg : regs)
		{
			assertNotNull("null key base: " + reg, reg.base);
			assertFalse("empty key base: " + reg, reg.base.isEmpty());
			assertEquals("key base not trimmed/lowercase: " + reg,
				reg.base.trim().toLowerCase(Locale.ROOT), reg.base);
			assertNotNull("null location set for base '" + reg.base + "'", reg.locations);
			assertFalse("empty location set: " + reg, reg.locations.isEmpty());
			for (CallLocation location : reg.locations)
			{
				assertNotNull("null location in " + reg, location);
			}
		}
	}

	/**
	 * Spaced shorthands ("rada e", "north seer") are derived by the parser, not registered. A
	 * registered one would also become a fuzzy candidate, so "rada n" could match "rada e".
	 */
	@Test
	public void directionlessKeysDoNotContainSpacedDirections() throws Exception
	{
		for (Reg reg : registry())
		{
			if (reg.direction != null)
			{
				continue;
			}
			for (String[] row : DIRECTIONS)
			{
				for (String token : row)
				{
					assertFalse("Registered spaced direction (derive it from an of(base, direction) key): " + reg,
						reg.base.startsWith(token + " ") || reg.base.endsWith(" " + token));
				}
			}
		}
	}

	@Test
	public void registryUsesInsertionOrderedSets() throws Exception
	{
		assertEquals(java.util.LinkedHashMap.class, rawRegistry().getClass());
		for (Set<CallLocation> set : rawRegistry().values())
		{
			assertEquals(LinkedHashSet.class, set.getClass());
		}
	}

	@Test
	public void everyDirectionlessKeyResolvesToAllItsLocations() throws Exception
	{
		for (Reg reg : registry())
		{
			if (reg.direction == null)
			{
				assertTrue("find(" + describe(reg.base) + ") should contain all of " + reg + " but was " + list(reg.base),
					list(reg.base).containsAll(reg.locations));
			}
		}
	}

	@Test
	public void everyDirectionalKeyResolvesInSpacedAbbreviationAndFullNameForms() throws Exception
	{
		for (Reg reg : registry())
		{
			if (reg.direction == null)
			{
				continue;
			}
			String[] row = directionRow(reg.direction);
			String[] inputs = {
				reg.base + " " + row[0],
				row[0] + " " + reg.base,
				reg.base + " " + row[1],
				row[1] + " " + reg.base,
			};
			for (String input : inputs)
			{
				assertTrue("find(" + describe(input) + ") should contain all of " + reg + " but was " + list(input),
					list(input).containsAll(reg.locations));
			}
		}
	}

	// ------------------------------------------------------------------------------------------
	// Explicit aliases (common, uncommon, obscure)
	// ------------------------------------------------------------------------------------------

	@Test
	public void commonAliases()
	{
		assertFinds("arceuus", ARCEUUS);
		assertFinds("arc", ARCEUUS);
		assertFinds("botd5", ARCEUUS);
		assertFinds("church", CHURCH);
		assertFinds("dray", DRAY);
		assertFinds("draynor", DRAY);
		assertFinds("dray oak", DRAY_OAK);
		assertFinds("draynor oak", DRAY_OAK);
		assertFinds("edge", EDGE);
		assertFinds("edgeville", EDGE);
		assertFinds("glade", GLADE);
		assertFinds("myth", MYTH);
		assertFinds("nemus", NEMUS);
		assertFinds("nemus s", NEMUS_S);
		assertFinds("snemus", NEMUS_S);
		assertFinds("nseer", NSEER);
		assertFinds("nseers", NSEER);
		assertFinds("nsorc", NSORC);
		assertFinds("nmage", NSORC);
		assertFinds("nwseer", NWSEER);
		assertFinds("nwseers", NWSEER);
		assertFinds("oasis", OASIS);
		assertFinds("ajp e", OASIS);
		assertFinds("pmage", PMAGE);
		assertFinds("prif mage", PMAGE);
		assertFinds("pteak", PTEAK);
		assertFinds("pmahog", PTEAK);
		assertFinds("prif teak", PTEAK);
		assertFinds("prif mahog", PTEAK);
		assertFinds("seer", SEER);
		assertFinds("seers", SEER);
		assertFinds("souls", SOULS_BJP_W);
		assertFinds("bjp w", SOULS_BJP_W);
		assertFinds("yak", YAK);
		assertFinds("yaks", YAK);
		assertFinds("zalc", ZALC);
		assertFinds("zalcano", ZALC);
	}

	@Test
	public void uncommonAliases()
	{
		assertFinds("ais e", AIS_E);
		assertFinds("ais sw", AIS_SW);
		assertFinds("ape", APE);
		assertFinds("apeatoll", APE);
		assertFinds("barb", BARB);
		assertFinds("ba", BARB);
		assertFinds("bkp w", BKP_W);
		assertFinds("camelot", CAMELOT);
		assertFinds("cam", CAMELOT);
		assertFinds("dray east", DRAY_EAST);
		assertFinds("draynor east", DRAY_EAST);
		assertFinds("dray e", DRAY_EAST);
		assertFinds("draynor e", DRAY_EAST);
		assertFinds("drum", DRUMSTICK);
		assertFinds("ds", DRUMSTICK);
		assertFinds("ecabbage", ECABBAGE);
		assertFinds("flax", FLAX);
		assertFinds("ge", GE);
		assertFinds("gorge", GORGE);
		assertFinds("heart", HEART);
		assertFinds("heart e", HEART_E);
		assertFinds("kingstown", HEART_E);
		assertFinds("kastori", KASTORI);
		assertFinds("kas", KASTORI);
		assertFinds("kast", KASTORI);
		assertFinds("lookout", LOOKOUT);
		assertFinds("mist maple yew", MIST_MAPLE_YEW);
		assertFinds("mist", MIST_MAPLE_YEW);
		assertFinds("mistrock", MIST_MAPLE_YEW);
		assertFinds("mta e", MTA_E);
		assertFinds("mta w", MTA_W);
		assertFinds("nconch", NCONCH);
		assertFinds("cjq", NCONCH);
		assertFinds("ne glade", NE_GLADE);
		assertFinds("neglade", NE_GLADE);
		assertFinds("nemus e", NEMUS_E);
		assertFinds("enemus", NEMUS_E);
		assertFinds("nemus sw", NEMUS_SW);
		assertFinds("swnemus", NEMUS_SW);
		assertFinds("nieve", NIEVE);
		assertFinds("nwald magic", NWALD_MAGIC);
		assertFinds("nwald", NWALD_MAGIC);
		assertFinds("ckq nw", NWALD_MAGIC);
		assertFinds("rada e", RADA_E);
		assertFinds("rada se", RADA_SE);
		assertFinds("rada w", RADA_W);
		assertFinds("rellekka sw", RELLEKKA_SW);
		assertFinds("relekka sw", RELEKKA_SW);
		assertFinds("sb", SUNBLEAK);
		assertFinds("sunbleak", SUNBLEAK);
		assertFinds("sun", SUNBLEAK);
		assertFinds("sunb", SUNBLEAK);
		assertFinds("botd3", BOTD3_SHAY);
		assertFinds("shay", BOTD3_SHAY);
		assertFinds("shayzien", BOTD3_SHAY);
		assertFinds("sorc", SORC);
		assertFinds("smage", SORC);
		assertFinds("sw conch", SW_CONCH);
		assertFinds("cjq sw", SW_CONCH);
		assertFinds("shore", SW_CONCH);
		assertFinds("teklan", TEKLAN);
		assertFinds("tek", TEKLAN);
		assertFinds("vale", VALE);
		assertFinds("vale e", VALE_E);
		assertFinds("ais n", VALE_E);
		assertFinds("vale s", VALE_S);
		assertFinds("vale w", VALE_W);
	}

	@Test
	public void obscureAliases()
	{
		assertFinds("ajp", AJP);
		assertFinds("ape w", APE_W);
		assertFinds("apeatoll w", APE_W);
		assertFinds("ardy monastry", ARDY_MONASTRY);
		assertFinds("ardy", ARDY_MONASTRY);
		assertFinds("battlefront", BATTLEFRONT);
		assertFinds("catherby", CATHERBY);
		assertFinds("cath", CATHERBY);
		assertFinds("champ", CHAMP);
		assertFinds("champion", CHAMP);
		assertFinds("champ e", CHAMP_E);
		assertFinds("champion e", CHAMP_E);
		assertFinds("corsair", CORSAIR);
		assertFinds("craft", CRAFT);
		assertFinds("cw east", CW_EAST);
		assertFinds("cw e", CW_EAST);
		assertFinds("fally gate", FALLY_GATE);
		assertFinds("fally", FALLY_GATE);
		assertFinds("falo", FALO);
		assertFinds("fortis pond n", FORTIS_POND_N);
		assertFinds("fortis pond e", FORTIS_POND_E);
		assertFinds("ge water fountain", GE_FOUNTAIN);
		assertFinds("ge fountain", GE_FOUNTAIN);
		assertFinds("grotto", GROTTO);
		assertFinds("gwennith", GWENNITH);
		assertFinds("hspirit", HSPIRIT);
		assertFinds("kastori n", KASTORI_N);
		assertFinds("kas n", KASTORI_N);
		assertFinds("kast n", KASTORI_N);
		assertFinds("kastori w", KASTORI_W);
		assertFinds("kas w", KASTORI_W);
		assertFinds("kast w", KASTORI_W);
		assertFinds("lgrave", LGRAVE);
		assertFinds("lletya", LLETYA);
		assertFinds("lum tree patch", LUM_TREE_PATCH);
		assertFinds("ltree patch", LUM_TREE_PATCH);
		assertFinds("ltree", LUM_TREE_PATCH);
		assertFinds("mos leharm", MOS_LEHARM);
		assertFinds("n glade", N_GLADE);
		assertFinds("nglade", N_GLADE);
		assertFinds("n tithe willow", N_TITHE_WILLOW);
		assertFinds("ntithe", N_TITHE_WILLOW);
		assertFinds("n yak", N_YAK);
		assertFinds("nald yew", NALD_YEW);
		assertFinds("nald", NALD_YEW);
		assertFinds("naldarin", NALD_YEW);
		assertFinds("ncabbage", NCABBAGE);
		assertFinds("nemus nw", NEMUS_NW);
		assertFinds("nwnemus", NEMUS_NW);
		assertFinds("phas", PHAS);
		assertFinds("pisc", PISC);
		assertFinds("ralos magics", RALOS_MAGICS);
		assertFinds("ralos", RALOS_MAGICS);
		assertFinds("rim", RIM);
		assertFinds("rimm", RIM);
		assertFinds("rimmington", RIM);
		assertFinds("rimm se", RIMM_SE);
		assertFinds("sarim", SARIM);
		assertFinds("scabbage", SCABBAGE);
		assertFinds("se gnome", SE_GNOME);
		assertFinds("se prif", SE_PRIF);
		assertFinds("sherlock", SHERLOCK);
		assertFinds("slegend", SLEGEND);
		assertFinds("slepe", SLEPE);
		assertFinds("slumber", SLUMBER);
		assertFinds("sw farm guild", SW_FARM_GUILD);
		assertFinds("sw farm", SW_FARM_GUILD);
		assertFinds("sw hos", SW_HOS);
		assertFinds("swlumber", SWLUMBER);
		assertFinds("tbw grove", TBW_GROVE);
		assertFinds("tbw", TBW_GROVE);
		assertFinds("tlati n", TLATI_N);
		assertFinds("tlati ne", TLATI_NE);
		assertFinds("uzer", UZER);
		assertFinds("vale n", VALE_N);
		assertFinds("varrock w bank", VARROCK_W_BANK);
		assertFinds("watson", WATSON);
		assertFinds("wc tutor", WC_TUTOR);
		assertFinds("wcg", WCG);
		assertFinds("wc guild", WCG);
		assertFinds("wlegend", WLEGEND);
		assertFinds("wlumber", WLUMBER);
		assertFinds("woodland nw", WOODLAND_NW);
		assertFinds("zanaris", ZANARIS);
	}

	// ------------------------------------------------------------------------------------------
	// Shared keys: complete result sets and iteration order
	// ------------------------------------------------------------------------------------------

	@Test
	public void sharedKeyFortis()
	{
		assertFinds("fortis", FORTIS_POND_N, FORTIS_POND_E);
	}

	@Test
	public void sharedKeyRpines()
	{
		assertFinds("rpines", RELLEKKA_SW, RELEKKA_SW);
	}

	@Test
	public void sharedKeysSincAndSinclair()
	{
		assertFinds("sinc", SINC, SINCLAIR);
		assertFinds("sinclair", SINC, SINCLAIR);
	}

	/** SINC's key(...) line comes before SINCLAIR's, so it is first in the shared set. */
	@Test
	public void sharedKeySinclairMansionFollowsRegistrationOrder()
	{
		assertFinds("sinclair mansion", SINC, SINCLAIR);
	}

	@Test
	public void sharedKeyCirW()
	{
		assertFinds("cir w", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("cir west", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("west cir", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("cirw", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("wcir", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("cir w mahog", CIR_W_MAHOG);
		assertFinds("cir w maple", CIR_W_MAPLE);
	}

	// ------------------------------------------------------------------------------------------
	// Directionless input
	// ------------------------------------------------------------------------------------------

	@Test
	public void directionlessSpecExamples()
	{
		assertFinds("seer", SEER);
		assertFinds("nemus", NEMUS);
		assertFinds("vale", VALE);
		assertFinds("cir", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("cabbage", ECABBAGE, NCABBAGE, SCABBAGE);
		assertFinds("rada", RADA_E, RADA_SE, RADA_W);
		assertFinds("lumber", SLUMBER, SWLUMBER, WLUMBER);
	}

	@Test
	public void directionlessDirectionalBaseVariants()
	{
		assertFinds("mage", NSORC, SORC);
		assertFinds("ais", AIS_E, AIS_SW, VALE_E);
		assertFinds("tlati", TLATI_N, TLATI_NE);
		assertFinds("legend", SLEGEND, WLEGEND);
		assertFinds("conch", NCONCH, SW_CONCH);
		assertFinds("ald", NWALD_MAGIC, NALD_YEW);
		assertFinds("fortis pond", FORTIS_POND_N, FORTIS_POND_E);
		assertFinds("rellekka", RELLEKKA_SW);
		assertFinds("relekka", RELEKKA_SW);
		assertFinds("cw", CW_EAST);
	}

	@Test
	public void exactDirectionlessKeySuppressesDirectionalVariants()
	{
		assertFinds("nemus", NEMUS);
		assertFinds("vale", VALE);
		assertFinds("glade", GLADE);
		assertFinds("heart", HEART);
		assertFinds("yak", YAK);
		assertFinds("ape", APE);
		assertFinds("kastori", KASTORI);
		assertFinds("dray", DRAY);
		assertFinds("rimm", RIM);
		assertFinds("cjq", NCONCH);
		assertFinds("ajp", AJP);
		assertFinds("seer", SEER);
		assertFinds("sorc", SORC);
	}

	@Test
	public void shortExactAliasesAreNotMisreadAsDirections()
	{
		assertFinds("ge", GE);
		assertFinds("ba", BARB);
		assertFinds("ds", DRUMSTICK);
		assertFinds("sb", SUNBLEAK);
		assertFinds("sun", SUNBLEAK);
		assertFinds("tek", TEKLAN);
		assertFinds("cam", CAMELOT);
		assertFinds("arc", ARCEUUS);
		assertFinds("kas", KASTORI);
		assertFinds("rim", RIM);
		assertFinds("wcg", WCG);
		assertFinds("nseer", NSEER);
		assertFinds("nwseer", NWSEER);
		assertFinds("snemus", NEMUS_S);
		assertFinds("enemus", NEMUS_E);
	}

	// ------------------------------------------------------------------------------------------
	// Direction parsing
	// ------------------------------------------------------------------------------------------

	@Test
	public void seerDirectionsAllForms()
	{
		for (String[] row : DIRECTIONS)
		{
			String abbr = row[0];
			String full = row[1];
			CallLocation expected;
			if (row[0].equals("n"))
			{
				expected = NSEER;
			}
			else if (row[0].equals("nw"))
			{
				expected = NWSEER;
			}
			else
			{
				continue;
			}
			assertFinds(abbr + " seer", expected);
			assertFinds("seer " + abbr, expected);
			assertFinds(full + " seer", expected);
			assertFinds("seer " + full, expected);
			assertFinds(abbr + "seer", expected);
			assertFinds("seer" + abbr, expected);
			assertFinds(full + "   seer", expected);
		}
	}

	/** SPEC: an explicit direction may fall back to the directionless registration only. */
	@Test
	public void allEightDirectionsFallBackToDirectionlessBase()
	{
		for (String[] row : DIRECTIONS)
		{
			String abbr = row[0];
			String full = row[1];
			assertFinds(abbr + " gorge", GORGE);
			assertFinds("gorge " + abbr, GORGE);
			assertFinds(full + " gorge", GORGE);
			assertFinds("gorge " + full, GORGE);
			assertFinds("gorge   " + abbr, GORGE);
		}
	}

	@Test
	public void nemusAllEightDirectionsSpacedAndFullName()
	{
		CallLocation[] expected = new CallLocation[DIRECTIONS.length];
		expected[0] = NEMUS;      // n  (no nemus-N registration)
		expected[1] = NEMUS;      // ne
		expected[2] = NEMUS_E;    // e
		expected[3] = NEMUS;      // se
		expected[4] = NEMUS_S;    // s
		expected[5] = NEMUS_SW;   // sw
		expected[6] = NEMUS;      // w
		expected[7] = NEMUS_NW;   // nw
		for (int i = 0; i < DIRECTIONS.length; i++)
		{
			String abbr = DIRECTIONS[i][0];
			String full = DIRECTIONS[i][1];
			assertFinds("nemus " + abbr, expected[i]);
			// Spaced prefix abbreviations n, ne and se are skipped: per the spec, the fuzzy NONE
			// match on the complete input (step 4) runs before the directionless-base fallback,
			// and "n nemus" / "ne nemus" / "se nemus" are within threshold of the registered
			// keys "nwnemus" / "snemus".
			if (!abbr.equals("n") && !abbr.equals("ne") && !abbr.equals("se"))
			{
				assertFinds(abbr + " nemus", expected[i]);
			}
			assertFinds("nemus " + full, expected[i]);
			assertFinds(full + " nemus", expected[i]);
		}
	}

	@Test
	public void attachedAbbreviationsOnRegisteredCombinations()
	{
		assertFinds("draye", DRAY_EAST);
		assertFinds("edray", DRAY_EAST);
		assertFinds("seern", NSEER);
		assertFinds("seernw", NWSEER);
		assertFinds("nemuss", NEMUS_S);
		assertFinds("nemusnw", NEMUS_NW);
		assertFinds("nemussw", NEMUS_SW);
		assertFinds("nemuse", NEMUS_E);
		assertFinds("radae", RADA_E);
		assertFinds("radase", RADA_SE);
		assertFinds("wrada", RADA_W);
		assertFinds("serada", RADA_SE);
		assertFinds("glade" + "ne", NE_GLADE);
		assertFinds("gladen", N_GLADE);
		assertFinds("ne" + "glade", NE_GLADE);
	}

	@Test
	public void attachedFullDirectionNamesAreDirections()
	{
		assertFinds("east dray", DRAY_EAST);
		assertFinds("dray east", DRAY_EAST);
		assertFinds("north seer", NSEER);
		assertFinds("seer northwest", NWSEER);
		assertFinds("northwest seer", NWSEER);
		// Attached full names
		assertFinds("northseer", NSEER);
		assertFinds("seernorth", NSEER);
		assertFinds("eastdray", DRAY_EAST);
		assertFinds("northwestseer", NWSEER);
		assertFinds("seernorthwest", NWSEER);
		assertFinds("southnemus", NEMUS_S);
		// No registered key for that direction, so it falls back to the directionless base
		assertFinds("northdray", DRAY);
	}

	@Test
	public void directionsOnlyRecognizedAtTheEnds()
	{
		assertExcludes("dray north oak", DRAY_EAST);
		assertExcludes("dray e oak", DRAY_EAST);
		assertExcludes("se north er", NSEER);
	}

	@Test
	public void directionOnlyInputIsRejected()
	{
		for (String[] row : DIRECTIONS)
		{
			assertEmpty(row[0]);
			assertEmpty(row[1]);
		}
	}

	@Test
	public void multipleValidParsesPickRegisteredCombination()
	{
		// "nwseer" is both nw+seer and n+wseer; "seernw" is both seer+nw and seern+w.
		assertFinds("nwseer", NWSEER);
		assertFinds("seernw", NWSEER);
		// "rada se" / "radase": se+rada vs s+erada, etc.
		assertFinds("rada se", RADA_SE);
		assertFinds("se rada", RADA_SE);
		assertFinds("tlati ne", TLATI_NE);
		assertFinds("ne tlati", TLATI_NE);
		assertFinds("glade ne", NE_GLADE);
		assertFinds("gladene", NE_GLADE);
	}

	// ------------------------------------------------------------------------------------------
	// Matching priorities and direction-specific fallback
	// ------------------------------------------------------------------------------------------

	@Test
	public void explicitDirectionNeverReturnsOtherDirectionalVariants()
	{
		assertEmpty("rada n");
		assertEmpty("rada s");
		assertEmpty("rada ne");
		assertEmpty("n rada");
		assertEmpty("cir n");
		assertEmpty("cir e");
		assertEmpty("north cir");
		assertExcludes("rada sw", RADA_E, RADA_SE, RADA_W);
		assertExcludes("mage e", NSORC, SORC);
		assertExcludes("mage w", NSORC, SORC);
		assertFinds("mage n", NSORC);
		assertFinds("mage s", SORC);
	}

	@Test
	public void explicitDirectionFallsBackToDirectionlessKeyOnly()
	{
		assertFinds("dray w", DRAY);
		assertFinds("west dray", DRAY);
		assertFinds("dray n", DRAY);
		assertFinds("ndray", DRAY);
		assertFinds("heart w", HEART);
		assertFinds("nemus w", NEMUS);
		assertFinds("seer e", SEER);
		assertFinds("seer s", SEER);
	}

	@Test
	public void exactDirectionalMatchBeatsDirectionlessBaseForSpacedInput()
	{
		assertFinds("seer n", NSEER);
		assertFinds("seer nw", NWSEER);
		assertFinds("dray e", DRAY_EAST);
		assertFinds("nemus s", NEMUS_S);
	}

	@Test
	public void separateCandidateKeysAreNeverCombined()
	{
		assertFinds("nemus", NEMUS);
		assertFinds("seer n", NSEER);
		assertFinds("seers n", NSEER);
		assertFinds("dray w", DRAY);
		assertFinds("dray e", DRAY_EAST);
		assertFinds("kastori", KASTORI);
		assertFinds("kastori n", KASTORI_N);
	}

	@Test
	public void directionlessBaseReturnsEveryVariantOfWinningBase()
	{
		assertFinds("rada", RADA_E, RADA_SE, RADA_W);
		assertFinds("cabbage", ECABBAGE, NCABBAGE, SCABBAGE);
		assertFinds("lumber", SLUMBER, SWLUMBER, WLUMBER);
		// fuzzy directional-base fallback returns every variant of the winning base
		assertFinds("radaa", RADA_E, RADA_SE, RADA_W);
		assertFinds("cabbag", ECABBAGE, NCABBAGE, SCABBAGE);
		assertFinds("lumbr", SLUMBER, SWLUMBER, WLUMBER);
	}

	// ------------------------------------------------------------------------------------------
	// Fuzzy matching
	// ------------------------------------------------------------------------------------------

	@Test
	public void observedInGameAndRealWorldTypos()
	{
		assertFinds("seerr", SEER);
		assertFinds("edgevile", EDGE);
		assertFinds("zanari", ZANARIS);
		assertFinds("sleep", SLEPE);
		assertFinds("drayy", DRAY);
		assertFinds("gwenith", GWENNITH);
		assertFinds("batlefron", BATTLEFRONT);
		assertFinds("kastroi", KASTORI);
		assertFinds("mistrk", MIST_MAPLE_YEW);
		assertFinds("glad", GLADE);
		assertFinds("mith", MYTH);
		assertFinds("letya", LLETYA);
		assertFinds("mos lehram", MOS_LEHARM);
		assertFinds("fortiss", FORTIS_POND_N, FORTIS_POND_E);
		assertFinds("cabage", ECABBAGE, NCABBAGE, SCABBAGE);
	}

	@Test
	public void insertionDeletionSubstitutionTransposition()
	{
		assertFinds("zanarisx", ZANARIS);   // insertion
		assertFinds("zanari", ZANARIS);     // deletion
		assertFinds("zanarix", ZANARIS);    // substitution
		assertFinds("zanairs", ZANARIS);    // transposition (cost 2, within the length-7 threshold)
		assertFinds("drazy", DRAY);         // insertion, length-4 candidate
		assertFinds("drax", DRAY);          // substitution, length-4 candidate
		assertFinds("nexus", NEMUS);        // substitution, length-5 candidate
	}

	@Test
	public void candidateLengthThresholdBoundaries()
	{
		// 4-5 chars: max distance 1
		assertFinds("drax", DRAY);
		assertEmpty("drxx");
		assertEmpty("draxz");
		assertFinds("nexus", NEMUS);
		assertEmpty("nemuxy");
		assertEmpty("nievexy");
		// 6-8 chars: max distance 2
		assertFinds("zanxxis", ZANARIS);
		assertEmpty("zanxxxs");
		// 9+ chars: max distance 3
		assertFinds("edgxvxlxe", EDGE);
		assertEmpty("edgxvxxxe");
		assertFinds("grottoxy", GROTTO);            // 6-char candidate, 2 edits
		assertEmpty("grottoxyz");                   // 6-char candidate, 3 edits
		assertFinds("catherbyxy", CATHERBY);        // 8-char candidate, 2 edits
		assertEmpty("catherbyxyz");                 // 8-char candidate, 3 edits
		assertFinds("mos leharmxyz", MOS_LEHARM);   // 10-char candidate, 3 edits
		assertEmpty("mos leharmwxyz");              // 10-char candidate, 4 edits
	}

	@Test
	public void minimumOriginalInputLength()
	{
		assertEmpty("see");   // one edit from "seer" but only 3 characters
		assertEmpty("dra");
	}

	@Test
	public void minimumCandidateLength()
	{
		// "wcg" and "arc" are 3 characters and never fuzzy matched.
		assertEmpty("wcgx");
		assertEmpty("arcx");
	}

	@Test
	public void firstCharacterMustMatch()
	{
		assertEmpty("cray");                  // one edit from "dray"
		assertExcludes("lave", VALE);         // anagram of "vale", different first letter
		assertExcludes("avle", VALE);
	}

	@Test
	public void anagramsBypassDistanceThreshold()
	{
		assertFinds("sleep", SLEPE);   // distance 2 > threshold 1, but an anagram
		assertFinds("nemsu", NEMUS);
		assertFinds("vael", VALE);
		assertFinds("drya", DRAY);
		assertFinds("zanairs", ZANARIS);
	}

	@Test
	public void repeatedCharacterToleranceCollapsesInputOnly()
	{
		assertFinds("drrayy", DRAY);
		assertFinds("seerr", SEER);
		assertFinds("sleeep", SLEPE);
		assertFinds("sarrim", SARIM);
		// Candidates are never collapsed: the registered double letters stay exact.
		assertFinds("rellekka", RELLEKKA_SW);
		assertFinds("relekka", RELEKKA_SW);
	}

	@Test
	public void directionSpecificFuzzyMatching()
	{
		assertFinds("draay e", DRAY_EAST);
		assertFinds("seerrs n", NSEER);
		assertFinds("seerrs nw", NWSEER);
		assertFinds("seerr n", NSEER);
		assertFinds("seerr nw", NWSEER);
		// A fuzzy base never crosses to another direction's variant.
		assertFinds("seerr e", SEER);
		assertFinds("north seerrs", NSEER);
		assertFinds("northwest seerrs", NWSEER);
	}

	@Test
	public void directionTokenItselfIsNeverFuzzyMatched()
	{
		assertExcludes("nrth seer", NSEER);
		assertExcludes("nroth seer", NSEER);
		assertExcludes("norht seer", NSEER);
		assertExcludes("seer nrth", NSEER);
	}

	/**
	 * "fall" is distance 1 from both "falo" (FALO) and "fally" (FALLY_GATE). Keys are registered in
	 * source order and FALO's line comes before FALLY_GATE's (FALO is called more often), so FALO wins the tie.
	 */
	@Test
	public void registrationOrderBreaksFuzzyTies()
	{
		assertFinds("fall", FALO);
	}

	// ------------------------------------------------------------------------------------------
	// Additional fuzzy and direction coverage
	// ------------------------------------------------------------------------------------------

	@Test
	public void adjacentInputReachesParsedFuzzyStep()
	{
		// "dryae" is an anagram of "draye"; only the parsed directional interpretation resolves it.
		assertFinds("dryae", DRAY_EAST);
	}

	@Test
	public void spacedAnagramWithDirection()
	{
		assertFinds("vael n", VALE_N);
	}

	@Test
	public void thresholdBoundariesAtLengthsSixEightAndTen()
	{
		assertFinds("heartx", HEART);               // 5-char candidate, 1 edit
		assertFinds("grottoxy", GROTTO);            // 6-char candidate, 2 edits
		assertEmpty("grottoxyz");                   // 6-char candidate, 3 edits
		assertFinds("catherbyxy", CATHERBY);        // 8-char candidate, 2 edits
		assertEmpty("catherbyxyz");                 // 8-char candidate, 3 edits
		assertFinds("mos leharmxyz", MOS_LEHARM);   // 10-char candidate, 3 edits
		assertEmpty("mos leharmwxyz");              // 10-char candidate, 4 edits
	}

	@Test
	public void shortCandidatesRejectTwoEdits()
	{
		assertEmpty("draxz");
		assertEmpty("nievexy");
	}

	@Test
	public void repeatedCharacterCollapseCombinedWithAnEdit()
	{
		// "heartxx" collapses to "heartx", which is one edit from "heart".
		assertFinds("heartxx", HEART);
	}

	@Test
	public void minimumLengthAndFirstCharacterNegatives()
	{
		assertEmpty("drr");
		assertEmpty("baxx");
		assertEmpty("deer");
	}

	@Test
	public void directionalBaseOnlyExactInputs()
	{
		assertFinds("bjp", SOULS_BJP_W);
		assertFinds("woodland", WOODLAND_NW);
	}

	@Test
	public void attachedFullDirectionNamesFallBackToDirectionlessBase()
	{
		assertFinds("drayeast", DRAY_EAST);
		assertFinds("gladenorth", N_GLADE);
		assertFinds("eastglade", GLADE);
	}

	@Test
	public void directionInTheMiddleReturnsEmpty()
	{
		assertEmpty("draymiddlex");
		assertEmpty("seer n middlex");
	}

	@Test
	public void extraDirectionalForms()
	{
		assertFinds("seers nw", NWSEER);
		assertFinds("netlati", TLATI_NE);
		assertFinds("tlatine", TLATI_NE);
		assertFinds("wmta", MTA_W);
		assertFinds("mtaw", MTA_W);
		assertFinds("segnome", SE_GNOME);
		assertFinds("gnomese", SE_GNOME);
		assertFinds("cabbage n", NCABBAGE);
		assertFinds("cabbage e", ECABBAGE);
		assertFinds("cabbage s", SCABBAGE);
		assertFinds("w cir", CIR_W_MAHOG, CIR_W_MAPLE);
		assertFinds("dray north", DRAY);
		assertFinds("north dray", DRAY);
		assertFinds("w dray", DRAY);
	}

	// ------------------------------------------------------------------------------------------
	// Result safety
	// ------------------------------------------------------------------------------------------

	@Test
	public void nullEmptyAndUnknownInputReturnEmptySet()
	{
		assertEmpty(null);
		assertEmpty("");
		assertEmpty("xyzzy");
		assertEmpty("qwertyuiop");
		assertEmpty("???");
		assertEmpty("zzzzzzzz plugh");
	}

	@Test
	public void resultIsAFreshLinkedHashSet()
	{
		String[] inputs = {"seer", "cir", "rada", "xyzzy", "", "dray w"};
		for (String input : inputs)
		{
			Set<CallLocation> first = CallLocationFinder.find(input);
			Set<CallLocation> second = CallLocationFinder.find(input);
			assertNotNull(first);
			assertTrue(first instanceof LinkedHashSet);
			assertNotSame("find(" + describe(input) + ") reused a result instance", first, second);
		}
		assertTrue(CallLocationFinder.find(null) instanceof LinkedHashSet);
	}

	@Test
	public void mutatingResultDoesNotAffectFutureLookups() throws Exception
	{
		List<String> before = registrySnapshot();
		String[] inputs = {"seer", "cir", "rada", "sinclair mansion", "fortis", "dray w", "seerr", "lumber"};
		for (String input : inputs)
		{
			List<CallLocation> expected = list(input);
			Set<CallLocation> result = CallLocationFinder.find(input);
			result.clear();
			result.add(GE);
			result.add(ZANARIS);
			assertEquals("find(" + describe(input) + ") changed after caller mutated a result",
				expected, list(input));
		}
		assertEquals("registry changed after lookups", before, registrySnapshot());
	}

	@Test
	public void repeatedLookupsAreIdentical() throws Exception
	{
		for (Reg reg : registry())
		{
			assertEquals(list(reg.base), list(reg.base));
		}
		String[] inputs = {"seerr", "radaa", "lumbr", "fall", "sleep", "north seerrs", "xyzzy", "ndray"};
		for (String input : inputs)
		{
			List<CallLocation> first = list(input);
			for (int i = 0; i < 5; i++)
			{
				assertEquals(first, list(input));
			}
		}
	}
}
