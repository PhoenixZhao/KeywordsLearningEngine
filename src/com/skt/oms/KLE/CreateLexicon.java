/* 
* Copyright (C) 1997, SKTC  Ltd., 
* All Rights Reserved 
* Class name: CreateLexicon.java 
* Description: Using the previous result and a man-made corpus to create a  
* 				field-adapted new lexicon. That is the final result.
* 
* Modification History: 
**********************************************************
* Date		           Author		    Comments
* 04  August 2011	   ZhaoHuan			Created
**********************************************************
*/

package com.skt.oms.KLE;
import org.jdom.*;
import org.jdom.input.*;
import java.io.*;
import java.util.*;

/** 
 * Step 4: 
 * The CreateLexicon class deals with the "lexicon-DC-temp.dat" generated in 
 * Step 3 and create a field-adapted-to-DC lexicon with the man-made corpus 
 * "ANNODB_DC-new.xml".
 * The whole process is just to judge if the every line in 
 * the "lexicon-DC-temp.dat" can match the content in "ANNODB_DC-new.xml".Then 
 * adjust the sentiment to the right polarity.
 */
public class CreateLexicon {
	/**
	 * 
	 * The main process method. It calls the adjustLexicon method.
	 */
	public static void Do(String strDCTemp, String strANN, String strFinal)
		throws Exception
	{
		
		CreateLexicon cl = new CreateLexicon();		
		cl.adjustLexicon(strDCTemp, strANN, strFinal);		
	}
	
	/**
	 * 
	 * The main process method. It calls the adjustLexicon method.
	 */
	public void adjustLexicon(String strDataIn, String strXml, 
							   String strDataOut) throws Exception
	{
		ArrayList<LexiconItem> LexiconItemListOld = dat2Lexicon(strDataIn);
		ArrayList<LexiconItem> LexiconItemListNew = getNewLexicon(strXml);
		sort(LexiconItemListNew);
		replaceNull(LexiconItemListOld, LexiconItemListNew);
		removeNull(LexiconItemListOld);
		LexiconItemListOld = arrange(LexiconItemListOld);
		lexicon2Dat(LexiconItemListOld, strDataOut);		
	}
	
