/*
 * @(#) Roberto.java
 * 
 * Tern Tangible Programming Language
 * Copyright (c) 2011 Michael S. Horn
 * 
 *           Michael S. Horn (michael.horn@tufts.edu)
 *           Northwestern University
 *           2120 Campus Drive
 *           Evanston, IL 60613
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2) as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package tidal.tern;

import java.util.ArrayList;
import java.util.Map;

import tidal.tern.rt.Debugger;
import tidal.tern.rt.Process;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * This is an "robot" interface file. The interpreter calls
 * functions on this class when it runs tern code. In turn,
 * this object displays animations of Roberto on the screen.
 */
public class Roberto extends View implements Debugger {
   
   public final String TAG = "Roberto";
   
   /** Name of the current action */
   protected String message = null;
   
   /** Roberto picture to draw */
   protected Drawable pose = null;
   
   /** Is there a program running */
   protected boolean running = false;
   
   /** Touch sensor latch */
   protected boolean tsensor = false;
   
   /** Link back to the main activity */
   protected Tern tern = null;
   
   /** Duration of frames in milliseconds */
   private final int Duration = 200;
	
   /** Is the animation playing */
   private boolean isPlaying = false;
   
   /** Populate the frames in a list of Drawables */
   private ArrayList<Drawable> DrawableList = new ArrayList<Drawable>();
   
   /** List of strings to hold the program */
   private ArrayList<String> sequenceList = new ArrayList<String>();
   
   /** Store the number of frames for each pose */
   private Map<String, Integer> frameCount = new java.util.HashMap<String, Integer>();
   
   /** List counter */
   private int listCounter = 0;
   
   /** Current frame index to be drawn */
   private int frame = 0;
   
   private long last_tick = 0;
   
   /** Is there a new pose to be drawn */
   private boolean newPose = true;
   
   /** Replay button is pressed */
   protected boolean reIntrep = false;
   
   private boolean change = true;
   
