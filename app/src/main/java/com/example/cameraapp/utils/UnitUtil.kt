package com.example.shiningapp.util

import android.content.Context

/**
 * @Author Martlet
 * @Date 2023/7/9 9:54
 * @Description
 */
class UnitUtil {
    companion object{
        fun dipToPx(context: Context,dipValue:Float):Int{
            val density = context.resources.displayMetrics.density
            return (density*dipValue+0.5f).toInt()
        }
    }
}