package demoaudio.yixiao.com.audiorecordandaudiotrack;

public class VersionUtils {
	        
	public static int getSDKVersionNumber() {  
	    int sdkVersion;  
	    try {  
	        sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
	    } catch (NumberFormatException e) {
	        sdkVersion = 0;  
	    }  
	    LogUtil.i("TAG", "sdkVersion:"+sdkVersion);
	    return sdkVersion;  
	}  
	/**
	 * 检测当前系统版本是否大于等于checkVersion版本
	 * @param checkVersion 对比检测版本
	 * @return
	 */
	public static boolean checkSDKVersion(int checkVersion){
		return getSDKVersionNumber() >= checkVersion ? true : false; 
	}
}
