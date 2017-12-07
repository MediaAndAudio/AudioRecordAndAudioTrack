package demoaudio.yixiao.com.audiorecordandaudiotrack

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Created by xy on 2017/12/7.
 */
class PermissionUtils {
    companion object {
        /**
         * 检查是否拥有指定的所有权限  false 表示没有权限
         */
        fun checkPermissionAllGranted(context: Context, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 只要有一个权限没有被授予, 则直接返回 false
                    return false
                }
            }
            return true
        }

        /**
         * 请求权限
         * @param mActivity 请求界面的上下文对象  需要对结果进行处理
         * @param permissions
         * @param requestCode
         */
        fun RequestPermissionsRequestCodeValidator(mActivity: Activity, permissions: Array<String>, requestCode: Int) {
            // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
            ActivityCompat.requestPermissions(mActivity,
                    permissions,
                    requestCode
            )
        }
    }
}