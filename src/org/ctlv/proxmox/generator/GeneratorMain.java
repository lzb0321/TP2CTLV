package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

import org.ctlv.proxmox.manager.*;

public class GeneratorMain {
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
		System.out.println("hello");
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;
		
		int ind=0; //name index, from 0 to the end
		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();
		myCTsPerServer.put(Constants.SERVER1, new ArrayList<>());
		myCTsPerServer.put(Constants.SERVER2, new ArrayList<>());
		
		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime());  // we don't use random here but fixed RAM like 512 MB
		
		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
		
		//for self-management after generation.
		Controller controller = new Controller(api);
		Analyzer analyzer = new Analyzer(api, controller);
		
		while (true) {

			String ctId = Long.toString(baseID+ind);	
			// 1. Calculer la quantit锟� de RAM utilis闁� par mes CTs sur chaque serveur
			//long memOnServer1 = api.getNode(Constants.SERVER1).getMemory_used();
			//long memOnServer2 = api.getNode(Constants.SERVER2).getMemory_used();

			// RAM used on Server1
			long memOnServer1 = 0;
			if(myCTsPerServer.get(Constants.SERVER1).size()!=0) {
				for(LXC ctSrv1 : myCTsPerServer.get(Constants.SERVER1)) {
					memOnServer1 += ctSrv1.getMem();
					long memCT= ctSrv1.getMem();
					long memMaxCT= ctSrv1.getMaxmem();
					String nameCT = ctSrv1.getName();
					String vmidCT = ctSrv1.getVmid();
					System.out.println("The CT: "+ nameCT+" with vmidCT: "+ vmidCT +" On Server 1 Consumes "+ memCT +" RAM. Max Mem: "+ memMaxCT);
				}
			}
			System.out.println("My cluster of CTs on Server 1 Consumes "+ memOnServer1 +" RAM.");
			
			// RAM used on Server2
			long memOnServer2 = 0;
			if(myCTsPerServer.get(Constants.SERVER2).size()!=0) {
				for(LXC ctSrv2 : myCTsPerServer.get(Constants.SERVER2)) {
					memOnServer2 += ctSrv2.getMem();
					long memMaxCT2= ctSrv2.getMaxmem();
					long memCT2= ctSrv2.getMem();
					String nameCT2 = ctSrv2.getName();
					System.out.println("The CT Name: "+ nameCT2 +" On Server 2 Consumes "+ memCT2 +" RAM. Max Mem: "+ memMaxCT2);
				}
			}
			System.out.println("My cluster of CTs on Server 2 Consumes "+ memOnServer2 +" RAM.");
			
			// function to calculate the RAM used by the cluster on Sever 

			
			// M闁檕ire autoris闁� sur chaque serveur
			float memRatioOnServer1 = memAllowedOnServer1; //16% of RAM =>0.16f
			float memRatioOnServer2 = memAllowedOnServer2;
			
			System.out.println("Sever1:"+ memAllowedOnServer1/(1024*1024) +"MB RAM allowed on Server1");
			System.out.println("Sever1:"+ memOnServer1/(1024*1024) +"MB RAM used on Server1, => " + 100*memOnServer1/memAllowedOnServer1 +"% used");
			System.out.println("Sever2:"+ memAllowedOnServer2/(1024*1024) +"MB RAM allowed on Server2");
			System.out.println("Sever2:"+ memOnServer2/(1024*1024) +"MB RAM used on Server2, => " + 100*memOnServer2/memAllowedOnServer2 +"% used");
			
			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) {  // Exemple de condition de l'arr闃� de la g闁氶枮ation de CTs

				// choisir un serveur al闁峵oirement avec les ratios sp闁廼fi闁� 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;	
				
				// cr闁憆 un contenaire sur ce serveur
				// ...
				api.createCT(serverName, ctId, Constants.CT_BASE_NAME+ind,512);
				//api.startCT(serverName,Long.toString(Constants.CT_BASE_ID+ind));
								
				// planifier la prochaine creation
				int timeToWait = 2*getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				
				// attendre jusqu'au prochain 闁㈤憺ement
				Thread.sleep(1000 * 40); //  minimum of about 30 sec 
				//start the CT just created & wait for little moment
				api.startCT(serverName,ctId);
				ind++;	
				Thread.sleep(1000 * 5); //wait a little bit
				
				// update HashMap
				System.out.println("Updating List on Server1.");
				myCTsPerServer.get(serverName).add(api.getCT(serverName, ctId));
				List<LXC> myCTsPerServer1_cp = new ArrayList<LXC>(myCTsPerServer.get(Constants.SERVER1)); //copy the old one
				List<LXC> myCTsPerServer2_cp = new ArrayList<LXC>(myCTsPerServer.get(Constants.SERVER2));
				System.out.println("myCTsPerServer1_cp.size is: "+myCTsPerServer1_cp.size());
				System.out.println("myCTsPerServer2_cp.size is: "+myCTsPerServer2_cp.size());
				// clear all and then we update it with concurrent information
				myCTsPerServer.clear();
				//myCTsPerServer.get(Constants.SERVER1).clear(); 
				//myCTsPerServer.get(Constants.SERVER2).clear();
				myCTsPerServer.put(Constants.SERVER1, new ArrayList<>());
				myCTsPerServer.put(Constants.SERVER2, new ArrayList<>());
				//re-fill the hashmap
				
				for(LXC ct: myCTsPerServer1_cp) {
					String ctId_loop = ct.getVmid();
					System.out.println(ctId_loop);
					System.out.println("updated ctId: "+ctId_loop +" On Server1");
					myCTsPerServer.get(Constants.SERVER1).add(api.getCT(Constants.SERVER1,ctId_loop));
					System.out.println("CT Info updated: "+myCTsPerServer.get(Constants.SERVER1).get(0).getName()+" On Server1");
				}
				for(LXC ct: myCTsPerServer2_cp) {
					String ctId_loop = ct.getVmid();
					System.out.println("updated ctId: "+ctId_loop+" On Server2");
					myCTsPerServer.get(Constants.SERVER2).add(api.getCT(Constants.SERVER2,ctId_loop));
					System.out.println("CT Info updated: "+myCTsPerServer.get(Constants.SERVER2)+" On Server2");
				}
				
				System.out.println(myCTsPerServer.get(Constants.SERVER1).size());
				System.out.println(myCTsPerServer.get(Constants.SERVER2).size());
				

				System.out.println("-------------------------------------------------------------------------------------");
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
				System.out.println("Auto-management is starting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
				
				Monitor monitor = new Monitor(api,analyzer,myCTsPerServer);
			}
		}
		
	}

}
