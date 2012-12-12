package com.example.nextmove;

public class Square {
	int index;
	String name;
	String piece;
	public int average_color;
	public int average_interferance;
	public boolean real_color;
	public Square(int index) {
		int row = index/8;
		int col = index%8;
		char a = 'a';
		a+=row;
		this.name = Character.toString(a)+Integer.toString(col+1);
		this.piece = "";
		this.average_color=0;
		this.average_interferance=0;
		this.real_color=true;
	}
	
	public String debug_string() {
		String string = "";
		string+=this.average_color;
		string+=",";
		string+=this.average_interferance;
		string+=",";
		string+=this.real_color;
		return string;
	}
}
