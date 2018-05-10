package de.Ste3et_C0st.FurnitureLib.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import de.Ste3et_C0st.FurnitureLib.Utilitis.RandomStringGenerator;
import de.Ste3et_C0st.FurnitureLib.main.Type.EventType;
import de.Ste3et_C0st.FurnitureLib.main.Type.PublicMode;
import de.Ste3et_C0st.FurnitureLib.main.Type.SQLAction;
import de.Ste3et_C0st.FurnitureLib.main.entity.fArmorStand;
import de.Ste3et_C0st.FurnitureLib.main.entity.fEntity;
import de.Ste3et_C0st.FurnitureLib.Crafting.Project;

public class ObjectID{
	private FurnitureManager manager = FurnitureLib.getInstance().getFurnitureManager();
	private String ObjectID, serial, Project, plugin;
	private HashSet<Location> locList = new HashSet<Location>();
	private Location loc;
	private UUID uuid;
	private HashSet<UUID> uuidList = new HashSet<UUID>();
	private PublicMode publicMode = FurnitureLib.getInstance().getDefaultPublicType();
	private EventType memberType = FurnitureLib.getInstance().getDefaultEventType();
	private SQLAction sqlAction = SQLAction.SAVE;
	private List<fEntity> packetList = new ArrayList<fEntity>();
	private HashSet<Player> players = new HashSet<Player>();
	private boolean finish=false, fixed=false, fromDatabase=false, Private=false;
	public String getID(){return this.ObjectID;}
	public String getProject(){return this.Project;}
	public Project getProjectOBJ(){return FurnitureLib.getInstance().getFurnitureManager().getProject(getProject());}
	public String getPlugin(){return this.plugin;}
	public String getSerial(){return this.serial;}
	public Location getStartLocation(){return this.loc;}
	public EventType getEventType(){return this.memberType;}
	public SQLAction getSQLAction(){return this.sqlAction;}
	public boolean isFixed(){return this.fixed;}
	public boolean isFinish() {return this.finish;}
	public void setFinish(){this.finish = true;}
	public void setEventTypeAccess(EventType type){this.memberType = type;}
	public void setSQLAction(SQLAction action){this.sqlAction=action;}
	public void setFixed(boolean b){fixed=b;}
	public void setMemberList(HashSet<UUID> uuidList){this.uuidList=uuidList;}
	public HashSet<UUID> getMemberList(){return this.uuidList;}
	public PublicMode getPublicMode(){return this.publicMode;}
	public UUID getUUID(){return this.uuid;}
	public World getWorld(){return this.loc.getWorld();}
	public Chunk getChunk(){return this.loc.getChunk();}
	public HashSet<Player> getPlayerList(){return this.players;}
	public boolean isMember(UUID uuid) {return uuidList.contains(uuid);}
	public void setFromDatabase(boolean b){this.fromDatabase=b;}
	public boolean isFromDatabase(){return this.fromDatabase;}
	public boolean isPrivate(){return this.Private;}
	public void addMember(UUID uuid){uuidList.add(uuid);}
	public void remMember(UUID uuid){uuidList.remove(uuid);}
	public List<fEntity> getPacketList() {return packetList;}
	public void setPacketList(List<fEntity> packetList) {this.packetList = packetList;}
	public boolean isInRange(Player player) {return (getStartLocation().distance(player.getLocation()) <= viewDistance);}
	public boolean isInWorld(Player player) {return getStartLocation().getWorld().equals(player.getLocation().getWorld());}
	public void addArmorStand(fEntity packet) {packetList.add(packet);}
	public void setPublicMode(PublicMode publicMode){this.publicMode = publicMode;}
	public void setPrivate(boolean b){this.Private = b;}
	public int viewDistance = 100;
	
	public void setStartLocation(Location loc) {this.loc = loc;}
	
	public void updatePlayerView(Player player){
		if(manager.getIgnoreList().contains(player.getUniqueId())){return;}
		if(isPrivate()){return;}
		if(getPacketList().isEmpty()){return;}
		if(getSQLAction().equals(SQLAction.REMOVE)){return;}
		if(!isInWorld(player)){
			if(players.contains(player)) players.remove(player);
			return;}
		if(isInRange(player)){
			if(players.contains(player)){return;}
			for(fEntity stand : getPacketList()){stand.send(player);}
			//player.sendMessage("In Range:" + getStartLocation().distance(player.getLocation()));
			players.add(player);
		}else{
			if(!players.contains(player)) return;
			
			for(fEntity stand : getPacketList()){stand.kill(player, false);}
			players.remove(player);
		}
	}
	
	public void removePacket(Player p){
		if(isPrivate()){return;}
		if(getPacketList().isEmpty()){return;}
		if(getSQLAction().equals(SQLAction.REMOVE)){return;}
		for(fEntity stand : getPacketList()){stand.kill(p, false);}
		players.remove(p);
	}
	
	
	public void setUUID(UUID uuid){
		if(this.uuid!=null&&!this.uuid.equals(uuid)){
			if(FurnitureLib.getInstance().getLimitManager()!=null){
				FurnitureLib.getInstance().getLimitManager().removePlayer(this);
			}
		}
		if(uuid!=null){
			if(FurnitureLib.getInstance().getLimitManager()!=null){
				FurnitureLib.getInstance().getLimitManager().addPlayer(this.uuid, this);
			}
		}
		this.uuid=uuid;
	}
	
