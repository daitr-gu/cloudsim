package org.cloudbus.cloudsim;

import java.util.List;

public class EstimationCloudletOfPartner {
	
	private ResCloudlet resCloudlet;
	private List<Integer> partnerIdsList;
	
	public EstimationCloudletOfPartner(ResCloudlet resCloudlet,
			List<Integer> partnerIdsList) {
		super();
		this.resCloudlet = resCloudlet;
		this.partnerIdsList = partnerIdsList;
	}
	
	public void receiveEstimateResult(int partnerId, ResCloudlet reResCloudlet) {
		int totalPartnerId = partnerIdsList.size();
		for (int i = 0; i < totalPartnerId; i++) {
			if (partnerIdsList.get(i) == partnerId) {
				partnerIdsList.remove(i);
				break;
			}
		}
		
		double finishTime = reResCloudlet.getClouddletFinishTime();
		double bestFinishTime = resCloudlet.getClouddletFinishTime();
		
		if (finishTime > 0 && finishTime < bestFinishTime) {
			resCloudlet.setFinishTime(finishTime);
			resCloudlet.getCloudlet().setVmId(reResCloudlet.getCloudlet().getVmId());
		}
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
	
	
	
}
