package com.example.akiva.crimestoppers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Parser {

	Hashtable<String, Integer> crime_count = new Hashtable<String, Integer>(100);
	LinkedList<Crime> data = new LinkedList<Crime>();

	public void parse(InputStream in) {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Document doc = null;

		try {
			doc = dBuilder.parse(in);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Element docElement = doc.getDocumentElement();
		NodeList n1 = docElement.getChildNodes();
		for(int i = 0; i < n1.getLength(); ++i) {

			Node nextCrime = n1.item(i);

			if (nextCrime.getNodeType() == Node.ELEMENT_NODE) {
				NodeList n2 = nextCrime.getChildNodes();

				Crime newCrime;
				String offense = null;
				String address = null;
				double xCoordinate = 0;
				double yCoordinate = 0;

				for(int j = 0; j < n2.getLength(); ++j) {

					Node nextAtt = n2.item(j);

					if (nextAtt.getNodeType() == Node.ELEMENT_NODE) {		

						if(nextAtt.getNodeName().compareTo("dcst:offense") == 0) {
							Node temp = nextAtt.getFirstChild();
							offense = temp.getNodeValue();
						} else if(nextAtt.getNodeName().compareTo("dcst:blockxcoord") == 0) {
							Node temp = nextAtt.getFirstChild();
							String xCoord = temp.getNodeValue();
							xCoordinate = Double.parseDouble(xCoord);
						} else if(nextAtt.getNodeName().compareTo("dcst:blockycoord") == 0) {
							Node temp = nextAtt.getFirstChild();
							String yCoord = temp.getNodeValue();
							yCoordinate = Double.parseDouble(yCoord);
						} else if(nextAtt.getNodeName().compareTo("dcst:blocksiteaddress") == 0) {
							Node temp = nextAtt.getFirstChild();
							address = temp.getNodeValue();
						}
					}
				}

				newCrime = new Crime(offense, address, xCoordinate, yCoordinate);
				if(crime_count.get(offense) == null) {
					crime_count.put(offense, 1);
				} else {
					crime_count.put(offense, crime_count.get(offense) + 1);
				}
				data.add(newCrime);
			}
		}
		for(String key: crime_count.keySet()) {
			System.out.println(key + " " + crime_count.get(key));
		}
	}

	private TreeSet<Crime> nearestCrimes(double lat, double lon) {

		TreeSet<Crime> tree = new TreeSet<Crime>();
		for (Crime c : data) {
			c.distance = distance(lat, lon, c.xCoordinate, c.yCoordinate, "N");
			tree.add(c);
		}
		return tree;

	}

	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}


	public void run() {
		/*Parser p = new Parser();
		try {
			p.parse(getResources().getAssets().open("ASAP__from03_27_2016__to04_07_2016.xml", 0));
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	}
}