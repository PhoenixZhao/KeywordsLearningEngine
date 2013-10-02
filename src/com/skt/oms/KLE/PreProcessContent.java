/* 
 * Copyright (C) 1997, SKTC  Ltd., 
 * All Rights Reserved 
 * Class name: PreProcessContent.java
 * Description: This class breaks the contents of the comment material into 
 * 				shorter sentences in order for further processing.
 *              
 * 
 * Modification History: 
 **********************************************************
 * Date		  Author		Comments
 * 11 Jan 2003	John			Created
 **********************************************************
 */


package com.skt.oms.KLE;

import java.io.*;
import java.util.Vector;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class PreProcessContent {
	final private static String regex = "[\\pP]";
	
	/*
	 * Entrance to the class, dealing with a directory.
	 */
	public static void doDir(String dirDatDirInput, String dirDataTemp) {
		try {
			File Directory = new File(dirDatDirInput);
			String[] files = Directory.list();

			for ( int i = 0; i < files.length; i++) {
				String filein = dirDatDirInput + files[i];
				String fileout = dirDataTemp + files[i];
				Preprocess(filein, fileout);
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Entrance to the class, dealing with a particular file.
	 */
	public static void doFile(String fileIn, String strDataTemp) {
		try {
			String tempFile = fileIn.split("[\\\\/]")[fileIn.split("[\\\\/]").length - 1];
			String fileOut = strDataTemp + tempFile;
			Preprocess(fileIn, fileOut);

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * This function first gets the text of "content" element from the input XML file.
	 * Then  break them into shorter sentences with common puctuations as boundaries.
	 */
	public static void Preprocess(String fileIn, String fileout) {		
		try {
			XMLInputFactory xif = XMLInputFactory.newInstance();
			xif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

			XMLStreamReader sr = xif.createXMLStreamReader(new FileInputStream(
					fileIn), "UTF-8");
		
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileout), "UTF-8"));
			
			int event = sr.getEventType();
			while (true){
				switch (event){
				case XMLStreamConstants.START_ELEMENT:
					if (sr.getLocalName().equals("content")){
						String strNewContent = sr.getElementText().replaceAll(regex, "#");
						Vector<String> vecContents = split(strNewContent, '#');
						for ( int i = 0; i < vecContents.size(); i++) {
							bw.write(vecContents.get(i) + "\n");
						}
					}
					break;
				default:
				    break;
				}
				if ( !sr.hasNext())
					break;
				event = sr.next();
			}
			sr.close();
			bw.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method splits a string, deletes the blank strings then return as a vector.
	 */
	public static Vector<String> split(String str, char x) {
		
		Vector<String> v = new Vector<String>();
		String str1 = new String();
		// String str1="";
		for ( int i = 0; i < str.length(); i++) {
			if ( str.charAt(i) == x) {
				v.add(str1);
				str1 = new String();
			} else {
				str1 += str.charAt(i);
			}
		}
		v.add(str1);

		for ( int i = 0; i < v.size(); i++) {
			String temp = v.elementAt(i).trim();
			if ( temp.equals("")) {
				v.remove(i);
				i--;
			}
		}
		return v;

	}
}