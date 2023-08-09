package com.example.cameraapp.utils
import android.util.Log

/**
 * @Author Martlet
 * @Date 2023/7/4 8:48
 * @Description
 */
class LogUtil {
    companion object{
        private const val TAG = "zhen"
        fun d(text:String){
            Log.d(TAG,text)
        }
    }
}
fun String.d(tag:String){
    Log.d(tag,this)
}