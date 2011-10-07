package craftyn.pvparena;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/*
 * PlayerListener class
 * 
 * author: craftyn
 * editor: slipcor
 * 
 * version: v0.0.0a - code tweaks
 * 
 * history:
 * 		v0.0.0 - copypaste
 */

public class PAPlayerListener extends PlayerListener {
	public PVPArena plugin;

	public PAPlayerListener(PVPArena instance) {
		this.plugin = instance;
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		if (!(PVPArena.fightUsersRespawn.containsKey(player.getName())))
			return;
		Location l = PVPArena.getCoords("spectator");
		event.setRespawnLocation(l);
		PVPArena.fightUsersRespawn.remove(player.getName());
		PVPArena.setInventory(player);
		PVPArena.loadPlayer(player, "spectator");
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (PVPArena.fightUsersTeam.containsKey(player.getName())) {
			if (PVPArena.fightUsersTeam.get(player.getName()) == "red") {
				PVPArena.redTeam -= 1;
				this.plugin.tellEveryoneExcept(player,
						ChatColor.RED + player.getName() + ChatColor.WHITE
								+ " has left the fight!");
			} else {
				PVPArena.blueTeam -= 1;
				this.plugin.tellEveryoneExcept(player,
						ChatColor.BLUE + player.getName() + ChatColor.WHITE
								+ " has left the fight!");
			}
			if (PVPArena.checkEnd())
				return;
			PVPArena.removePlayer(player);
		}
	}

	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (PVPArena.fightUsersTeam.containsKey(player.getName())) {
			player.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
					+ ChatColor.WHITE + "Not so fast! No Cheating!");
			event.setCancelled(true);
		}
	}

	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		if ((!(PVPArena.fightUsersTeam.containsKey(player.getName())))
				|| (PVPArena.fightTelePass.containsKey(player.getName())))
			return;
		event.setCancelled(true);
		PVPArena.tellPlayer(player, "Please use '/pa leave' to exit the fight!");
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		event.getAction();
		if ((event.getAction() == Action.LEFT_CLICK_BLOCK)
				&& ((((PVPArena.Permissions == null) && (player.isOp())) || ((PVPArena.Permissions != null)
						&& (PVPArena.Permissions.has(player, "admin"))
						&& (player.getItemInHand().getTypeId() == PVPArena.wand)))) && (PVPArena.regionmodify)) {
			PVPArena.pos1 = event.getClickedBlock().getLocation();
			PVPArena.tellPlayer(player, "First position set.");
		}

		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& ((((PVPArena.Permissions == null) && (player.isOp())) || ((PVPArena.Permissions != null)
						&& (PVPArena.Permissions.has(player, "admin"))
						&& (player.getItemInHand().getTypeId() == PVPArena.wand)))) && (PVPArena.regionmodify)) {
			PVPArena.pos2 = event.getClickedBlock().getLocation();
			PVPArena.tellPlayer(player, "Second position set.");
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();

				if ((PVPArena.fightClasses.containsKey(sign.getLine(0)))
						&& (PVPArena.fightUsersTeam.containsKey(player
								.getName()))) {
					PVPArena.fightSigns.put(player.getName(), sign);

					if (PVPArena.fightUsersClass.containsKey(player.getName())) {
						if (PVPArena.fightUsersClass.get(player.getName()) == sign
								.getLine(0)) {
							PVPArena.fightUsersClass.remove(player.getName());
							if (sign.getLine(2) == player.getName()) {
								sign.setLine(2, "");
								sign.update();
								PVPArena.clearInventory(player);
							} else if (sign.getLine(3) == player.getName()) {
								sign.setLine(3, "");
								sign.update();
								PVPArena.clearInventory(player);
							} else {
								player.sendMessage(ChatColor.YELLOW
										+ "[PVP Arena] "
										+ ChatColor.WHITE
										+ "Please tell developer about this bug (#5017).");
							}
						} else {
							player.sendMessage(ChatColor.YELLOW
									+ "[PVP Arena] "
									+ ChatColor.WHITE
									+ "You must first remove yourself from the other class!");
						}

					} else if (sign.getLine(2).trim().equals("")) {
						PVPArena.fightUsersClass.put(player.getName(),
								sign.getLine(0));
						sign.setLine(2, player.getName());
						sign.update();
						PVPArena.giveItems(player);
					} else if (sign.getLine(3).trim().equals("")) {
						PVPArena.fightUsersClass.put(player.getName(),
								sign.getLine(0));
						sign.setLine(3, player.getName());
						sign.update();
						PVPArena.giveItems(player);
					} else {
						player.sendMessage(ChatColor.YELLOW
								+ "[PVP Arena] "
								+ ChatColor.WHITE
								+ "There are too many of this class, pick another class.");
					}
				}
			}
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();

			if ((block.getTypeId() == 42)
					&& (PVPArena.fightUsersTeam.containsKey(player.getName()))
					&& (PVPArena.teamReady((String) PVPArena.fightUsersTeam
							.get(player.getName())))) {
				String color = (String) PVPArena.fightUsersTeam.get(player
						.getName());

				if (color == "red") {
					PVPArena.redTeamIronClicked = true;
					PVPArena.tellEveryone(ChatColor.RED + "Red "
							+ ChatColor.WHITE + "team is ready!");

					if ((PVPArena.teamReady("blue"))
							&& (PVPArena.blueTeamIronClicked)) {
						this.plugin.teleportAllToSpawn();
						PVPArena.fightInProgress = true;
						PVPArena.tellEveryone("Let the fight begin!");
					}

				} else if (color == "blue") {
					PVPArena.blueTeamIronClicked = true;
					PVPArena.tellEveryone(ChatColor.BLUE + "Blue "
							+ ChatColor.WHITE + "team is ready!");

					if ((PVPArena.teamReady("red"))
							&& (PVPArena.redTeamIronClicked)) {
						this.plugin.teleportAllToSpawn();
						PVPArena.fightInProgress = true;
						PVPArena.tellEveryone("Let the fight begin!");
					}

				}

			} else if ((block.getTypeId() == 42)
					&& (PVPArena.fightUsersTeam.containsKey(player.getName()))) {
				player.sendMessage(ChatColor.YELLOW + "[PVP Arena] "
						+ ChatColor.WHITE
						+ "Not all of your team has picked a class!");
			}
		}
	}
}