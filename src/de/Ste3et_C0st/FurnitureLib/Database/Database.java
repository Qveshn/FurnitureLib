package de.Ste3et_C0st.FurnitureLib.Database;

import java.io.EOFException;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import de.Ste3et_C0st.FurnitureLib.main.FurnitureLib;
import de.Ste3et_C0st.FurnitureLib.main.ObjectID;
import de.Ste3et_C0st.FurnitureLib.main.Type.DataBaseType;
import de.Ste3et_C0st.FurnitureLib.main.Type.SQLAction;

public abstract class Database {
	FurnitureLib plugin;
    Connection connection;
    Statement statement;
    CallBack callBack;
    boolean result = false;
    public Database(FurnitureLib instance){
        this.plugin = instance;
    }
    
    public abstract Connection getSQLConnection();

    public abstract void load();
    public abstract DataBaseType getType();
    
    public boolean isExist(String s){
    	try{
    		boolean query = statement.execute("SELECT * FROM `"+s+"`");
    		if(query){return true;}
    	}catch(Exception e){
    		return false;
    	}
    	return false;
    }
    
    public void initialize(){
        connection = getSQLConnection();
        try{
        	statement = connection.createStatement();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM FurnitureLib_Objects");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }
    
    public boolean save(ObjectID id){
    	String binary = FurnitureLib.getInstance().getSerializer().SerializeObjectID(id);
    	String query = "REPLACE INTO FurnitureLib_Objects (`ObjID`,`Data`) VALUES ('" + id.getID() + "', '" + binary + "');";
    	try{
    		statement.executeUpdate(query);
    		return true;
    	}catch(Exception e){
    		if(e instanceof SocketException || e instanceof EOFException){
    			initialize();
    			try{
    				statement.executeUpdate(query);
    			}catch(Exception ex){
    				ex.printStackTrace();
    			}
    			return false;
    		}
    		e.printStackTrace();
    	}
    	return false;
    }

    public void loadAll(final SQLAction action, final CallBack callBack){
    	final long time1 = System.currentTimeMillis();
    	final boolean b = FurnitureLib.getInstance().isAutoPurge();
    	this.callBack = callBack;
    	// Here was error. callBack2 must be initialized before loadFurnitures,
		// because loadFurnitures could call callBack2 very fast if database is empty
		// This is optional changes, because we will make single-thread database loading. See loadFurnitures.
    	this.callBack2 = new CallBack() {
			@Override
			public void onResult(boolean b) {
				if(b){
					plugin.getLogger().info("FurnitureLib load " + FurnitureLib.getInstance().getFurnitureManager().getObjectList().size()  +  " Objects from: " + getType().name() + " Database");
		        	long time2 = System.currentTimeMillis();
		        	long newTime = time2-time1;
		        	SimpleDateFormat time = new SimpleDateFormat("mm:ss.SSS");
		        	String timeStr = time.format(newTime);
		        	int ArmorStands = FurnitureLib.getInstance().getDeSerializer().armorStands;
		        	int purged = FurnitureLib.getInstance().getDeSerializer().purged;
		        	plugin.getLogger().info("FurnitureLib have loadet " + ArmorStands + " in " +timeStr);
		        	plugin.getLogger().info("FurnitureLib have purged " + purged + " Objects");
		        	for(ObjectID id : FurnitureLib.getInstance().getFurnitureManager().getObjectList()){
		        		id.sendAll();
		        	}
		        	
		        	callBack.onResult(true);
				}
			}
		};
		loadFurnitures(0, b, action);
    }
    

    private CallBack callBack2;
    
    public boolean loadFurnitures(final int i, final boolean b, final SQLAction action){
    	// It is very hard to organize "parallel" reading database in another thread.
		// 1. Server can begin shutdown before loading database is finished
		// 2. Packets must be sended via spigot main thread: plugin.getServer().getScheduler().runTask
		// 3. Is it it important how long will start?
		// 4. Anyway all data will be loaded in memory at the end
		// Therefore I've made single-thread loading =(
    	String query = "SELECT * FROM FurnitureLib_Objects";
		try{
			ResultSet rs = statement.executeQuery(query);
			while (rs.next()){
				FurnitureLib.getInstance().getDeSerializer().Deserialze(rs.getString(1), rs.getString(2), action, b);
			}
			rs.close();
		}catch(Exception ex){
			// We can't start plugin if error has occurred
			throw new RuntimeException("Error while reading database =(", ex);
		}
		result = true;
		callBack2.onResult(true);
		return true;
//    	if(result) return false;
//    	new Thread(new Runnable() {
//    		@Override
//			public void run() {
//    			try{
//    				int count = FurnitureLib.getInstance().getStepSize();
//    				int offset = i, j = 0;
//        			String query = "SELECT * FROM FurnitureLib_Objects LIMIT " + count + " OFFSET " + offset;
//        			ResultSet rs = statement.executeQuery(query);
//        			while (rs.next()){
//        				FurnitureLib.getInstance().getDeSerializer().Deserialze(rs.getString(1), rs.getString(2), action, b);
//        				j++;
//        			}
//        			if(!rs.next()){
//		    			rs.close();
//		    			if(j != count){
//		    				result = true;
//		    				callBack2.onResult(true);
//		    				return;
//		    			}else{
//		    				loadFurnitures(i + count, b, action);
//		    				return;
//		    			}
//		    		}
//    			}catch(Exception ex){
//    				ex.printStackTrace();
//    			}
//    		}
//    	}).start();
//    	return false;
	}

    public void delete(ObjectID objID){
    	try {
    		statement.execute("DELETE FROM FurnitureLib_Objects WHERE ObjID = '" + objID.getID() + "'");
		} catch (Exception e) {
    		if(e instanceof SocketException || e instanceof EOFException){
    			initialize();
    			try{
    				statement.execute("DELETE FROM FurnitureLib_Objects WHERE ObjID = '" + objID.getID() + "'");
    			}catch(Exception ex){
    				ex.printStackTrace();
    			}
    			return;
    		}
    		e.printStackTrace();
		}
    }
 

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null) ps.close();
            if (rs != null) rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void close(){
    	try {
			connection.close();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}