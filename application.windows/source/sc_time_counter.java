import processing.core.*; 
import processing.xml.*; 

import processing.opengl.*; 
import fullscreen.*; 
import japplemenubar.*; 
import org.json.*; 
import java.util.Timer; 
import java.util.TimerTask; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class sc_time_counter extends PApplet {










PFont myFont;
String baseURL = "http://api.soundcloud.com";
String apiKey = "3L8SgEELZydxJ59AdLUg";
Timer myTimer;
FullScreen fs; 
int sum = 0;
Integrator animatedSum = new Integrator(0, 0.5f, 0.01f);
HashMap ids = new HashMap();
String startStr;
String[] months = {
  "January","February","March","April","May","June","July","August","September","October","November","December"};
PImage fade;

int[] durations = {
};
String[] titles = {
};



public void setup(){
  size(1280, 720);
  frameRate(30);
//  myFont = createFont("Helvetica-Bold", 68, true);
  myFont = loadFont("Helvetica-Bold-68.vlw");
  //  getData();
  myTimer = new Timer();
  myTimer.scheduleAtFixedRate(new TimerTask() {
    public void run() {
      try{
        getData();
      }catch (Exception e) {
      println ("The SoundCloud api is not reacheable!");
      print(e);
    };
      
    }
  }
  , 0, 25 * 1000);
  background(0);
  fade = get(0,0, width, height);
  // Create the fullscreen object
  fs = new FullScreen(this); 

  startStr = day() + "\u00A0" + months[month() - 1] + "\u00A0" + nf(hour(), 2) + ":" + nf(minute(), 2);

  smooth();

};

int titleAlpha;
String lastTitle = "";

public void draw(){
  background(0);
  tint(255, 255, 255, 249); 
  image(fade, 0, 0, width, height);
  if(titles.length > 0 && frameCount % 60 == 0){
    lastTitle = titles[titles.length - 1];
    titleAlpha = 150;
    titles = shorten(titles);
  }

  if(lastTitle.length() > 0 && titleAlpha > 0){
    fill(255, titleAlpha);
    textFont(myFont, 45);
    text(lastTitle, 40, 200);
    titleAlpha--;
  }

  // save the background
  //  fade = get(0,0, width, height);

  // keep interpolation runing
  animatedSum.update();

  String txt = "";
  if(sum > 0){
    txt = timeString(floor(animatedSum.value)) + "of audio uploaded to SoundCloud since " + startStr;
  }
  else{
    txt = "waiting for uploads...";
  }
  fill(250);
  textFont(myFont, 68);
  text(txt, 40, height/2 - 19, width - 80, height/2);

};

public void getData() {
  println("getData");


  try {
    String request = baseURL + "/tracks.json?consumer_key=" + apiKey;
    JSONArray data = new JSONArray(join(loadStrings(request), ""));
    println("loaded!");
    parseData(data);
  }
  catch (JSONException e) {
    println ("There was an error parsing the JSONObject.");
    print(e);
  };

};

boolean isNew = false;

public void parseData(JSONArray data){


  for(int i = 0; i < data.length(); i++){
    try{
      JSONObject t = data.getJSONObject(i);
      String id = t.getString("id");
      // save only the tracks we hadn't added before
      if(isNew && !ids.containsKey(id)){
        durations = append(durations, t.getInt("duration"));
        titles = append(titles, t.getString("title"));
        sum +=  t.getInt("duration");
      }
      ids.put(id, id);
    } 
    catch (JSONException e) {
      println ("There was an error parsing the JSONObject.");
      print(e);
    };

  }
  isNew = true;
  animatedSum.target(sum);
};

// format the timecode string
public String timeString(int current){

  int s = floor((current/1000) % 60);
  int m = floor((current/60000) % 60);
  int h = floor((current/(60*60*1000)) % 24);
  int d = floor((current/(24*60*60*1000)) % 30);
  int mo = floor((current/(30*24*60*60*1000)) % 12);
  int y = floor((current/(12*30*24*60*60*1000))); // appoximate :)

  String timeStr = "";

  //  if(s > 0){
  timeStr = nf(s, 2) + (s == 1 ? "\u00A0seconds " : "\u00A0seconds ") + timeStr;
  //  }

  if(m > 0){
    timeStr = m + (m == 1 ? "\u00A0minute " : "\u00A0minutes ") + timeStr;
  }

  if(h > 0){
    timeStr = h + (h == 1 ? "\u00A0hour " : "\u00A0hours ") + timeStr;
  }

  if(d > 0){
    timeStr = str(d) + (d == 1 ? "\u00A0day " : "\u00A0days ") + timeStr;
  }

  if(mo > 0){
    timeStr = str(mo) + (mo == 1 ? "\u00A0month " : "\u00A0months ") + timeStr;
  }

  if(y > 0){
    timeStr = str(y) + (y == 1 ? "\u00A0year " : "\u00A0years ") + timeStr;
  }

  return timeStr;
};



public void mouseClicked(){
  // enter fullscreen mode
  if(fs.isFullScreen()){
    fs.leave();
  }
  else{
    fs.enter(); 
  }

}


























class Integrator {

  final float DAMPING = 0.5f;
  final float ATTRACTION = 0.2f;

  float value;
  float vel;
  float accel;
  float force;
  float mass = 1;

  float damping = DAMPING;
  float attraction = ATTRACTION;
  boolean targeting;
  float target;


  Integrator() { }


  Integrator(float value) {
    this.value = value;
  }


  Integrator(float value, float damping, float attraction) {
    this.value = value;
    this.damping = damping;
    this.attraction = attraction;
  }


  public void set(float v) {
    value = v;
  }


  public void update() {
    if (targeting) {
      force += attraction * (target - value);      
    }

    accel = force / mass;
    vel = (vel + accel) * damping;
    value += vel;

    force = 0;
  }


  public void target(float t) {
    targeting = true;
    target = t;
  }


  public void noTarget() {
    targeting = false;
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "sc_time_counter" });
  }
}
