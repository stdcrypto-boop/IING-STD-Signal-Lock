package com.iingstd.signallock;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

final class Updater {
 static final String BASE="https://github.com/stdcrypto-boop/IING-STD-Signal-Lock/releases/download/";
 static void check(Activity a){
 new Thread(()->{try{
 HttpURLConnection c=(HttpURLConnection)new URL("https://api.github.com/repos/stdcrypto-boop/IING-STD-Signal-Lock/releases/latest").openConnection();
 c.setConnectTimeout(15000);c.setReadTimeout(15000);
 if(c.getResponseCode()==404){notice(a,"Belum ada rilis publik");return;}
 JSONObject release;try(InputStream in=c.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>2097152)throw new IOException("Metadata terlalu besar");}release=new JSONObject(out.toString("UTF-8"));}finally{c.disconnect();}
 String tag=release.getString("tag_name");if(!tag.matches("v[0-9]+"))throw new IOException("Format versi tidak didukung");
 long version=Long.parseLong(tag.substring(1));if(version<=a.getPackageManager().getPackageInfo(a.getPackageName(),0).getLongVersionCode()){notice(a,"Aplikasi sudah terbaru");return;}
 JSONArray assets=release.getJSONArray("assets");String url=null;for(int i=0;i<assets.length();i++){JSONObject asset=assets.getJSONObject(i);if(asset.getString("name").equals("IING-STD-Signal-Lock.apk"))url=asset.getString("browser_download_url");}
 if(url==null||!url.equals(BASE+tag+"/IING-STD-Signal-Lock.apk"))throw new IOException("APK rilis tidak tersedia");
 final String link=url;a.runOnUiThread(()->new AlertDialog.Builder(a).setTitle("Update "+tag).setMessage(release.optString("body")).setPositiveButton("Unduh & instal",(d,w)->download(a,link,version)).setNegativeButton("Nanti",null).show());
 }catch(Exception e){notice(a,"Gagal cek update: "+e.getMessage());}}).start();}
 static void notice(Activity a,String s){a.runOnUiThread(()->Toast.makeText(a,s,Toast.LENGTH_LONG).show());}
 static void download(Activity a,String link,long version){
 if(!a.getPackageManager().canRequestPackageInstalls()){a.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,android.net.Uri.parse("package:"+a.getPackageName())));notice(a,"Izinkan instalasi lalu ulangi cek update");return;}
 ProgressDialog p=new ProgressDialog(a);p.setMessage("Mengunduh APK…");p.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);p.setMax(100);p.setCancelable(false);p.show();
 new Thread(()->{File file=null;int sessionId=-1;PackageInstaller installer=a.getPackageManager().getPackageInstaller();try{
 file=File.createTempFile("update",".apk",a.getCacheDir());HttpURLConnection c=(HttpURLConnection)new URL(link).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);long total=c.getContentLengthLong(),done=0;try(InputStream in=c.getInputStream();OutputStream out=new FileOutputStream(file)){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1){done+=n;if(done>150L*1024*1024)throw new IOException("APK terlalu besar");out.write(b,0,n);if(total>0){int percent=(int)(done*100/total);a.runOnUiThread(()->p.setProgress(percent));}}}finally{c.disconnect();}
 PackageManager pm=a.getPackageManager();PackageInfo candidate=pm.getPackageArchiveInfo(file.getAbsolutePath(),PackageManager.GET_SIGNING_CERTIFICATES),current=pm.getPackageInfo(a.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);
 if(candidate==null||!a.getPackageName().equals(candidate.packageName)||candidate.getLongVersionCode()!=version||version<=current.getLongVersionCode()||candidate.signingInfo==null||current.signingInfo==null||!new HashSet<>(Arrays.asList(candidate.signingInfo.getApkContentsSigners())).equals(new HashSet<>(Arrays.asList(current.signingInfo.getApkContentsSigners()))))throw new SecurityException("Identitas, versi atau tanda tangan APK tidak cocok");
 PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);params.setAppPackageName(a.getPackageName());sessionId=installer.createSession(params);
 try(PackageInstaller.Session session=installer.openSession(sessionId);InputStream in=new FileInputStream(file);OutputStream out=session.openWrite("base.apk",0,file.length())){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);session.fsync(out);Intent callback=new Intent(a,InstallReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(a,sessionId,callback,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);session.commit(pi.getIntentSender());}sessionId=-1;
 }catch(Exception e){if(sessionId>=0)try{installer.abandonSession(sessionId);}catch(Exception ignored){}notice(a,"Update gagal: "+e.getMessage());}finally{if(file!=null)file.delete();a.runOnUiThread(()->p.dismiss());}}).start();}
}