	/**
	 * The dat2Lexicon method to read the "lexicon-DC-temp.dat" and use a 
	 * String array to record the content of every line in the file.
	 * Return the ArrayList.
	 */
	public ArrayList<LexiconItem> dat2Lexicon(String strDataIn) throws Exception
	{
		ArrayList<LexiconItem> lexItemList = new ArrayList<LexiconItem>();
		BufferedReader bufRData = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(strDataIn)), "UTF-8"));
		String strItem = bufRData.readLine();
		while (strItem != null)
		{
			String [] strTemp = strItem.split("\t");
			LexiconItem newLexItem = new LexiconItem(strTemp[0], strTemp[1],
					strTemp[2], strTemp[3], strTemp[4], strTemp[5]);
			newLexItem.frequency = Integer.parseInt(strTemp[6]);
			lexItemList.add(newLexItem);
			strItem = bufRData.readLine();
		}
		bufRData.close();
		return lexItemList;		
	}
	
	/**
	 * The lexicon2dat method to write the processed lexicon into the final
	 * file "lexicon-DC-final.dat".
	 */
	public void lexicon2Dat(ArrayList<LexiconItem> lex, 
			String strFileOut) throws Exception
	{
		BufferedWriter bufW = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(strFileOut), "UTF-8"));
		for (int i = 0; i < lex.size(); i++)
		{
			bufW.write(lex.get(i).toString());
			bufW.newLine();
		}
		
		bufW.close();
	}
	
	/**
	 * The getNewLexicon method is to use a XML parser to read the 
	 * "ANNODB_DC-new.xml". The we got the contents of the "<Pros>", "<Cons>", 
	 * "<Generals>","<sentences>" "<Comment>" and "<Paragraph>"
	 * Return an ArrayList of LexiconItem include all the contents needed to 
	 * match the "lexicon-DC-temp.dat".
	 */
	public ArrayList<LexiconItem> getNewLexicon(String strFile) throws Exception
	{
		ArrayList<LexiconItem> LexItemList = new ArrayList<LexiconItem>();	
		SAXBuilder builder = new SAXBuilder();
		Document docR = builder.build(new File(strFile));
		Element revs = docR.getRootElement();
		List    listRev = revs.getChildren();
		
		for (int i = 0; i < listRev.size(); i++)
		{
			Element e = (Element)listRev.get(i);
			Element ePros = e.getChild("Pros");
			Element eCons = e.getChild("Cons");
			Element eGens = e.getChild("Generals");
			
			List<Element> sentenceList = (List)new ArrayList<Element>();
			List<Element> commentList = (List)new ArrayList<Element>();
			
			if ((ePros = ePros.getChild("Paragraph")) != null)
			{
				if ((sentenceList = ePros.getChildren("Sentence")) != null)
				{
					for (int j = 0; j < sentenceList.size(); j++)
					{
						commentList = sentenceList.get(j)
								.getChildren("Comment");
						if(commentList != null)
						{
							addToLexList(LexItemList, commentList);
						}						
					}
				}
			}
			
			if ((eCons = eCons.getChild("Paragraph")) != null)
			{
				if ((sentenceList = eCons.getChildren("Sentence")) != null)
				{
					for (int j = 0; j < sentenceList.size(); j++)
					{
						commentList = sentenceList.get(j)
								.getChildren("Comment");
						if(commentList != null)
						{
							addToLexList(LexItemList, commentList);
						}						
					}
				}
			}
			
			if ((eGens = eGens.getChild("Paragraph")) != null)
			{
				if ((sentenceList = eGens.getChildren("Sentence")) != null)
				{
					for (int j = 0; j < sentenceList.size(); j++)
					{
						commentList = sentenceList.get(j)
								.getChildren("Comment");
						if(commentList != null)
						{
							addToLexList(LexItemList, commentList);
						}						
					}
				}
			}									
		}
		return LexItemList;
	}
	
	/**
	 * The addToLexList method is called by the getNewLexicon. It mainly to add
	 * the every content such Attribute into the LexiconItem ArrayList.
	 * Return the ArrayList.
	 */
	private void addToLexList(ArrayList<LexiconItem> LexItemList, 
							  List<Element> commentList) throws Exception
	{
		int index = 0;
		String strKeyword = new String();
		String strComp = new String();
		String strFuncs = new String();
		String strPhe = new String();
		String strAttr = new String();
		String strPol = new String();
		String strNeg = new String();
		
		for (int i = 0; i < commentList.size(); i++)
		{
			strKeyword = commentList.get(i).getAttribute("opinion_keyword")
					.getValue();
			strComp = getStdAttr(commentList.get(i)
						.getAttribute("std_component").getValue());
			strFuncs = getStdAttr(commentList.get(i)
					.getAttribute("std_function").getValue());
			strPhe = getStdAttr(commentList.get(i)
					.getAttribute("std_phenomenon").getValue());
			strAttr = getStdAttr(commentList.get(i)
					.getAttribute("std_attribute").getValue());
			strPol = commentList.get(i).getAttributeValue("negation");
			strNeg = commentList.get(i).getAttributeValue("negation");
			
			if (!strNeg.equals(""))
			{
				if (strPol.equals("1"))
				{
					strPol = "-1";
				}
				else if (strPol.equals("-1")) 
				{
					strPol = "1";
				}
			}
			for (index = 0; index < LexItemList.size(); index++)
			{
				boolean IsKeywordMatched = strKeyword.equals(LexItemList
						.get(index).strKeyword);
				boolean IsCompMatched = strComp.equals(LexItemList.get(index)
						.strComp);
				boolean IsFuncsMatched = strFuncs.equals(LexItemList.get(index)
						.strFuncs);
				boolean IsPheMatched = strPhe.equals(LexItemList.get(index)
						.strPhe);
				boolean IsAttrMatched = strAttr.equals(LexItemList.get(index)
						.strAttr);
				boolean IsPolMatched = strPol.equals(LexItemList.get(index)
						.strPol);
				
				if (IsKeywordMatched && IsCompMatched && IsFuncsMatched 
						&& IsPheMatched && IsAttrMatched && IsPolMatched)
				{
					break;
				}						
			}
			if (index < LexItemList.size())
			{
				LexItemList.get(index).frequency++;
			}
			else
			{
				LexItemList.add(new LexiconItem(strKeyword, strComp, strFuncs,
												strPhe, strAttr, strPol));
			}
		}
	}
	/**
	 * The getStdAttr method is called by the addToLexList method to help get
	 * the attribute of the "<content>" flag and convert it into string.
	 * Return the string.
	 */
	private String getStdAttr(String str)
	{
		if (str.equals(""))
			return "null";
		else if (!str.contains(" ")) 
		{
			return str;
		}
		else
		{
			String[] temp = str.split(" ");
			return temp[1];
		}
			
	}
	
	/**
	 * The sort method is called by the adjustLexicon method to sort the 
	 * LexiconItem ArrayList by the frequency.
	 */
	public void sort(ArrayList<LexiconItem> lexItemList)
	{
		int index = 0;
		LexiconItem tempItem;
		for (int i = 0; i < lexItemList.size(); i++)
		{
			index = findMax(lexItemList, i);
			if(index != i)
			{
				tempItem = lexItemList.get(index);
				lexItemList.set(index, lexItemList.get(i));
				lexItemList.set(i, tempItem);
			}
		}		
	}
	
	
	/**
	 * The findMax method is called by the sort method to just find the
	 * Max frequency. 
	 * Return the value.
	 */
	private int findMax(ArrayList<LexiconItem> lexItemList, int strt)
	{
		int maxIndex;
		for (maxIndex = strt; strt < lexItemList.size(); strt++)
		{
			if (lexItemList.get(maxIndex).frequency < lexItemList.get(strt)
					.frequency)
			{
				maxIndex = strt;
			}
		}
		return maxIndex;
	}
	
	/**
	 * The arrange method is to cluster the LexiconItems with the same keyword.
	 * Return a new ArrayList arranged.
	 */ 
	private ArrayList<LexiconItem> arrange(ArrayList<LexiconItem> lex)
	{
		sort(lex);
		ArrayList<LexiconItem> arrangedLex = new ArrayList<LexiconItem>();
		while (lex.size() > 0)
		{
			String strOpnKey = lex.get(0).strKeyword;
			for (int i = 0; i < lex.size(); i++)
			{
				if (lex.get(i).strKeyword.equals(strOpnKey))
				{
					arrangedLex.add(lex.get(i));
					lex.remove(i);
					i--;
				}		
			}
		}
		return arrangedLex;
	}
	
	/**
	 * The replaceNull method deals with the item that it has four null(strComp,
	 * strFuncs, strPhe, strAttr). If the opinion keyword in the lexOld Item
	 * matched that in the lexNew Item, then set the four "null" with the value
	 * of the corresponding position in the new lexNew Item.
	 */
	private void replaceNull(ArrayList<LexiconItem> lexOld, 
						     ArrayList<LexiconItem> lexNew)
	{
		for (int i = 0; i < lexOld.size(); i++)
		{
			if (lexOld.get(i).IsInNull())
			{
				for (int j = 0; j < lexNew.size(); j++)
				{
					if (lexOld.get(i).IsMatched(lexNew.get(j)))
					{
						lexOld.set(i, lexNew.get(j));
					}
				}
			}
		}
	}
	
	/**
	 * The removeNull method is called after the replaceNull. Its function is
	 * to remove the remaining lexItem processed by replaceNull method
	 *  which still has four nullstrComp,strFuncs, strPhe, strAttr).
	 */
	public void removeNull(ArrayList<LexiconItem> lexItemList)
	{
		for (int i = 0; i < lexItemList.size(); i++)
		{
			if (lexItemList.get(i).IsInNull())
			{
				lexItemList.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * The LexiconItem class is to read the every element of every line in the
	 * "lexicon-DC-temp.dat" file. It totals 7 parameters. We rewrite the 
	 * Construct method to get the required parameters. It also has several
	 * simple methods just to make some string judge.
	 */
	private class LexiconItem{
		public String strKeyword; //Opinion keyword.
		public String strComp;    //Component
		public String strFuncs;   //Functions
		public String strPhe;     //Phenomenon
		public String strAttr;    //Attributes
		public String strPol;     //Polarity
		public int    frequency;  //frequency
			
		/**
		 * The Construct method to get the needed parameters.
		 */
		public LexiconItem(String strKeywordIn, String strCompIn, 
				String strFuncsIn, String strPheIn, String strAttrIn, 
				String strPolIn)
		{
			this.strKeyword = strKeywordIn;
			this.strComp = strCompIn;
			this.strFuncs = strFuncsIn;
			this.strPhe = strPheIn;
			this.strAttr = strAttrIn;
			this.strPol = strPolIn;
			this.frequency = 1;			
		}
		
		/**
		 * IsInNull method is to judge if the input string is null.
		 * Return a boolean variable to identify.
		 */
		public boolean IsInNull()
		{
			boolean IsCompNull = strComp.equals("null");
			boolean IsFuncsNull = strFuncs.equals("null");
			boolean IsPheNull  = strPhe.equals("null");
			boolean IsAttrNull = strAttr.equals("null");
			if (IsCompNull && IsFuncsNull && IsPheNull && IsAttrNull)
			{
				return true;
			}
			else
			{
				return false;
			}			
		}
		
		/**
		 * IsMatched method is to judge if the opinion keyword is matched.
		 * Return a boolean variable to identify.
		 */
		public boolean IsMatched(LexiconItem lex)
		{
			if (this.strKeyword.equals(lex.strKeyword))
			{
				return true;
			}
			else
			{
				return false;
			}			
		}
		
		/**
		 * toString method is to convert the every item of the LexiconItem to
		 * a new string.
		 * Return the new string.
		 */
		public String toString()
		{
			String strRes = this.strKeyword + "\t" + this.strComp + "\t" 
					+ this.strFuncs + "\t" + this.strPhe + "\t" + this.strAttr
					+ "\t" + this.strPol + "\t" 
					+ String.valueOf(this.frequency);
			return strRes;
		}		
	}
}
