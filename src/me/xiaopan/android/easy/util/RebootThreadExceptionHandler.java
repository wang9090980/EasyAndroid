package me.xiaopan.android.easy.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;

import me.xiaopan.android.easy.receiver.StartApplicationBrocastReceiver;
import me.xiaopan.java.easy.util.StringUtils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

/**
 * 重启线程异常处理器，当发生未知异常时会提示异常信息并在一秒钟后重新启动应用
 * <br>使用此功能的第一步需要你在AndroidMainfest.xml中注册me.xiaopan.easy.android.receiver.StartApplicationBrocastReceiver广播（注意不要任何的filter）
 * <br>第二步就是在你的Application的onCreate()方法中加上new RebootThreadExceptionHandler(getBaseContext());即可
 */
public class RebootThreadExceptionHandler implements UncaughtExceptionHandler{
	private Context context;
	private String hintText;
	
	public RebootThreadExceptionHandler(Context context, String hintText){
		this.context = context;
		this.hintText = hintText;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	public RebootThreadExceptionHandler(Context context){
		this(context, null);
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		ex.printStackTrace();//输出异常信息到控制台
		
		if(StringUtils.isNotEmpty(hintText)){
			/* 启动新线程提示程序异常 */
			new Thread(new Runnable() {	
				@Override
				public void run() {
					Looper.prepare();
					Toast.makeText(context, hintText, Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
			}).start();
			
			/* 主线程等待1秒钟，让提示信息显示出来 */
			try {	
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* 设置定时器，在1秒钟后发出启动程序的广播 */
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.SECOND, 1);
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, new Intent(context, StartApplicationBrocastReceiver.class), 0));
		
		Process.killProcess(Process.myPid());	//结束程序
	}
}