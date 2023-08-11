package com.example.cameraapp.utils

import android.text.TextUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @Author Martlet
 * @Date 2023/7/9 13:35
 * @Description
 */
class FileUtil {
    //todo 记得评价
    companion object{
        /**
         * 文件读写
         */
        fun inToOut(`in`:InputStream?,out:OutputStream?):Boolean{
            if(`in`==null||out==null){
                return false
            }
            try {
                var read = `in`.read()
                `in`.use { input->
                    out.use {
                        while (read!=-1){
                            it.write(read)
                            read = input.read()
                        }
                    }
                }
            }catch (e:Throwable){
                e.printStackTrace()
                return false
            }
            return true
        }

        /**
         * 读取文件路径为path的文件的内容，以字符串格式输出
         */
        fun openText(path:String?):String{
            var text = ""
            if(TextUtils.isEmpty(path)){
                return text
            }
            try {
                //这个方法没有换行，并且不简洁
//                BufferedReader(FileReader(path)).use {
//                    var line = it.readLine()
//                    while (line!=null){
//                        sb.append(line)
//                        line = it.readLine()
//                    }
//                }
                //很OK bufferedReader可以设置字符集Charsets.ISO_8859_1
//                sb.append(File(path).bufferedReader().use {
//                    it.readText()
//                })
                //test 这个也不换行
//                val listOfLine = mutableListOf<String>()
//                File(path).bufferedReader().useLines { lines->
//                    lines.forEach {
//                        val x = ">("+it.length+")"+it
//                        listOfLine.add(x)
//                        sb.append(x)
//                    }
//                }
                //很OK bufferedReader可以设置字符集Charsets.ISO_8859_1
                text = File(path).bufferedReader().use {
                    it.readText()
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
            return text
        }
    }
}