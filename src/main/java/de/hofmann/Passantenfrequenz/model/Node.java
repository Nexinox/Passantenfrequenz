package de.hofmann.Passantenfrequenz.model;

/**
 * Class Camera for data model of application
 */
public class Node {

	private String name;
	private int in, out;
	public int getIn() {
		return in;
	}
	public void setIn(int in) {
		this.in = in;
	}
	public int getOut() {
		return out;
	}
	public void setOut(int out) {
		this.out = out;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}



}
