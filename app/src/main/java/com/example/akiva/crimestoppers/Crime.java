package com.example.akiva.crimestoppers;

public class Crime implements Comparable<Crime> {
	String offense;
	String address;
	double xCoordinate;
	double yCoordinate;
	double distance;
	
	Crime (String offense, String address, double xCoordinate, double yCoordinate) {
		this.offense = offense;
		this.address = address;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.distance = 0;
	}

	public int compareTo(Crime c2) {
		if (distance < c2.distance)
			return -1;
		else if (distance > c2.distance)
			return 1;
		else
			return 0;
	}

}
