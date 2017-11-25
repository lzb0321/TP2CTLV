package org.ctlv.proxmox.manager;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.*;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;
	public Controller(ProxmoxAPI api){
		this.api = api;
	}
	
	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer) throws LoginException, JSONException, IOException  {
		String ctID;
		ctID = (String)this.api.getCTs(srcServer).get(0).getVmid();	//the CT who hold the first one will be considered as the elder one. 
		api.migrateCT(srcServer,ctID,dstServer);
	}

	// arr�ter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException {
		String ctID;
		ctID = (String)this.api.getCTs(server).get(0).getVmid();	//the CT who hold the first one will be considered as the elder one.
		api.stopCT(server,ctID);
	}
	
	public String getId() {
		return null;
	}
	
}
