package utils;

import java.util.Comparator;

public class Provider implements Comparator<Provider>, Comparable<Provider> {

public String deviceId;
	
	public String getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}


	public Double getnValue() {
		return nValue;
	}


	public void setnValue(Double nValue) {
		this.nValue = nValue;
	}


	public Double getdWeight() {
		return dWeight;
	}


	public void setdWeight(Double dWeight) {
		this.dWeight = dWeight;
	}



	public Double nValue;
	public Double dWeight;
	public Double battery;
	public Double linkSpeed;
	public Double power;
	public Double distance;
	public Double old_distance;
	public int nsensors;
	public boolean servicing;
	
	public boolean getServcing() {
		return this.servicing;
	}
	
	public void setServicing(boolean servicing) {
		this.servicing = servicing;
	}
	
	public int getNsensors() {
		return nsensors;
	}


	public void setNsensors(int nsensors) {
		this.nsensors = nsensors;
	}


	public Double getBattery() {
		return battery;
	}


	public void setBattery(Double battery) {
		this.battery = battery;
	}


	public Double getLinkSpeed() {
		return linkSpeed;
	}


	public void setLinkSpeed(Double linkSpeed) {
		this.linkSpeed = linkSpeed;
	}


	public Double getPower() {
		return power;
	}


	public void setPower(Double power) {
		this.power = power;
	}


	public Double getDistance() {
		return distance;
	}


	public void setDistance(Double distance) {
		this.distance = distance;
	}


	public Double getOld_distance() {
		return old_distance;
	}


	public void setOld_distance(Double old_distance) {
		this.old_distance = old_distance;
	}


	public Provider(String deviceId, double nValue, double dWeight,double battery,double linkSpeed,
					double power, double oDist, double nDist, int nsensors, boolean servicing) {
		this.deviceId = deviceId;
		this.nValue = nValue;
		this.dWeight = dWeight;
		this.battery=battery;
		this.linkSpeed=linkSpeed;
		this.power=power;
		this.old_distance = oDist;
		this.distance = nDist;
		this.nsensors=nsensors;
		this.servicing = servicing;
	}


//	@Override
	public int compare(Provider arg0, Provider arg1) {
		
		if((arg0.dWeight - arg1.dWeight) > 0)
			return 1;
		else if((arg0.dWeight - arg1.dWeight) < 0)
			return -1;
		return 0;
	}


//	@Override
	public int compareTo(Provider arg0) {
		// TODO Auto-generated method stub
		return (this.dWeight).compareTo(arg0.dWeight);
	}
	
	public String toString() {
		return ("("+this.deviceId +":"+ this.nValue+ ":" + this.dWeight.toString()+")" );
	}

	@Override
    public boolean equals(Object o){
		
        if(o instanceof Provider){
            String toCompare = ((Provider) o).deviceId;
            return this.deviceId.equalsIgnoreCase(toCompare);
        }
        return false;
    }



    @Override
    public int hashCode(){
        return 1;
    }
}
