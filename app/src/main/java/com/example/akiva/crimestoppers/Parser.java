package com.example.akiva.crimestoppers;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class holds the methods for parsing a local file XML file as well as a URL known to be the
 * most recent crime data for DC. This URL is constantly checked for updated crimes by querying and
 * checking against our database of crimes. New crimes are added to the front, and old crimes past
 * a certain date are removed from the list if they are not relevant. This class also holds a
 * method to return the closest crimes based on a radius given.
 */
public class Parser {


	Hashtable<String, Integer> crime_count = new Hashtable<String, Integer>(100);

	public static LinkedList<Crime> data = new LinkedList<Crime>();

	public crimeSync mLink;

	private final String LIVE_URL =
			"http://data.octo.dc.gov/feeds/crime_incidents/crime_incidents_current.xml";

	public int parse() {
		int count = 0;
		if (mLink != null){
			mLink = new crimeSync();

			try {
				count = mLink.get();
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		return count;
	}

	private class crimeSync extends AsyncTask<Void, Void, Integer> {
		private HttpURLConnection cn;
		private InputStream bits;
		protected Integer doInBackground(Void... params) {

			int ret = 0;

			try {
				URL url = new URL(LIVE_URL);
				cn = (HttpURLConnection) url.openConnection();

				bits = cn.getInputStream();

				ret = liveParse(bits);

			} catch (MalformedURLException e) {
				Log.e("DEBUG", e.toString());
			} catch (IOException e) {
				Log.e("DEBUG", e.toString());
			} finally {
				try {
					if (null != bits) {
						bits.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				cn.disconnect();
			}

			return Integer.valueOf(ret);
		}

		public int liveParse(InputStream in) {

			//holds position to insert new crimes in the front of the linkedlist
			//First crime inserted at front, then second, then third and so on until
			//no more crimes are available. The caller then knows how many crimes are new to query
			//from the list of crimes.

			int newPos = 0;

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
					String start = null, end = null;
					double LatCoordinate = 0;
					double LongCoordinate = 0;

					for(int j = 0; j < n2.getLength(); ++j) {

						Node nextAtt = n2.item(j);

						if (nextAtt.getNodeType() == Node.ELEMENT_NODE) {

							if(nextAtt.getNodeName().compareTo("dcst:offense") == 0) {
								Node temp = nextAtt.getFirstChild();
								offense = temp.getNodeValue();
							} else if(nextAtt.getNodeName().compareTo("dcst:blockxcoord") == 0) {
								Node temp = nextAtt.getFirstChild();
								String latCoord = temp.getNodeValue();
								LatCoordinate = Double.parseDouble(latCoord) / 10000;
							} else if(nextAtt.getNodeName().compareTo("dcst:blockycoord") == 0) {
								Node temp = nextAtt.getFirstChild();
								String longCoord = temp.getNodeValue();
								LongCoordinate = (Double.parseDouble(longCoord) / 10000) - 90;
							} else if(nextAtt.getNodeName().compareTo("dcst:blocksiteaddress") == 0) {
								Node temp = nextAtt.getFirstChild();
								address = temp.getNodeValue();
							} else if(nextAtt.getNodeName().compareTo("dcst:start_date") == 0) {
								Node temp = nextAtt.getFirstChild();
								start = temp.getNodeValue();
							} else if(nextAtt.getNodeName().compareTo("dcst:end_date") == 0) {
								Node temp = nextAtt.getFirstChild();
								end = temp.getNodeValue();
							}
						}
					}
					newCrime = new Crime(offense, address, start, end, LatCoordinate, LongCoordinate);

					if (data.contains(newCrime)) {

						return newPos;

					} else {

						data.add(newPos++, newCrime);

					}
				}

			}

			return newPos;
		}

	}

	/**
	 * Parses a whole XML document of crimes, used for testing purposes
	 * @param in Stream to file
     */
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
				String start = null, end = null;
				double LatCoordinate = 0;
				double LongCoordinate = 0;

				for(int j = 0; j < n2.getLength(); ++j) {

					Node nextAtt = n2.item(j);

					if (nextAtt.getNodeType() == Node.ELEMENT_NODE) {

						if(nextAtt.getNodeName().compareTo("dcst:offense") == 0) {
							Node temp = nextAtt.getFirstChild();
							offense = temp.getNodeValue();
						} else if(nextAtt.getNodeName().compareTo("dcst:blockxcoord") == 0) {
							Node temp = nextAtt.getFirstChild();
							String latCoord = temp.getNodeValue();
							LatCoordinate = Double.parseDouble(latCoord) / 10000;
						} else if(nextAtt.getNodeName().compareTo("dcst:blockycoord") == 0) {
							Node temp = nextAtt.getFirstChild();
							String longCoord = temp.getNodeValue();
							LongCoordinate = (Double.parseDouble(longCoord) / 10000) - 90;
						} else if(nextAtt.getNodeName().compareTo("dcst:blocksiteaddress") == 0) {
							Node temp = nextAtt.getFirstChild();
							address = temp.getNodeValue();
						} else if(nextAtt.getNodeName().compareTo("dcst:start_date") == 0) {
							Node temp = nextAtt.getFirstChild();
							start = temp.getNodeValue();
						} else if(nextAtt.getNodeName().compareTo("dcst:end_date") == 0) {
							Node temp = nextAtt.getFirstChild();
							end = temp.getNodeValue();
						}
					}
				}
				newCrime = new Crime(offense, address, start, end, LatCoordinate, LongCoordinate);
				if(crime_count.get(offense) == null) {
					crime_count.put(offense, 1);
				} else {
					crime_count.put(offense, crime_count.get(offense) + 1);
				}
				data.add(newCrime);
				//----------
				//System.out.println(newCrime.LatCoordinate  + "  -  " +newCrime.LongCoordinate + "\n");
				//----------
			}
		}
		for(String key: crime_count.keySet()) {
			System.out.println(key + " " + crime_count.get(key));
		}
	}

	/**
	 *
	 * @param lat Location latitude
	 * @param lon Location longitude
     * @return Sorts crimes into a TreeSet to return based on their distance
     */
	public TreeSet<Crime> nearestCrimes(double lat, double lon, double radius) {

		TreeSet<Crime> tree = new TreeSet<Crime>();
		for (Crime c : data) {
			c.distance = distance(lat, lon, c.Lat, c.Long, "N");
			tree.add(c);
		}
		return tree;
	}

	/**
	 * Method returns the Haversine distance between two points on the globe.
	 * @param lat1 location 1 latitude
	 * @param lon1 location 1 longitude
	 * @param lat2 location 2 latitude
	 * @param lon2 location 2 longitude
	 * @param unit Units of distance requested
     * @return Distance in unit
     */
	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit.equals("K")) {
			dist = dist * 1.609344;
		} else if (unit.equals("N")) {
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

}