	public void setID(String s){
		this.ObjectID = s;
		try{
			if(s.contains(":")){
				String[] l = s.split(":");
				this.Project=l[0];
				this.serial=l[1];
				this.plugin=l[2];
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ObjectID(String name, String plugin, Location startLocation){
		try {
			this.Project = name;
			this.plugin = plugin;
			this.serial = RandomStringGenerator.generateRandomString(10,RandomStringGenerator.Mode.ALPHANUMERIC);
			this.ObjectID = name+":"+this.serial+":"+plugin;
			if(startLocation!=null){this.loc = startLocation;}
			this.viewDistance = FurnitureLib.getInstance().getViewDistance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addBlock(List<Block> bl){
		if(bl==null||bl.isEmpty()){return;}
		for(Block b : bl){
			FurnitureLib.getInstance().getBlockManager().addBlock(b);
			this.locList.add(b.getLocation());
		}
	}

	
	public void remove(Player p){
		for(fEntity entity : getPacketList()){
			entity.setFire(false);
		}
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
		Location loc = getStartLocation();
		dropItem(p, loc.clone().add(0, 1, 0), getProjectOBJ());
		deleteEffect(packetList);
		FurnitureLib.getInstance().getBlockManager().destroy(getBlockList(), false);
		removeAll();
		locList.clear();
		manager.remove(this);
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
	}
	
	public void remove(){
		if(!this.packetList.isEmpty()){
			for(fEntity entity : packetList){
				if(entity.isFire()){
					entity.setFire(false);
				}
			}
		}
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
		deleteEffect(packetList);
		FurnitureLib.getInstance().getBlockManager().destroy(getBlockList(), false);
		removeAll();
		locList.clear();
		manager.remove(this);
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
	}
	
	public void remove(boolean effect){
		if(!this.packetList.isEmpty()){
			for(fEntity entity : packetList){
				if(entity.isFire()){
					entity.setFire(false);
				}
			}
		}
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
		if(effect){deleteEffect(packetList);}
		FurnitureLib.getInstance().getBlockManager().destroy(getBlockList(), false);
		removeAll();
		locList.clear();
		manager.remove(this);
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
	}
	
	public void remove(Player p,boolean dropItem, boolean deleteEffect){
		for(fEntity entity : getPacketList()){
			entity.setFire(false);
		}
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
		Location loc = getStartLocation();
		if(dropItem) dropItem(p, loc.clone().add(0, 1, 0), getProjectOBJ());
		if(deleteEffect) deleteEffect(packetList);
		removeAll();
		manager.remove(this);
		FurnitureLib.getInstance().getLimitManager().removePlayer(this);
	}
	
	public void dropItem(Player p, Location loc, Project porject){
		if(FurnitureLib.getInstance().useGamemode()&&p.getGameMode().equals(GameMode.CREATIVE)){return;}
		World w = loc.getWorld();
		w.dropItemNaturally(loc, porject.getCraftingFile().getRecipe().getResult());
	}
	
	public void deleteEffect(List<fEntity> asList){
		int i = 0;
		if(!getProjectOBJ().isSilent()){
			try{
				if(asList==null||asList.isEmpty()) return;
				 for (fEntity packet : asList) {
					if(packet!=null && packet instanceof fArmorStand){
						if(packet.getInventory() != null && packet.getInventory().getHelmet()!=null){
							if(packet.getInventory().getHelmet().getType()!=null&&!packet.getInventory().getHelmet().getType().equals(Material.AIR)){
								if(i<6){
									packet.getLocation().getWorld().playEffect(packet.getLocation(), Effect.STEP_SOUND, packet.getInventory().getHelmet().getType());
									i++;
								}else{break;}
							}
						}
					}
				 }
			}catch(Exception e){}
			if(i==0){
				for (fEntity packet : asList) {
					if(i<6){
						packet.getLocation().getWorld().playEffect(packet.getLocation(), Effect.STEP_SOUND, Material.WOOD);
						i++;
					}else{break;}
				}
			}
		}
	}
	
	public String getPlayerName(){
		String name = "§cUNKNOW";
		if(uuid!=null){
			OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
			name = p.getName();
		}
		return name;
	}
	public HashSet<Location> getBlockList() {
		return this.locList;
	}
	
	public void send(Player p){updatePlayerView(p);}
	public void sendAll(){for(Player p : Bukkit.getOnlinePlayers()) send(p);}
	public void update() {
		if(isPrivate()){return;}
		if(getPacketList().isEmpty()){return;}
		if(getSQLAction().equals(SQLAction.REMOVE)){return;}
		for(Player p : getPlayerList()){
			for(fEntity stand : getPacketList()){stand.update(p);}
		}
	}
	
	public void removeAll(){
		if(isPrivate()){return;}
		if(getPacketList().isEmpty()){return;}
		if(getSQLAction().equals(SQLAction.REMOVE)){return;}
		for(Player p : getPlayerList()){
			for(fEntity stand : getPacketList()){stand.kill(p, false);}
		}
		this.players.clear();
	}
}
