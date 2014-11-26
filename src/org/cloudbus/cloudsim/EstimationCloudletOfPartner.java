package org.cloudbus.cloudsim;

import java.util.List;

public class EstimationCloudletOfPartner {
	
	private ResCloudlet resCloudlet;
	private List<Integer> partnerIdsList;
	private int currentBestPartnerId;
	
	public EstimationCloudletOfPartner(ResCloudlet resCloudlet,
			List<Integer> partnerIdsList) {
		super();
		this.resCloudlet = resCloudlet;
		this.partnerIdsList = partnerIdsList;
		this.currentBestPartnerId = -1;
	}
	
	public int receiveEstimateResult(int partnerId, ResCloudlet reResCloudlet) {
		int partner_cancel_waiting_exec = partnerId; 
		int totalPartnerId = partnerIdsList.size();
		for (int i = 0; i < totalPartnerId; i++) {
			if (partnerIdsList.get(i) == partnerId) {
				partnerIdsList.remove(i);
				break;
			}
		}
		
		double finishTime = reResCloudlet.getClouddletFinishTime();
		double bestFinishTime = resCloudlet.getClouddletFinishTime();
		if (bestFinishTime == -1 ||(finishTime > 0 && finishTime < bestFinishTime)){
			partner_cancel_waiting_exec = getCurrentBestPartnerId();
			setCurrentBestPartnerId(partnerId);
			resCloudlet.setFinishTime(finishTime);
			resCloudlet.getCloudlet().setVmId(reResCloudlet.getCloudlet().getVmId());
			Log.printLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+bestFinishTime);
			Log.printLine("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+resCloudlet.getClouddletFinishTime());
		}
		
		return partner_cancel_waiting_exec;
	}
	
	public ResCloudlet getResCloudlet() {
		return resCloudlet;
	}

	public void setResCloudlet(ResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}

	public List<Integer> getPartnerIdsList() {
		return partnerIdsList;
	}

	public void setPartnerIdsList(List<Integer> partnerIdsList) {
		this.partnerIdsList = partnerIdsList;
	}
	
	public boolean isFinished() {
		return partnerIdsList.size() == 0;
	}

	public int getCurrentBestPartnerId() {
		return currentBestPartnerId;
	}

	public void setCurrentBestPartnerId(int currentBestPartnerId) {
		this.currentBestPartnerId = currentBestPartnerId;
	}
	
	
	
}
