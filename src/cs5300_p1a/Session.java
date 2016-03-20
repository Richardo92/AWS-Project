package cs5300_p1a;

import java.time.ZonedDateTime;
import java.util.Date;

public class Session {
	private final int sessionID;
	private ZonedDateTime expireTime;
	private int version;
	private String sessionState;
	
	public Session(int sessionID, int minutes, int version, String sessionState) {
		this.sessionID = sessionID;
		this.expireTime = ZonedDateTime.now().plusMinutes(minutes);
		this.version = version;
		this.sessionState = sessionState;
	}
	
	/**
	 * return the session id
	 * @return
	 */
	synchronized public int getSessionID() {
		return this.sessionID;
	}
	
	/**
	 * return the expire time in the form of long
	 * @return
	 */
	synchronized public ZonedDateTime getExpireTime() {
		return this.expireTime;
	}
	
	/**
	 * return the version number
	 * @return
	 */
	synchronized public int getVersion() {
		return this.version;
	}
	
	/**
	 * return the session state 
	 * @return
	 */
	synchronized public String getSessionState() {
		return this.sessionState;
	}
	
	/**
	 * set the expire date as the input parameter
	 * @param expireDate
	 */
	synchronized public void setExpireTime(int minutes) {
		this.expireTime = ZonedDateTime.now().plusMinutes(minutes);
	}
	
	/**
	 * set the version of this session as the input parameter
	 * @param version
	 */
	synchronized public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * set the session state as the input parameter
	 * @param sessionState
	 */
	synchronized public void setSessionState(String sessionState) {
		this.sessionState = sessionState;
	}
}
