package com.example.shiningapp.util

import android.content.Context
import android.widget.Toast

/**
 * @Author Martlet
 * @Date 2023/7/3 19:57
 * @Description
 */
class ToastUtil {

    companion object{
        fun toastShow(context: Context,text:String){
            Toast.makeText(context,text,Toast.LENGTH_SHORT).show()
        }
    }
}