package de.hofmann.Passantenfrequenz.XLS;

import java.time.LocalTime;

public class Entry {
	
	private String camName;
	private java.util.Date date;
	private LocalTime startTime, endTime;
	private int totalIn, totalOut, personsIn, personsOut, unknownIn, unknownOut;
	
	

	public LocalTime getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}
	public LocalTime getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	public java.util.Date getDate() {
		return date;
	}
	public void setDate(java.util.Date ddate) {
		date = ddate;
	}
	public String getCamName() {
		return camName;
	}
	public void setCamName(String camName) {
		this.camName = camName;
	}
	public int getTotalIn() {
		return totalIn;
	}
	public void setTotalIn(int totalIn) {
		this.totalIn = totalIn;
	}
	public int getTotalOut() {
		return totalOut;
	}
	public void setTotalOut(int totalOut) {
		this.totalOut = totalOut;
	}
	public int getPersonsIn() {
		return personsIn;
	}
	public void setPersonsIn(int personsIn) {
		this.personsIn = personsIn;
	}
	public int getPersonsOut() {
		return personsOut;
	}
	public void setPersonsOut(int personsOut) {
		this.personsOut = personsOut;
	}
	public int getUnknownIn() {
		return unknownIn;
	}
	public void setUnknownIn(int unknownIn) {
		this.unknownIn = unknownIn;
	}
	public int getUnknownOut() {
		return unknownOut;
	}
	public void setUnknownOut(int unknownOut) {
		this.unknownOut = unknownOut;
	}
}
