package com.apkmaker.maker;
import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
 static final int IND=0xFF4F46E5,GRN=0xFF16A34A,GRY=0xFF667085,BG=0xFFF2F4F8;
 int mode;Uri src;String srcName="";Bitmap logo;
 final String[][] P={{"camera","📷  Camera"},{"mic","🎤  Microphone"},{"location","📍  Location (GPS)"},{"storage","🖼️  Gallery / Files"},{"notifications","🔔  Notifications"},{"contacts","👤  Contacts"},{"phone","📞  Phone call"}};
 boolean[] on={true,true,true,true,true,false,false};
 boolean fs=false,awake=false,portrait=false;
 String nameV="My App",urlV="";
 EditText nameE,urlE;Uri savedUri;

 int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);}
 LinearLayout page(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(16),dp(40),dp(16),dp(28));return l;}
 LinearLayout card(LinearLayout parent){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(12),dp(16),dp(12));
  GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setCornerRadius(dp(16));c.setBackground(g);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));c.setLayoutParams(p);parent.addView(c);return c;}
 Button btn(String t,int col){Button b=new Button(this);b.setText(t);b.setAllCaps(false);b.setTextSize(17);b.setTextColor(Color.WHITE);
  GradientDrawable g=new GradientDrawable();g.setColor(col);g.setCornerRadius(dp(14));b.setBackground(g);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));b.setLayoutParams(p);b.setPadding(dp(16),dp(20),dp(16),dp(20));return b;}
 TextView tv(String t,int sz,boolean bold){TextView v=new TextView(this);v.setText(t);v.setTextSize(sz);v.setTextColor(0xFF14171F);if(bold)v.setTypeface(null,Typeface.BOLD);v.setPadding(0,dp(5),0,dp(5));return v;}
 CheckBox cb(String t,boolean v,CompoundButton.OnCheckedChangeListener l){CheckBox c=new CheckBox(this);c.setText(t);c.setTextSize(16);c.setChecked(v);c.setOnCheckedChangeListener(l);return c;}
 void show(View v){ScrollView s=new ScrollView(this);s.setBackgroundColor(BG);s.addView(v);setContentView(s);}

 @Override protected void onCreate(Bundle b){super.onCreate(b);home();}

 void home(){
  mode=0;LinearLayout l=page();
  l.addView(tv("📦 APK Maker",28,true));l.addView(tv("Kya banana hai? Aik option chunein",14,false));
  String[] t={"📄  HTML to APK","🗜️  ZIP to APK","🌐  Website to APK"};
  String[] d={"Aik HTML file se app","Poori website ki ZIP se app","Website ke link se app"};
  for(int i=0;i<3;i++){final int m=i+1;Button x=btn(t[i]+"\n"+d[i],IND);x.setGravity(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL);
   x.setOnClickListener(v->{mode=m;src=null;srcName="";form();});l.addView(x);}
  show(l);
 }
 void form(){
  LinearLayout l=page();
  Button back=btn("← Wapas",0xFF98A2B3);back.setOnClickListener(v->{keep();home();});l.addView(back);
  l.addView(tv(mode==1?"HTML to APK":mode==2?"ZIP to APK":"Website to APK",24,true));
  LinearLayout c1=card(l);
  c1.addView(tv("1. App ka naam",13,false));nameE=new EditText(this);nameE.setText(nameV);c1.addView(nameE);
  urlE=null;
  if(mode<3){Button f=btn("📁 File choose karein",GRY);f.setOnClickListener(v->{keep();pickFile(1,"*/*");});c1.addView(f);
   c1.addView(tv(srcName.isEmpty()?"Abhi koi file nahi chuni":"✔ "+srcName,13,false));}
  else{c1.addView(tv("Website link",13,false));urlE=new EditText(this);urlE.setHint("https://example.com");urlE.setText(urlV);c1.addView(urlE);}
  LinearLayout c2=card(l);
  c2.addView(tv("2. App ka logo",13,false));
  LinearLayout row=new LinearLayout(this);row.setGravity(android.view.Gravity.CENTER_VERTICAL);
  ImageView iv=new ImageView(this);iv.setLayoutParams(new LinearLayout.LayoutParams(dp(72),dp(72)));
  if(logo!=null)iv.setImageBitmap(logo);else iv.setBackgroundColor(0xFFE3E6EE);row.addView(iv);
  Button lg=btn("🖼️ Gallery se logo",GRY);lg.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1f));lg.setOnClickListener(v->{keep();pickFile(2,"image/*");});row.addView(lg);
  c2.addView(row);
  LinearLayout c3=card(l);
  LinearLayout h=new LinearLayout(this);h.addView(tv("3. Permissions",13,false),new LinearLayout.LayoutParams(0,-2,1f));
  Button all=new Button(this);all.setText("Sab tick");all.setAllCaps(false);all.setOnClickListener(v->{keep();Arrays.fill(on,true);form();});h.addView(all);c3.addView(h);
  for(int i=0;i<P.length;i++){final int k=i;c3.addView(cb(P[i][1],on[i],(v,ch)->on[k]=ch));}
  LinearLayout c4=card(l);
  c4.addView(tv("4. Options",13,false));
  c4.addView(cb("Fullscreen (status bar chhupao)",fs,(v,ch)->fs=ch));
  c4.addView(cb("Screen ON rakho",awake,(v,ch)->awake=ch));
  c4.addView(cb("Sirf Portrait",portrait,(v,ch)->portrait=ch));
  Button go=btn("🚀 APK Banao",GRN);go.setOnClickListener(v->{keep();build();});l.addView(go);
  show(l);
 }
 void keep(){if(nameE!=null)nameV=nameE.getText().toString();if(urlE!=null)urlV=urlE.getText().toString();}
 void pickFile(int req,String type){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,req);}
 @Override protected void onActivityResult(int rq,int rs,Intent d){
  super.onActivityResult(rq,rs,d);if(rs!=RESULT_OK||d==null||d.getData()==null)return;
  Uri u=d.getData();
  try{
   if(rq==1){src=u;String p=u.getLastPathSegment();srcName=p==null?"file":p.substring(p.lastIndexOf('/')+1);}
   else{Bitmap bm=BitmapFactory.decodeStream(getContentResolver().openInputStream(u));logo=Bitmap.createScaledBitmap(bm,192,192,true);}
  }catch(Exception e){Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();}
  form();
 }
 void build(){
  final String nm=nameV.trim().isEmpty()?"My App":nameV.trim();
  if(mode<3&&src==null){Toast.makeText(this,"Pehle file choose karein",Toast.LENGTH_SHORT).show();return;}
  if(mode==3&&!urlV.trim().startsWith("http")){Toast.makeText(this,"Sahi link likhein (https://...)",Toast.LENGTH_SHORT).show();return;}
  LinearLayout l=page();l.addView(tv("⏳ APK ban rahi hai...",22,true));l.addView(tv("Thora intezar karein",14,false));show(l);
  final List<String> perms=new ArrayList<>();for(int i=0;i<P.length;i++)if(on[i])perms.add(P[i][0]);
  new Thread(()->{
   try{
    File f=ApkBuilder.build(this,nm,mode,src,urlV.trim(),logo,perms,fs,awake,portrait);
    String fn=nm.replaceAll("[^A-Za-z0-9]","");if(fn.isEmpty())fn="app";
    Uri u=saveToDownloads(f,fn+".apk");
    runOnUiThread(()->done(u,nm));
   }catch(Throwable t){runOnUiThread(()->fail(t));}
  }).start();
 }
 Uri saveToDownloads(File f,String fn) throws IOException{
  ContentValues cv=new ContentValues();cv.put(MediaStore.Downloads.DISPLAY_NAME,fn);
  cv.put(MediaStore.Downloads.MIME_TYPE,"application/vnd.android.package-archive");
  cv.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
  Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv);
  try(InputStream in=new FileInputStream(f);OutputStream o=getContentResolver().openOutputStream(u)){byte[] b=new byte[16384];int n;while((n=in.read(b))>0)o.write(b,0,n);}
  return u;
 }
 void done(Uri u,String name){
  savedUri=u;LinearLayout l=page();
  l.addView(tv("✅ "+name+" tayyar hai!",24,true));l.addView(tv("APK Downloads folder mein save ho gayi.",14,false));
  Button i=btn("📲 Install karein",IND);i.setOnClickListener(v->install());l.addView(i);
  Button s=btn("📤 Share karein (WhatsApp etc.)",GRY);s.setOnClickListener(v->share());l.addView(s);
  Button n=btn("➕ Nayi app banayein",0xFF98A2B3);n.setOnClickListener(v->{src=null;srcName="";logo=null;home();});l.addView(n);
  show(l);
 }
 void fail(Throwable t){
  LinearLayout l=page();l.addView(tv("❌ Error",24,true));l.addView(tv(String.valueOf(t),14,false));
  Button n=btn("← Wapas",0xFF98A2B3);n.setOnClickListener(v->form());l.addView(n);show(l);
 }
 void install(){
  if(Build.VERSION.SDK_INT>=26&&!getPackageManager().canRequestPackageInstalls()){
   Toast.makeText(this,"Is app ko install ki ijazat dein, phir wapas aa kar dobara dabayein",Toast.LENGTH_LONG).show();
   startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())));return;}
  startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(savedUri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
 }
 void share(){
  Intent i=new Intent(Intent.ACTION_SEND).setType("application/vnd.android.package-archive").putExtra(Intent.EXTRA_STREAM,savedUri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  startActivity(Intent.createChooser(i,"APK share karein"));
 }
}
