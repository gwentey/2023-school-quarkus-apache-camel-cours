package fr.pantheonsorbonne.ufr27.miage.camel;

public class Triangle {
	private Point a;
	private Point b;
	private Point c;

	public Triangle(Point a, Point b, Point c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Point a() {
		return a;
	}

	public Point b() {
		return b;
	}

	public Point c() {
		return c;
	}
}
