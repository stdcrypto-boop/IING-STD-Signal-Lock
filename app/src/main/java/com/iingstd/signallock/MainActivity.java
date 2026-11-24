package com.iingstd.signallock;
import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.telephony.*;
import android.graphics.Color;
import android.widget.*;
import android.view.*;
import java.util.*;
public class MainActivity extends Activity {
 final Handler h=new Handler(Looper.getMainLooper());
 TextView stats; LinearLayout root; Spinner sim; int sub=-1;
 final int green=0xff36e8bd;
 final Runnable tick=new Runnable(){public void run(){refresh();h.postDelayed(this,3000);}};
 public void onCreate(Bundle b){super.onCreate(b);
 ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,32,24,32);root.setBackgroundColor(0xff101820);scroll.addView(root);setContentView(scroll);
 label("IING STD SIGNAL LOCK",26);label("NON-ROOT • LIVE NETWORK",14);
 sim=new Spinner(this);root.addView(sim);stats=label("Memuat jaringan…",18);
 button("Izinkan akses sinyal",()->requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1));
 button("Band Lock • MediaTek",()->openEngineering());
 button("Network Mode • 4G / 5G / Auto",()->openNetwork());
 button("Cek pembaruan",()->checkUpdate());
 label("Band Lock diterapkan melalui menu modem bawaan. Aplikasi tidak mengklaim lock berhasil tanpa verifikasi. Simpan konfigurasi awal sebelum mengubah band.",14);
 loadSims();}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(green);t.setPadding(8,16,8,16);root.addView(t);return t;}
 void button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setTextColor(green);root.addView(b);b.setOnClickListener(v->action.run());}
 void loadSims(){ArrayList<String> names=new ArrayList<>();ArrayList<Integer> ids=new ArrayList<>();names.add("SIM data utama");ids.add(-1);try{for(SubscriptionInfo i:getSystemService(SubscriptionManager.class).getActiveSubscriptionInfoList()){names.add("SIM "+(i.getSimSlotIndex()+1)+" • "+i.getCarrierName());ids.add(i.getSubscriptionId());}}catch(Exception ignored){}sim.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));sim.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){sub=ids.get(pos);refresh();}});}
 public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);loadSims();refresh();}
 String val(int v){return v==Integer.MAX_VALUE?"—":String.valueOf(v);}
 void refresh(){try{TelephonyManager t=getSystemService(TelephonyManager.class);if(sub>=0)t=t.createForSubscriptionId(sub);StringBuilder s=new StringBuilder(t.getNetworkOperatorName()+"\n");SignalStrength ss=t.getSignalStrength();if(ss!=null)s.append("Signal level: "+ss.getLevel()+"/4\n");List<CellInfo> cells=t.getAllCellInfo();if(cells!=null)for(CellInfo c:cells){if(!c.isRegistered())continue;if(c instanceof CellInfoLte){CellInfoLte l=(CellInfoLte)c;CellSignalStrengthLte a=l.getCellSignalStrength();CellIdentityLte i=l.getCellIdentity();s.append("4G LTE\nRSRP "+val(a.getRsrp())+" dBm\nRSRQ "+val(a.getRsrq())+" dB\nSINR "+(a.getRssnr()==Integer.MAX_VALUE?"—":a.getRssnr()/10.0)+" dB\nPCI "+val(i.getPci())+"\nEARFCN "+val(i.getEarfcn())+"\nCell ID "+val(i.getCi())+"\n");if(Build.VERSION.SDK_INT>=30)s.append("Band "+Arrays.toString(i.getBands())+"\n");}else if(c instanceof CellInfoNr){CellInfoNr n=(CellInfoNr)c;CellSignalStrengthNr a=(CellSignalStrengthNr)n.getCellSignalStrength();CellIdentityNr i=(CellIdentityNr)n.getCellIdentity();s.append("5G NR\nRSRP "+val(a.getSsRsrp())+" dBm\nRSRQ "+val(a.getSsRsrq())+" dB\nSINR "+val(a.getSsSinr())+" dB\nPCI "+val(i.getPci())+"\nNRARFCN "+val(i.getNrarfcn())+"\n");if(Build.VERSION.SDK_INT>=30)s.append("Band "+Arrays.toString(i.getBands())+"\n");}}s.append("\nData modem; dapat tertunda atau tidak tersedia.");stats.setText(s);}catch(Exception e){stats.setText("Data belum tersedia. Izinkan telepon dan lokasi presisi, aktifkan lokasi.");}}
 void openEngineering(){try{Intent i=getPackageManager().getLaunchIntentForPackage("com.mediatek.engineermode");if(i!=null){startActivity(i);return;}Intent j=new Intent();j.setClassName("com.mediatek.engineermode","com.mediatek.engineermode.EngineerMode");startActivity(j);}catch(Exception e){new AlertDialog.Builder(this).setTitle("Engineering Mode").setMessage("Akses langsung dibatasi. Buka dialer dan ketik *#*#3646633#*#*, lalu BandMode.").setPositiveButton("Salin kode",(d,w)->((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("kode","*#*#3646633#*#*"))).setNegativeButton("Tutup",null).show();}}
 void openNetwork(){try{Intent i=new Intent();i.setClassName("com.android.settings","com.android.settings.RadioInfo");startActivity(i);}catch(Exception e){startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));}}
 void checkUpdate(){new Thread(()->{try{java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL("https://api.github.com/repos/stdcrypto-boop/IING-STD-Signal-Lock/releases/latest").openConnection();c.setConnectTimeout(10000);c.setReadTimeout(10000);if(c.getResponseCode()==404){runOnUiThread(()->Toast.makeText(this,"Belum ada rilis publik",Toast.LENGTH_LONG).show());return;}String json;try(java.io.InputStream in=c.getInputStream()){json=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}org.json.JSONObject o=new org.json.JSONObject(json);String tag=o.optString("tag_name");runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Rilis GitHub: "+tag).setMessage(o.optString("body")).setPositiveButton("Buka rilis",(d,w)->startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/stdcrypto-boop/IING-STD-Signal-Lock/releases/latest")))).setNegativeButton("Tutup",null).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Gagal memeriksa update",Toast.LENGTH_LONG).show());}}).start();}
 protected void onResume(){super.onResume();h.removeCallbacks(tick);h.post(tick);}protected void onPause(){h.removeCallbacks(tick);super.onPause();}
}