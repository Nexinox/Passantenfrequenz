package de.hofmann.Passantenfrequenz;

public class Node {

	private int x,y;
	private String name;
	private int rotationDeg;
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
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRotationDeg() {
		return rotationDeg;
	}
	public void setRotationDeg(int rotationDeg) {
		this.rotationDeg = rotationDeg;
	}
	
	
	
}
