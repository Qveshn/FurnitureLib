package de.Ste3et_C0st.FurnitureLib.main;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import de.Ste3et_C0st.FurnitureLib.Events.FurnitureBreakEvent;
import de.Ste3et_C0st.FurnitureLib.Events.FurnitureClickEvent;
import de.Ste3et_C0st.FurnitureLib.main.entity.fArmorStand;

public abstract class Furniture extends FurnitureHelper implements Listener{
	public Furniture(ObjectID id){super(id);}
	public fArmorStand spawnArmorStand(Location loc){return getManager().createArmorStand(getObjID(), loc);}
	public abstract void spawn(Location location);
	public abstract void onFurnitureBreak(FurnitureBreakEvent e);
	public abstract void onFurnitureClick(FurnitureClickEvent e);
}
