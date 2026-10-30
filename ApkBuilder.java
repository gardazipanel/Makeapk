package com.apkmaker.maker;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import com.android.apksig.ApkSigner;
import org.json.JSONObject;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.*;

public class ApkBuilder {
 static final String PKG_PH="com.apkmaker.g00000000", NAME_PH="APPNAME_PLACEHOLDER_0123456789";

 static byte[] readAll(InputStream in) throws IOException{
  ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[16384];int n;
  while((n=in.read(b))>0)o.write(b,0,n);return o.toByteArray();}
 static void put(ZipOutputStream z,String n,byte[] d) throws IOException{
  z.putNextEntry(new ZipEntry(n));z.write(d);z.closeEntry();}
 static int idx(byte[] d,byte[] p){
  outer:for(int i=0;i<=d.length-p.length;i++){for(int j=0;j<p.length;j++)if(d[i+j]!=p[j])continue outer;return i;}return -1;}
 // Manifest ke string pool mein same-length replace (UTF-16 ya ASCII)
 static void rep(byte[] d,String from,String to) throws Exception{
  String[] cs={"UTF-16LE","US-ASCII"};
  for(String cn:cs){
   String t=cn.equals("US-ASCII")?to.replaceAll("[^\\x20-\\x7E]","?"):to;
   byte[] a=from.getBytes(cn),b=t.getBytes(cn);
   if(a.length!=b.length)continue;
   boolean any=false;int i;
   while((i=idx(d,a))>=0){System.arraycopy(b,0,d,i,b.length);any=true;}
   if(any)return;
  }
 }
 static byte[] png(Bitmap b) throws IOException{ByteArrayOutputStream o=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,o);return o.toByteArray();}

 public static File build(Context c,String name,int mode,Uri src,String url,Bitmap logo,List<String> perms,boolean fs,boolean awake,boolean portrait) throws Exception{
  Map<String,byte[]> www=new LinkedHashMap<>();
  if(mode==1){
   try(InputStream in=c.getContentResolver().openInputStream(src)){www.put("index.html",readAll(in));}
  }else if(mode==2){
   Map<String,byte[]> all=new LinkedHashMap<>();
   try(ZipInputStream zi=new ZipInputStream(c.getContentResolver().openInputStream(src))){
    ZipEntry e;while((e=zi.getNextEntry())!=null){if(!e.isDirectory()&&!e.getName().startsWith("__MACOSX"))all.put(e.getName(),readAll(zi));}}
   String pre="";
   if(!all.containsKey("index.html")){String best=null;
    for(String k:all.keySet())if(k.endsWith("/index.html")&&(best==null||k.length()<best.length()))best=k;
    if(best==null)throw new Exception("ZIP mein index.html nahi mili");
    pre=best.substring(0,best.length()-10);}
   for(Map.Entry<String,byte[]> en:all.entrySet())if(en.getKey().startsWith(pre))www.put(en.getKey().substring(pre.length()),en.getValue());
  }
  String cfg=new JSONObject().put("url",mode==3?url:"").put("perms",android.text.TextUtils.join(",",perms)).put("fullscreen",fs).put("awake",awake).put("orient",portrait?"portrait":"auto").toString();

  StringBuilder h=new StringBuilder();Random r=new SecureRandom();for(int i=0;i<8;i++)h.append(Integer.toHexString(r.nextInt(16)));
  String pkg="com.apkmaker.g"+h;
  StringBuilder nm=new StringBuilder(name.length()>30?name.substring(0,30):name);while(nm.length()<30)nm.append(' ');
  File tmp=new File(c.getCacheDir(),"unsigned.apk"),out=new File(c.getCacheDir(),"signed.apk");
  try(ZipOutputStream zo=new ZipOutputStream(new FileOutputStream(tmp));ZipInputStream zi=new ZipInputStream(c.getAssets().open("template.apk"))){
   ZipEntry e;
   while((e=zi.getNextEntry())!=null){
    String n=e.getName();if(e.isDirectory())continue;
    if(n.startsWith("META-INF/")&&(n.endsWith(".SF")||n.endsWith(".RSA")||n.endsWith(".DSA")||n.endsWith(".EC")||n.endsWith("MANIFEST.MF")))continue;
    if(n.startsWith("assets/www/")||n.equals("assets/config.json"))continue;
    byte[] d=readAll(zi);
    if(n.equals("AndroidManifest.xml")){rep(d,PKG_PH,pkg);rep(d,NAME_PH,nm.toString());}
    else if(logo!=null&&n.startsWith("res/mipmap")&&n.endsWith("ic_launcher.png"))d=png(logo);
    put(zo,n,d);
   }
   put(zo,"assets/config.json",cfg.getBytes("UTF-8"));
   for(Map.Entry<String,byte[]> en:www.entrySet())put(zo,"assets/www/"+en.getKey(),en.getValue());
  }
  char[] pw="apkmaker".toCharArray();
  KeyStore ks=KeyStore.getInstance("PKCS12");ks.load(c.getAssets().open("sign.p12"),pw);
  PrivateKey k=(PrivateKey)ks.getKey("key",pw);X509Certificate cert=(X509Certificate)ks.getCertificate("key");
  ApkSigner.SignerConfig sc=new ApkSigner.SignerConfig.Builder("key",k,Collections.singletonList(cert)).build();
  if(out.exists())out.delete();
  new ApkSigner.Builder(Collections.singletonList(sc)).setInputApk(tmp).setOutputApk(out)
   .setMinSdkVersion(24).setV1SigningEnabled(true).setV2SigningEnabled(true).setV3SigningEnabled(false).build().sign();
  return out;
 }
}
