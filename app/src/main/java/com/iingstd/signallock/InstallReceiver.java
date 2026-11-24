package com.iingstd.signallock;
import android.content.*;
import android.content.pm.PackageInstaller;
import android.widget.Toast;
public class InstallReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){
 int status=i.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
 if(status==PackageInstaller.STATUS_PENDING_USER_ACTION){Intent confirm=i.getParcelableExtra(Intent.EXTRA_INTENT);if(confirm!=null){confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{c.startActivity(confirm);}catch(Exception e){Toast.makeText(c,"Buka aplikasi dan ulangi update",Toast.LENGTH_LONG).show();}}}
 else Toast.makeText(c,status==PackageInstaller.STATUS_SUCCESS?"Update berhasil":"Instalasi tidak selesai: "+i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE),Toast.LENGTH_LONG).show();
 }
}