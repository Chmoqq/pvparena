package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;

/**
 * <pre>Arena Runnable class "PlayerReset"</pre>
 * 
 * An arena timer to reset a player
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PlayerResetRunnable implements Runnable {
	private final ArenaPlayer p;
	private Debug db = new Debug(40);
	private int id;
	private Location loc;

	/**
	 * create a timed arena runnable
	 * 
	 * @param p
	 *            the player to reset
	 */
	public PlayerResetRunnable(ArenaPlayer p, int i, Location loc) {
		id = 0;
		this.p = p;
		this.loc = loc;
		db.i("PlayerResetRunnable constructor");
		String removeme = "";
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("PlayerResetRunnable commiting");
		//p.getArena().playerLeave(p.get(), "lose");
		Bukkit.getScheduler().cancelTask(id);
		p.get().teleport(loc);
	}
	
	public void setId(int i) {
		id = i;
	}
}