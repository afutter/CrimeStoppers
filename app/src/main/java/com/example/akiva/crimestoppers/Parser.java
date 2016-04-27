package com.example.akiva.crimestoppers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//com.example.akiva.crimestoppers;

public class Parser {


	public static void main(String[] args) {

		Hashtable<String, Integer> crime_count = new Hashtable<String, Integer>(100);
		//LinkedList<Crime> data = new LinkedList();

		try { //used for debugging, comment out if you want to pass in file

			
		} catch (/*FileNotFoundException*/ Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

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
			doc = dBuilder.parse(System.in);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element docElement = doc.getDocumentElement();
		NodeList n1 = docElement.getChildNodes();
		for(int i = 0; i < n1.getLength(); ++i) {

			Node nextCrime = n1.item(i);

			if (nextCrime.getNodeType() == Node.ELEMENT_NODE) {
				NodeList n2 = nextCrime.getChildNodes();

				//Crime newCrime = null;
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



				//newCrime = new Crime(offense, address, xCoordinate, yCoordinate);
				if(crime_count.get(offense) == null) {
					crime_count.put(offense, 1);
				} else {
					crime_count.put(offense, crime_count.get(offense) + 1);
				}
				//data.add(newCrime);
			}
		}
		for(String key: crime_count.keySet()) {
			System.out.println(key + " " + crime_count.get(key));
		}
	}

}