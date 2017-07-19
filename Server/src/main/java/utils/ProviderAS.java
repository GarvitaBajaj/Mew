package utils;

public class ProviderAS {
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public int getOldSensingtime() {
		return oldSensingtime;
	}
	public void setOldSensingtime(int oldSensingtime) {
		this.oldSensingtime = oldSensingtime;
	}
	public int getNewSensingtime() {
		return newSensingtime;
	}
	public void setNewSensingtime(int newSensingtime) {
		this.newSensingtime = newSensingtime;
	}
	public int getDifftime() {
		return difftime;
	}
	public void setDifftime(int difftime) {
		this.difftime = difftime;
	}
	String provider;
	int oldSensingtime;
	int newSensingtime;
	int difftime;

}