   private SoundPool sounds;
   private int beepSound;
   private Drawable entry = null;
   private Drawable logo;
   private Drawable button;
   private String currentPose = null;
   
   
   public Roberto(Context context) {
      super(context);
   }
   
   
   public Roberto(Context context, AttributeSet attribs) {
      super(context, attribs);
      sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
      beepSound = sounds.load(context, R.raw.beep, 1);
      Resources res = getContext().getResources();
      logo = res.getDrawable(R.drawable.logo);
      button = res.getDrawable(R.drawable.play_button_up);

      //populate the number of frames for each pose...
      frameCount.put("walk", 5);
      frameCount.put("jump", 5);
      frameCount.put("spin", 6);
      frameCount.put("wiggle", 6);
      frameCount.put("sleep", 1);
      frameCount.put("run", 1);
      frameCount.put("stand", 1);
      frameCount.put("yawn", 1);
      frameCount.put("sit", 1);
      frameCount.put("replay",1);
      
   }
   
   
   public void setTern(Tern tern) {
      this.tern = tern;
   }
   
   
   public boolean onTouchEvent(MotionEvent event) {
      int action = event.getAction();
      if (action == MotionEvent.ACTION_DOWN) {
         this.tsensor = true;
         if (reIntrep) { clearAnimation(); tern.finishCompile(true); }
		   
      } else if (action == MotionEvent.ACTION_UP) {
         if (! running ) tern.onClick(this);
      }
      
      return true;
   }

   
   protected void onDraw(Canvas canvas) {
	   
      int w = getWidth();
      int h = getHeight();
      int dx, dy, dw, dh;
      float ds;
      
      Drawable current = null;

      // clear background 
      canvas.drawRGB(210, 210, 210);
      
      if (!this.running) {
    	 
         // draw logo
         dw = logo.getIntrinsicWidth();
         dh = logo.getIntrinsicHeight();
         ds = Math.min(0.8f, 0.8f * w / dw);
         dw *= ds;
         dh *= ds;
         logo.setBounds(w/2 - dw/2, 70, w/2 + dw/2, 70 + dh);
         logo.draw(canvas);

         // draw button
         dw = button.getIntrinsicWidth();
         dh = button.getIntrinsicHeight();
         dx = w - dw - 10;
         dy = h - dh - 10;
         button.setBounds(dx, dy, dx + dw, dy + dh);
         button.draw(canvas);  
      }
      
      // Draw roberto 
      else if (isPlaying) {
    	  change = false;
    	  
    	  /** if the program has a new pose, get it's name and number of frames, 
    	  	  and populate a list of drawables to animate the new pose. */
    	  
    	  if (newPose) {
    		  currentPose = sequenceList.get(listCounter);
    		  int total = frameCount.get(currentPose);
    		  Log.i(TAG, currentPose + " " + total);
    		  listCounter++;
    		  frame = 0;
    		  DrawableList.clear();
    		  
    		  for (int x=1; x <= total; x++) {
    			  String name = currentPose + "0" + x;
    			  int res_id = getContext().getResources().getIdentifier(name, "drawable", "tidal.tern");
    			  entry = getContext().getResources().getDrawable(res_id);
    			  try { DrawableList.add(entry); }
    			  catch (Exception r) { Log.i(TAG, "Not able to add to list: " + name);}
    			  entry.setCallback(null);
    			  entry = null;
			   }
    		  
    		  newPose = false;
    		  
    		  sounds.play(beepSound, 1.0f, 1.0f, 0, 0, 1.0f);
    	  }
    	  
    	  long elapsed = (System.currentTimeMillis() - last_tick);
    	  
          if (elapsed >= Duration) {
        	  if (frame < DrawableList.size()) {
        		  last_tick = System.currentTimeMillis();
        		  current = DrawableList.get(frame);
                  dw = current.getIntrinsicWidth() / 2;
                  dh = current.getIntrinsicHeight() / 2;
                  dx = w/2 - dw/2;
                  dy = h/2 - dh/2;
                  current.setBounds(dx, dy, dx + dw, dy + dh);
                  current.draw(canvas);
                  current.setCallback(null);
       	       	  current = null;
                  frame++;
                  
                  if (frame < DrawableList.size())//second check after increment to hold on to the last frame
                	  repaint();
                  
                  else { change = true; isPlaying = false; Log.i(TAG, "finished with "+currentPose); }
        	  }
        	  
        	  
            	  
          }
          
          else { //within duration
        	  if (frame < DrawableList.size()) {
        		  current = DrawableList.get(frame);
                  dw = current.getIntrinsicWidth() / 2;
                  dh = current.getIntrinsicHeight() / 2;
                  dx = w/2 - dw/2;
                  dy = h/2 - dh/2;
                  current.setBounds(dx, dy, dx + dw, dy + dh);
                  current.draw(canvas);
                  current.setCallback(null);
       	       	  current = null;
                  repaint();
        	  }     
          }
          
      }
      
      
          
      /** Because the interpreter runs very fast when "replaying" we need to add this "if" statement
          inside the onDraw method */
      
      if (listCounter < sequenceList.size() && change) {
  		  //get next pose
    	  change = false;
  		  isPlaying = true;	   
  		  newPose = true;	  
  		  Log.i(TAG, "called inside onDraw");
  		  repaint(); 
      }
      
     
   }
   
   
   
/**
 * Thread safe invalidate function
 */
   public void repaint() {
      repaintHandler.sendEmptyMessage(0);
   }
   
   
   private Handler repaintHandler = new Handler() {
      @Override public void handleMessage(Message msg) {
         invalidate();
      }
   };
   
   
   protected void changePicture(String img, int f) {
	   if (f>0) { 
		   sequenceList.add(img);
		   Log.i(TAG,"ADDED " + img + " sequence size = "+ sequenceList.size());
		   Log.i(TAG, "listCounter = " + listCounter);
		   //isPlaying = true;
		   //newPose = true;
	   }
	   
	   
	   /** repaint if we have an additional pose and ONLY after the previous pose is fully animated,
       not confirming that the previous pose is completed will result in overwriting the list,
       resulting in cutting the number of frames. */
	   
	   if (listCounter < sequenceList.size() && change) {
	  		isPlaying = true;	   
	  		newPose = true;	   
	  		Log.i(TAG,"called inside changePic");
	  		repaint();
	  	}
	 
   }
     
   
   public int doJump(int [] args) {
	   changePicture("jump",5);     	   
       return 0;
   }
   
   
   public int doRun(int [] args) {
	   changePicture("run",1);   
       return 0;
   }
   
   
   public int doWalk(int [] args) {
	  changePicture("walk",5);	   
      return 0;
   }
   
   
   public int doWiggle(int [] args) {
	  changePicture("wiggle",6); 	   
      return 0;
   }
   
   
   public int doSleep(int [] args) {
	   changePicture("sleep",1);   	   
       return 0;
   }
   
   
   public int doSit(int [] args) {
	   changePicture("yawn",1); 
       return 0;
   }
   
   
   public int doYawn(int [] args) {
	   changePicture("yawn",1);   	   
       return 0;
   }
   
   
   public int doStand(int [] args) {
	   changePicture("stand",1);  	   
       return 0;
   }
   
   
   public int doSpin(int [] args) {
	   changePicture("spin",6);   	   
       return 0;
   }
   
   
   public int doDance(int [] args) {
       return 0;
   }
   
   
   public int getTouchSensor(int [] args) {
      int result = tsensor ? 1 : 0;
      tsensor = false;
      return result;
   }
      
   
   public void processStarted(Process p) {
      this.running = true;
      this.tsensor = false;
   }
   
   public void processStopped(Process p) {
	   Log.i(TAG,"processStopped Called with sequence size = " + sequenceList.size());
	   reIntrep = true;
	   changePicture("replay",1);
   }
   
   public void clearAnimation() {
	   sequenceList.clear();
	   DrawableList.clear();
	   listCounter = 0;
	   frame = 0;
	   reIntrep = false;
	   tern.interp.clear();
   }
   
   public void trace(Process p, String message) { }
   
   public void print(Process p, String message) { }
   
   public void error(Process p, String message) {
      Log.i(TAG, message);
      clearAnimation();
      this.running = false;
      
   }
}