package com.example.nextmove;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import com.example.nextmove.R;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import chess.ChessParseError;
import chess.History;
import chess.Move;
import chess.MoveGen;
import chess.Position;
import chess.Search;
import chess.TextIO;
import chess.TranspositionTable;

public class NextMove extends Activity {
	Square[][] piece_array = null;
	boolean reset_view = false;
	
	int min_thresh = 210;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == 0) && (resultCode == RESULT_OK) && (data!=null)) {
			processPhoto(data);
		}
	}
	
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}

	private ImageView mImageView = null;
	Bitmap original = null;
	
	private boolean not_in(Vector<HoughLine> seen, HoughLine line){
		for(int i=0; i<seen.size(); i++){
			HoughLine line2= seen.get(i);
			if(line2.theta-line.theta<0.05){
				if(line2.r-line.r<5) {
					return true;
				}
			}
		}
		return true;
	}
	
	private void processPhoto(Intent intent) {
    	mImageView = new ImageView(this);
    	reset_view = true;
	    Bundle extras = intent.getExtras();
	    Bitmap mImageBitmap = (Bitmap) extras.get("data");
	    original = mImageBitmap;
	    CannyEdgeDetector detector = new CannyEdgeDetector();
	    detector.setSourceImage(mImageBitmap);
	    detector.process();
	    mImageBitmap = detector.getEdgesImage();
	    HoughTransform h = new HoughTransform(mImageBitmap.getWidth(), mImageBitmap.getHeight()); 
     
        // add the points from the image (or call the addPoint method separately if your points are not in an image 
        h.addPoints(mImageBitmap); 
 
        // get the lines out 
        Vector<HoughLine> lines = h.getLines(15); 
        Vector<HoughLine> seen = new Vector<HoughLine>();
        int horiz=0;
        int vert = 0;
        // draw the lines back onto the image
        for (int j = 0; j < lines.size(); j++) {
            HoughLine line = lines.elementAt(j);
            if(line.horizontal() && not_in(seen, line)){
            	horiz++;
            	if(horiz<10){
            		seen.add(line);
                    line.draw(mImageBitmap, Color.RED);
            	}
            }
            if(line.vertical() && not_in(seen, line)){
            	vert++;
            	if(vert<10){
            		seen.add(line);
                    line.draw(mImageBitmap, Color.RED);
            	}
            }
        } 
        Vector<Vector<Integer>> intersections = new Vector<Vector<Integer>>();
        for(int i=0; i<seen.size(); i++){
        	HoughLine line = seen.elementAt(i);
        	for(int j=0; j<seen.size(); j++){
        		if(j!=i){
        			HoughLine line2 = seen.elementAt(j);
        			if(line.vertical()){
        				if(line2.horizontal()){
        					Vector<Integer> point = new Vector<Integer>();
        					int x = line.getX(mImageBitmap);
        					int y = line2.getY(mImageBitmap);
        					point.add(x);
        					point.add(y);
        					intersections.add(point);
        				}
        			}
        		}
        	}
        }
        for(int i=0; i<intersections.size(); i++){
        	Vector<Integer> point = intersections.get(i);
        	int x = point.get(0);
        	int y = point.get(1);
        	if(((x+1) < mImageBitmap.getWidth()) && ((x-1) >0) && ((y+1)<mImageBitmap.getHeight()) && ((y-1)>0)){
	        	mImageBitmap.setPixel(x, y, Color.GREEN);
	        	mImageBitmap.setPixel(x-1, y, Color.GREEN);
	        	mImageBitmap.setPixel(x+1, y, Color.GREEN);
	        	mImageBitmap.setPixel(x, y-1, Color.GREEN);
	        	mImageBitmap.setPixel(x, y+1, Color.GREEN);
        	}
        }
        Collections.sort(intersections, new Comparator<Vector<Integer>>(){
			public int compare(Vector<Integer> lhs, Vector<Integer> rhs) {
				if(lhs.get(0)>rhs.get(0)){
					return 1;
				} else if(lhs.get(0)<rhs.get(0)) {
					return -1;
				} else if(lhs.get(1)>rhs.get(1)) {
					return 1;
				} else if(lhs.get(1)<rhs.get(1)) {
					return -1;
				} else {
					return 0;
				}
			}
        });
        int side1 = 0;
        int first = intersections.get(0).get(0);
        while(intersections.get(side1).get(0)==first){
        	side1++;
        }
        int side2 = intersections.size()/side1;
        System.out.println(side1);
        System.out.println(side2);
        for(int k=0; k<side1-1; k++) {
            for(int l=0; l<side2-1; l++) {
            	boolean white = ((k%2 ==1) && (l%2==0)) || ((k%2==0)&&(l%2==1));
            	int box_num=k+l*(side1);
	        	int box_x = intersections.get(box_num).get(0);
		        System.out.println("x:"+Integer.toString(box_x));
	        	int box_y = intersections.get(box_num).get(1);
		        System.out.println("y:"+Integer.toString(box_y));
	        	int box_width = intersections.get(box_num+side1).get(0)-box_x;
		        System.out.println("width:"+Integer.toString(box_width));
	        	int box_height = intersections.get(box_num+1).get(1)-box_y;
		        System.out.println("height:"+Integer.toString(box_height));
		        //for each box, avereage pixels
		        double average=0;
		        double average2=0;
		        int acount = 0;
		        for(int i=0; i<box_width; i++){
		        	for(int j=0; j<box_height; j++) {
		        		int color=original.getPixel(i+box_x, j+box_y);
		        		int color_sum = Color.red(color) + Color.blue(color) + Color.green(color);
		        		average+= color_sum;
		        		color=mImageBitmap.getPixel(i+box_x, j+box_y);
		        		color_sum = Color.red(color) + Color.blue(color) + Color.green(color);
		        		average2+= color_sum;
		        		acount++;
		        	}
		        }
		        average/=acount;
		        average2/=acount;
        		piece_array[k][l].average_color=(int)average;
        		piece_array[k][l].real_color=white;
        		piece_array[k][l].average_interferance=(int)average2;
        		piece_array[k][l].piece="";
        		if(average2>min_thresh+90) {//king
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="K";
        				} else { //black piece
        	        		piece_array[k][l].piece="k";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="K";
        				} else { //black piece
        	        		piece_array[k][l].piece="k";
        				}
        			}
        		}
        		else if(average2>min_thresh+80) {//queen
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="Q";
        				} else { //black piece
        	        		piece_array[k][l].piece="q";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="Q";
        				} else { //black piece
        	        		piece_array[k][l].piece="q";
        				}
        			}
        		}
        		else if(average2>min_thresh+70) {//bishop
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="B";
        				} else { //black piece
        	        		piece_array[k][l].piece="b";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="B";
        				} else { //black piece
        	        		piece_array[k][l].piece="B";
        				}
        			}
        		}
        		else if(average2>min_thresh+60) {//knight
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="N";
        				} else { //black piece
        	        		piece_array[k][l].piece="n";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="N";
        				} else { //black piece
        	        		piece_array[k][l].piece="n";
        				}
        			}
        		}
        		else if(average2>min_thresh+50) {//rook
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="R";
        				} else { //black piece
        	        		piece_array[k][l].piece="r";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="R";
        				} else { //black piece
        	        		piece_array[k][l].piece="r";
        				}
        			}
        		}
        		else if(average2>min_thresh) {//pawn
        			if(white){ //white square
        				if(average>500){ //white piece
        	        		piece_array[k][l].piece="P";
        				} else { //black piece
        	        		piece_array[k][l].piece="p";
        				}
        			} else {
        				 //black square
        				if(average>300){ //white piece
        	        		piece_array[k][l].piece="P";
        				} else { //black piece
        	        		piece_array[k][l].piece="p";
        				}
        			}
        		}
		        System.out.println("box:"+Integer.toString(box_num));
		        System.out.println("average:"+Double.toString(average));
            }
		}
        
        mImageBitmap = Bitmap.createScaledBitmap(mImageBitmap, 400, 600, true);
        mImageView.setImageBitmap(mImageBitmap);
	}
	
	private void dispatchTakePictureIntent(int actionCode) {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(takePictureIntent, actionCode);
	}
	
	public String to_color(String text){
		return "";
	}
	
	public String to_piece(String text, String color){
		if(color.equals("White")){
			if(text.equals("Rook")){
				return "R";
			} else if(text.equals("Pawn")) {
				return "P";
			} else if(text.equals("Knight")) {
				return "N";
			} else if(text.equals("Bishop")) {
				return "B";
			} else if(text.equals("Queen")) {
				return "Q";
			} else if(text.equals("King")) {
				return "K";
			}
		} else if(color.equals("Black")) {
			if(text.equals("Rook")){
				return "r";
			} else if(text.equals("Pawn")) {
				return "p";
			} else if(text.equals("Knight")) {
				return "n";
			} else if(text.equals("Bishop")) {
				return "b";
			} else if(text.equals("Queen")) {
				return "q";
			} else if(text.equals("King")) {
				return "k";
			}
		}
		return "";
	}
	
	public void change_title(Button b) {
        setContentView(R.layout.spinner1);
		final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		if(spinner == null) {
			Toast.makeText(getApplicationContext(), "could not load spinner", Toast.LENGTH_SHORT).show();
			return;
		}
		// Create an ArayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.piece_names, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
		// Create an ArayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
		        R.array.colors, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner2.setAdapter(adapter2);
		final int i = b.getId() / 8;
		final int j = b.getId() % 8;
		TextView text = (TextView) findViewById(R.id.textView2);
		text.setText(piece_array[i][j].debug_string());
        Button button2=(Button) findViewById(R.id.button1);
        button2.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		String text = spinner.getSelectedItem().toString();
        		String text2 = spinner2.getSelectedItem().toString();
        		if(!text.equals("No Piece")) {
        			String new_piece = to_piece(text, text2);
        			piece_array[i][j].piece=new_piece;
        		} else {
        			piece_array[i][j].piece="";
        		}
        		adjust_board();
        	}
        });
	}
	
	public void adjust_board() {
		LinearLayout linearLayout= new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		//add rows
		for(int i=0; i<8; i++) {
			LinearLayout arow = new LinearLayout(this);
			arow.setOrientation(LinearLayout.HORIZONTAL);
			arow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			for(int j=0; j<8; j++){
				Button square= new Button(this);
		        String piece = piece_array[i][j].piece;
		        square.setText(piece);
		        square.setTextSize(9);
		        square.setId(i*8+j);
		        square.setWidth(60);
		        square.setHeight(20);
		        square.setOnClickListener(new View.OnClickListener(){
		        	public void onClick(View v){
		        		change_title((Button)v);
		        	}
		        });
		        arow.addView(square);
			}
			linearLayout.addView(arow);
		}
		
		
		//add return button
		Button button1= new Button(this);
        button1.setText("Return"); 
        button1.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		setup_main_menu();
        	}
        });
        linearLayout.addView(button1);
		setContentView(linearLayout);
	}
	
	 private Move idSearch(Search sc, int maxDepth) {
	        MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(sc.pos);
	        MoveGen.removeIllegal(sc.pos, moves);
	        sc.scoreMoveList(moves, 0);
	        sc.timeLimit(-1, 10);
	        Move bestM = sc.iterativeDeepening(moves, maxDepth, -1, true);
	        return bestM;
	    }
	
	public void get_move() throws IOException {
		setContentView(R.layout.results);

	    final long[] nullHist = new long[200];
	    TranspositionTable tt = new TranspositionTable(4);
	    History ht = new History();
        Position pos = null;
        final Button button1=(Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		setup_main_menu();
        	}
        });
        TextView text = (TextView) findViewById(R.id.textView1);
		try {
			System.out.println(board_to_fen());
			pos = TextIO.readFEN(board_to_fen());
		} catch (ChessParseError e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			text.setText("Error.");
			return;
		}
		Search sc = new Search(pos, nullHist, 0, tt, ht);
        Move bestM = idSearch(sc, 2);
		String score = "Score: " + Integer.toString(bestM.score);
        TextView text2 = (TextView) findViewById(R.id.textView2);
        text2.setText(score);
		String result = bestM.toString();
		text.setText(result);
	}
	
	public void setup_main_menu() {
        setContentView(R.layout.front_page);
        final EditText et=(EditText) findViewById(R.id.editText1);
        et.setText(Integer.toString(min_thresh));
        et.setOnKeyListener(new View.OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				min_thresh=Integer.parseInt(et.getText().toString());
				return false;
			}
        });
        final Button button1=(Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		take_picture();
        	}
        });
        final Button button2=(Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		view_picture();
        	}
        });
        final Button button3=(Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		adjust_board();
        	}
        });
        final Button button4=(Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		try {
					get_move();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        });
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	piece_array = new Square[8][8];
    	for(int i=0; i<8; i++) {
    		for(int j=0; j<8; j++) {
    			piece_array[i][j] = new Square(i*8+j);
    		}
    	}
        super.onCreate(savedInstanceState);
        setup_main_menu();
    }
    
    public void take_picture() {
        //set image resource
        dispatchTakePictureIntent(0);
    }
    
    public String board_to_fen() {
    	String desc = "";
    	for(int i=0; i<8; i++) {
    		//add row
    		int count = 0;
    		for(int j=0; j<8; j++){
    			String piece = piece_array[j][8-i-1].piece;
    			if(piece.equals("")){
    				count++;
    			} else {
    				if(count>0) {
    					desc+=Integer.toString(count);
    					count=0;
    				}
    				desc+=piece;
    			}
    		}
    		if(count>0) {
    			desc+=Integer.toString(count);
    		}
    		if(i<7) {
    			desc+="/";
    		}
    	}
    	//append who to move
    	desc+=" w ";
    	//castling
    	desc+="KQkq ";
    	//enpassant
    	desc+="- ";
    	//append halfmoves since last capture
    	desc+="0 ";
    	//append total moves
    	desc+="1";
    	return desc;
    }
    
    public void view_picture() {
		LinearLayout linearLayout= new LinearLayout(this);
    	if(reset_view) {
    		reset_view=false;
    		linearLayout.setOrientation(LinearLayout.VERTICAL);
    		linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    		//add image
    		mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    		linearLayout.removeAllViews();
    		System.out.println("here1");
    		linearLayout.addView(mImageView);
    		System.out.println("here2");
    		//add return button
    	}
		Button button1= new Button(this);
        button1.setText("Return"); 
        button1.setOnClickListener(new View.OnClickListener(){
        	public void onClick(View v){
        		setup_main_menu();
        	}
        });
        linearLayout.addView(button1);
		setContentView(linearLayout);
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
 
}