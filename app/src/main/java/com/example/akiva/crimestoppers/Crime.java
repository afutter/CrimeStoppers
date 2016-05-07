package com.example.akiva.crimestoppers;

import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Circle;

public class Crime implements Comparable<Crime> {
	String address, start_date, offense, end_date;
	double Lat;
	double Long;
	double distance;
	Marker marker;
	Circle circle;
	private boolean isVisable = true;//crimes are visable by default
	
	Crime (String offense, String address, String start_date, String end_date, double xCoordinate, double yCoordinate) {
		this.offense = offense;
		this.address = address;
		this.Lat = xCoordinate;
		this.Long = yCoordinate;
		this.distance = 0;
		this.start_date = start_date;
		this.end_date = end_date;
	}

	public int compareTo(Crime c2) {
		if (distance < c2.distance)
			return -1;
		else if (distance > c2.distance)
			return 1;
		else
			return 0;
	}
	public void setVisable(boolean visability){
		if(marker == null){
			Log.i("MainActivity", "marker set to null");
			return;
		}
		if(circle == null){
			Log.i("MainActivity", "marker set to null");
			return;
		}
		marker.setVisible(visability);
		circle.setVisible(visability);
		this.isVisable = visability;
	}
	public boolean isVisable(){
		return isVisable;
	}

}
