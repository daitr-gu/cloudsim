package org.cloudbus.cloudsim;

import java.util.List;

public class EstimationCloudletObserve {
	private List<Integer> datacenterList;
	private ResCloudlet resCloudlet;
	
	public EstimationCloudletObserve(ResCloudlet resCloudlet, List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
		this.resCloudlet = resCloudlet;
	}
	
	public List<Integer> getDatacenterList() {
		return datacenterList;
	}
	
	public void setDatacenterList(List<Integer> datacenterList) {
		this.datacenterList = datacenterList;
	}
	
	public ResCloudlet getResCloudlet() {
		return resCloudlet;
	}
	
	public void setResCloudlet(ResCloudlet resCloudlet) {
		this.resCloudlet = resCloudlet;
	}
	
	public void receiveEstimateResult(int datacenterID, ResCloudlet reResCloudlet) {
		int totalDatacenter = datacenterList.size();
		for (int i = 0; i < totalDatacenter; i++) {
			if (datacenterList.get(i) == datacenterID) {
				datacenterList.remove(i);
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
	
	public boolean isFinished() {
		return datacenterList.size() == 0;
	}
}
