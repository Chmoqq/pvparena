package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * spawn manager class
 * 
 * -
 * 
 * provides commands to deal with spawns
 * 
 * @author slipcor
 * 
 * @version v0.6.40
 * 
 */

public class Spawns {
	private static Debug db = new Debug(35);

	/**
	 * get the location from a coord string
	 * 
	 * @param place
	 *            the coord string
	 * @return the location of that string
	 */
	public static Location getCoords(Arena arena, String place) {
		db.i("get coords: " + place);
		World world = Bukkit.getWorld(arena.cfg.getString("general.world",
				Bukkit.getWorlds().get(0).getName()));
		if (place.equals("spawn") || place.equals("powerup")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: " + name);
				}
			}

			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		} else if (arena.cfg.get("spawns." + place) == null) {
			String type = null;
			if (arena.cfg.getBoolean("arenatype.flags")) {
				type = arena.getType();
				type = type.equals("pumpkin") ? type : "flag";
			}
			if (!place.contains("spawn") && type == null) {
				db.i("place not found!");
				return null;
			}
			// no exact match: assume we have multiple spawnpoints
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			db.i("searching for team spawns");

			HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
					.getYamlConfiguration().getConfigurationSection("spawns")
					.getValues(false);
			for (String name : coords.keySet()) {
				if (name.startsWith(place)) {
					locs.put(i++, name);
					db.i("found match: " + name);
				}
				if (type == null) {
					continue;
				}
				if (name.endsWith(type)) {
					for (String sTeam : arena.paTeams.keySet()) {
						if (name.startsWith(sTeam)) {
							locs.put(i++, name);
							db.i("found match: " + name);
						}
					}
				}
			}

			if (locs.size() < 1) {
				return null;
			}
			Random r = new Random();

			place = locs.get(r.nextInt(locs.size()));
		}

		String sLoc = arena.cfg.getString("spawns." + place, null);
		db.i("parsing location: " + sLoc);
		return Config.parseLocation(world, sLoc);
	}

	/**
	 * set an arena coord to a player's position
	 * 
	 * @param player
	 *            the player saving the coord
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setCoords(Arena arena, Player player, String place) {
		// "x,y,z,yaw,pitch"

		Location location = player.getLocation();

		Integer x = location.getBlockX();
		Integer y = location.getBlockY();
		Integer z = location.getBlockZ();
		Float yaw = location.getYaw();
		Float pitch = location.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		db.i("setting spawn " + place + " to " + s.toString());

		arena.cfg.set("spawns." + place, s);

		arena.cfg.save();
	}

	/**
	 * set an arena coord to a given location
	 * 
	 * @param loc
	 *            the location to save
	 * @param place
	 *            the coord name to save the location to
	 */
	public static void setCoords(Arena arena, Location loc, String place) {
		// "x,y,z,yaw,pitch"

		Integer x = loc.getBlockX();
		Integer y = loc.getBlockY();
		Integer z = loc.getBlockZ();
		Float yaw = loc.getYaw();
		Float pitch = loc.getPitch();

		String s = x.toString() + "," + y.toString() + "," + z.toString() + ","
				+ yaw.toString() + "," + pitch.toString();

		db.i("setting spawn " + place + " to " + s.toString());

		arena.cfg.set("spawns." + place, s);

		arena.cfg.save();
	}

	/**
	 * is a player near a spawn?
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player to check
	 * @param diff
	 *            the distance to check
	 * @return true if the player is near, false otherwise
	 */
	public static boolean isNearSpawn(Arena arena, Player player, int diff) {
		db.i("checking if arena is near a spawn");
		if (!Players.isPartOf(arena, player)) {
			return false;
		}
		if (Players.getTeam(player).equals("")) {
			return false;
		}

		HashSet<Location> spawns = getSpawns(arena, Players.getTeam(player));

		for (Location loc : spawns) {
			if (loc.distance(player.getLocation()) <= diff) {
				db.i("found near spawn: " + loc.toString());
				return true;
			}
		}

		return false;
	}

	/**
	 * get all (team) spawns of an arena
	 * 
	 * @param arena
	 *            the arena to check
	 * @param sTeam
	 *            a team name or "flags" or "[team]pumpkin" or "[team]flag"
	 * @return a set of possible spawn matches
	 */
	public static HashSet<Location> getSpawns(Arena arena, String sTeam) {
		db.i("reading spawns of arena " + arena + " (" + sTeam + ")");
		HashSet<Location> result = new HashSet<Location>();

		HashMap<String, Object> coords = (HashMap<String, Object>) arena.cfg
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		World world = Bukkit.getWorld(arena.getWorld());
		for (String name : coords.keySet()) {
			if (sTeam.equals("flags")) {
				if (!name.startsWith("flag")) {
					continue;
				}
			} else if (name.endsWith("flag") || name.endsWith("pumpkin")) {
				String sName = sTeam.replace("flag", "");
				sName = sName.replace("pumpkin", "");
				db.i("checking if " + name + " starts with " + sName);
				if (!name.startsWith(sName)) {
					continue;
				}
			} else if (sTeam.equals("free")) {
				if (!name.startsWith("spawn")) {
					continue;
				}
			} else if (name.contains("lounge") || !name.contains(sTeam)) {
				continue;
			}
			db.i(" - " + name);
			String sLoc = arena.cfg.getString("spawns." + name, null);
			result.add(Config.parseLocation(world, sLoc));
		}

		return result;
	}

	/**
	 * calculate the arena center, including all team spawn locations
	 * 
	 * @param arena
	 * @return
	 */
	public static Location getRegionCenter(Arena arena) {
		HashSet<Location> locs = new HashSet<Location>();

		for (String sTeam : arena.paTeams.keySet()) {
			for (Location loc : getSpawns(arena, sTeam)) {
				locs.add(loc);
			}
		}

		Vector v = new Vector(0, 0, 0);

		for (Location loc : locs) {
			v.add(loc.toVector());
		}

		v.multiply(1 / locs.size());

		return v.toLocation(Bukkit.getWorld(arena.getWorld()));
	}

	public static Location getNearest(HashSet<Location> spawns,
			Location location) {
		Location result = null;
		
		for (Location loc : spawns) {
			if (result == null || result.distance(location) > loc.distance(location)) {
				result = loc;
			}
		}
		
		return result;
	}
}