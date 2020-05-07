package me.vinceh121.cl;

import java.net.InetAddress;
import java.util.Date;

public class PingResult {
	private InetAddress address;
	private Date date = new Date();
	private int httpStatus = -1;
	private String httpError, rawAddr, dnsError;

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public InetAddress getAddress() {
		return this.address;
	}

	public void setAddress(final InetAddress address) {
		this.address = address;
	}

	public int getHttpStatus() {
		return this.httpStatus;
	}

	public void setHttpStatus(final int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getHTTPError() {
		return this.httpError;
	}

	public void setHTTPError(final String error) {
		this.httpError = error;
	}

	public String getRawAddr() {
		return this.rawAddr;
	}

	public void setRawAddr(final String rawAddr) {
		this.rawAddr = rawAddr;
	}

	public String getDnsError() {
		return this.dnsError;
	}

	public void setDnsError(final String dnsError) {
		this.dnsError = dnsError;
	}

	@Override
	public String toString() {
		return "PingResult [address="
				+ this.address
				+ ", date="
				+ this.date
				+ ", httpStatus="
				+ this.httpStatus
				+ ", httpError="
				+ this.httpError
				+ ", rawAddr="
				+ this.rawAddr
				+ ", dnsError="
				+ this.dnsError
				+ "]";
	}
}
