/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBroker extends SimEntity {

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	/** The estimate cloudlet list. */
	protected Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap;
	
	/** The brokers list*/
	protected List<Integer> brokerIdsList;
	
	/** The estimate cloulet list of partner */
	
	protected Map<Integer,EstimationCloudletOfPartner> estimateCloudletofParnerMap;


	

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBroker(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		setEstimateCloudletMap(new HashMap<Integer, Map<Integer, EstimationCloudletObserve>>());
		setBrokerIdsList(new ArrayList<Integer>());
		setEstimateCloudletofParnerMap(new HashMap<Integer, EstimationCloudletOfPartner>());
		
	}

	public Map<Integer, Map<Integer, EstimationCloudletObserve>> getEstimateCloudletMap() {
		return estimateCloudletMap;
	}

	public void setEstimateCloudletMap(
			Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap) {
		this.estimateCloudletMap = estimateCloudletMap;
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// if the cloudlet is submitted from partner
			case CloudSimTags.PARTNER_ESTIMATE:
				processPartnerCloudletEstimate(ev);
				break;
			case CloudSimTags.PARTNER_INTERNAL_ESTIMATE_RETURN:
				processPartnerCloudletInternalEstimateReturn(ev);
				break;
			case CloudSimTags.PARTNER_ESTIMATE_TIMEOUT:
				processPartnerEstimateTimeout(ev);
				break;
			case CloudSimTags.PARTNER_EXEC:
				processPartnerCloudlet(ev);
				break;
			//if the cloudlet is wanted to process in another DB center
			case CloudSimTags.PARTNER_ESTIMATE_SENT:
				processSentTaskToPartnerEstimate(ev);
				break;
			//if the cloudle estimate result returned from partner
			case CloudSimTags.PARTNER_ESTIMATE_RETURN: 
				processReturnEstimateFromParner(ev);
				break;
			case CloudSimTags.PARTNER_EXEC_INTERNAL_RETURN:
				processPartnerExecInternalReturn(ev);
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		Log.printLine("CloudSim.getBrokerIdsList: "+CloudSim.getBrokerIdsList());
		setBrokerIdsList(CloudSim.getBrokerIdsList());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");
		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
//			sendNow(getId(), CloudSimTags.PARTNER_EXEC, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}
	
	protected void processPartnerCloudletEstimate(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received partner estimate cloudlet.");
		
		Map<Integer, Map<Integer, EstimationCloudletObserve>> estimateCloudletMap = getEstimateCloudletMap();
		
		if (!estimateCloudletMap.containsKey(ev.getSource())) {
			Map<Integer, EstimationCloudletObserve> cloudletList = new HashMap<Integer, EstimationCloudletObserve>();
			estimateCloudletMap.put(ev.getSource(), cloudletList);
		}
		
		Map<Integer, EstimationCloudletObserve> cloudletList = estimateCloudletMap.get(ev.getSource());
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		List<Integer> datacenterIDs = getDatacenterIdsList();
		
		ResCloudlet resCloudlet = new ResCloudlet(cloudlet);
		resCloudlet.setFinishTime(Double.MAX_VALUE);
		
		EstimationCloudletObserve eco = new EstimationCloudletObserve(resCloudlet, datacenterIDs);
		
		cloudletList.put(new Integer(cloudlet.getCloudletId()), eco);
		
		for (int i: datacenterIDs) {
			Object[] data = {ev.getSource(), cloudlet};
			sendNow(i, CloudSimTags.PARTNER_ESTIMATE, data);
		}
		
		Object[] timeoutData = {ev.getSource(), cloudlet.getCloudletId()};
		send(getId(), 30, CloudSimTags.PARTNER_ESTIMATE_TIMEOUT, timeoutData);
	}
	
	protected void processPartnerCloudletInternalEstimateReturn(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received internal estimate from datacenter #" 
						+ ev.getSource());
		
		Object[] data = (Object[]) ev.getData();
		int partnerID = (int)data[0];
		ResCloudlet resCloudlet = (ResCloudlet) data[1];
		int cloudletID = resCloudlet.getCloudlet().getCloudletId();
		
		Map<Integer, EstimationCloudletObserve> partnerCloudletList = getEstimateCloudletMap().get(partnerID);
		
		if (partnerCloudletList.containsKey(cloudletID)) {
			EstimationCloudletObserve eco = partnerCloudletList.get(cloudletID);
			
			eco.receiveEstimateResult(ev.getSource(), resCloudlet);
			if (eco.isFinished()) {
				// send result to partner
				sendNow(partnerID, CloudSimTags.PARTNER_ESTIMATE_RETURN, eco.getResCloudlet());
				
				// remove partner estimation cloudlet 
				partnerCloudletList.remove(eco);
			}
		}
	}
	
	protected void processPartnerEstimateTimeout(SimEvent ev) {
		Object[] data = (Object[]) ev.getData();
		int partnerId = (int)data[0];
		int cloudletId = (int)data[1];
		
		if (getEstimateCloudletMap().containsKey(partnerId)) {
			Map<Integer, EstimationCloudletObserve> partnerCloudletList = getEstimateCloudletMap().get(partnerId);
			
			if (partnerCloudletList.containsKey(cloudletId)) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Partner estimate timeout!");
				
				EstimationCloudletObserve eco = partnerCloudletList.get(cloudletId);
				// send result to partner
				sendNow(partnerId, CloudSimTags.PARTNER_ESTIMATE_RETURN, eco.getResCloudlet());
				
				// remove partner estimation cloudlet
				partnerCloudletList.remove(eco);
			}
		}
	}
	
	protected void processPartnerCloudlet(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received partner exec cloudlet from Broker #" + ev.getSource());
		
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		cloudlet.setUserId(getId());
		int vmId = cloudlet.getVmId();
		
		Object[] data = { ev.getSource(), cloudlet };
		
		sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.PARTNER_EXEC, data);
	}
	
	protected void processPartnerExecInternalReturn(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received exec result from datacenter #" + ev.getSource());
		Object[] data = (Object[]) ev.getData();
		
		int partnerId = (int)data[0];
		int result = (int)data[1];
		String msg = (String)data[2];
		Cloudlet cl = (Cloudlet)data[3];
		
		Object[] returnData = { result, msg, cl };
		
		sendNow(partnerId, CloudSimTags.PARTNER_EXEC_RETURN, returnData);
	}
	
	/**
	 *	process send data to all partner to estimate time process 
	 * @param ev
	 */
	protected void processSentTaskToPartnerEstimate(SimEvent ev) {
		Cloudlet cl = (Cloudlet) ev.getData();
		//if process is require estimate in partners
		if(cl.getStatus() == Cloudlet.PARTNER_SUBMMITED ){
			if(getDatacenterIdsList().size() == 1 && getBrokerIdsList().get(0) == getId()){
				Log.printLine("No parner found, can not send task anywhere");
			}
			ResCloudlet rCl = new ResCloudlet(cl); 
			List<Integer> partnerIdsList  = new ArrayList<Integer>();
			for( Integer partnerIds : this.getBrokerIdsList()){
				if(partnerIds != getId()){
					Log.printLine("Cloundlet #"+ cl.getCloudletId()+ "have send to broker #"+partnerIds);
					//send to partner
					send(partnerIds, 0, CloudSimTags.PARTNER_ESTIMATE, cl);
					//add to requested list
					partnerIdsList.add(partnerIds);
				}
			}
			EstimationCloudletOfPartner esOfPatner = new EstimationCloudletOfPartner(rCl, partnerIdsList);
			getEstimateCloudletofParnerMap().put(rCl.getCloudletId(), esOfPatner);
		}
	}
	/**
	 *  Process result estimate form partner ID 
	 *  If all estimate has been received send execute to partner have best execute time to execute 
	 * @param ev
	 */
	protected void processReturnEstimateFromParner(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Received estimate result from Broker #" + ev.getSource());
		Log.printLine(ev.getData() instanceof Cloudlet);
		ResCloudlet rCl =(ResCloudlet) ev.getData();
		Integer clouletId = rCl.getCloudletId();
		Integer partnerId = rCl.getUserId();
		
		EstimationCloudletOfPartner partnerCloudletEstimateList = getEstimateCloudletofParnerMap().get(clouletId);
		
		if (partnerCloudletEstimateList.getPartnerIdsList().contains(partnerId)) {
			partnerCloudletEstimateList.receiveEstimateResult(partnerId, rCl);			
			if (partnerCloudletEstimateList.isFinished()) {
				// send result to partner
				sendNow(partnerId, CloudSimTags.PARTNER_EXEC, rCl.getCloudlet());
			}
		}
	}
	
	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

	public List<Integer> getBrokerIdsList() {
		return brokerIdsList;
	}

	public void setBrokerIdsList(List<Integer> brokerIdsList) {
		this.brokerIdsList = brokerIdsList;
	}

	public Map<Integer, EstimationCloudletOfPartner> getEstimateCloudletofParnerMap() {
		return estimateCloudletofParnerMap;
	}

	public void setEstimateCloudletofParnerMap(
			Map<Integer, EstimationCloudletOfPartner> estimateCloudletofParnerMap) {
		this.estimateCloudletofParnerMap = estimateCloudletofParnerMap;
	}


	
}
