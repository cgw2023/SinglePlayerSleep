package com.github.joelgodofwar.sps.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StrUtils {
	/** StringRight */
	public static String Right(String input, int chars){
		if (input.length() > chars){
			//System.out.println("Right input=" + input);
			return input.substring(input.length() - chars);
		} 
		else{
			return input;
		}
	}
	
	/** StringLeft */
	public static String Left(String input, int chars){
		if (input.length() > chars){
			//System.out.println("Left input=" + input);
			//System.out.println("Left chars=" + chars);
			return input.substring(0, chars);
		} 
		else{
			return input;
		}
	}
	// 񗉱񗉱񗉱D񗝞񗝞񗝞e񗱋񗱋񗱋M񘄸񘄸񘄸o񘘥񘘥񘘥N
	public static String parseRGBNameColors(String string){
		String nickPrefix = "\"}";
		String nickString = ",{\"text\":\"<text>\",\"color\":\"<color>\"}";
		String nickSuffix = ",{\"text\":\"";
		//String[] string2 = null;
		List<String> string2 = new ArrayList<String>();
		String string3 = "";
		String color;
		String text;
		//int counter = 0;
		int index;
		if(string.contains("")){
			int count = StringUtils.countMatches(string, "");
			//System.out.println("count=" + count);
			for(int i = 0; i < count; i++){//while(string.length() > 0){
				index = string.indexOf("");
				//System.out.println("i=" + i);
				//System.out.println("index=" + index);
				//System.out.println("index2=" + string.indexOf("", index + 1));
				int index2 = string.indexOf("", index + 1);
				if(index2 <= 1){
					string2.add(Left(string, string.length()));
				}else{
					string2.add(Left(string, index2)); // 񗉱񗉱񗉱D
				}
				string = string.replace(string2.get(i), "");
			}
			for(int i = 0; i < count; i++){
			    color = Left(string2.get(i), 14).replace("", "#").replace("�", "");
			    text = Right(string2.get(i), string2.get(i).length() - 14);
			    string3 = string3 + nickString.replace("<color>", color).replace("<text>", text);
			}
			return nickPrefix + string3 + nickSuffix;
		}
		return string;
	}
}
