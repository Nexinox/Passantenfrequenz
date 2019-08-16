package de.hofmann.Passantenfrequenz.model;

/**
 * Class Camera for data model of application
 */
public class Camera {

	private int x, y;

	private String name;
	private int rotationDeg;

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
