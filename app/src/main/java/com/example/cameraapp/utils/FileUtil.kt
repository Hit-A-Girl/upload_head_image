package com.example.cameraapp.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.StringBuilder

/**
 * @Author Martlet
 * @Date 2023/7/9 13:35
 * @Description
 */
class FileUtil {
    //todo 记得评价
    companion object{
        /**
         * 从Uri中获取文件路径，参数为Context,Uri 返回String文件路径
         * 第一版：有很多问题，第二版很快就来
         */
        fun getImgPathOnKitKat(context: Context?,uri: Uri?):String{
            var imgPath = ""
            if(context==null||uri==null){
                return imgPath
            }
            LogUtil.d("uri=$uri")
            LogUtil.d("uri.authority=${uri.authority}")
            if(DocumentsContract.isDocumentUri(context,uri)){
                val docId = DocumentsContract.getDocumentId(uri)
                if("com.android.providers.media.documents" == uri.authority){
                    val id = docId.split(":")[1]
                    val selection = "${MediaStore.Images.Media._ID}=$id"
                    imgPath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection,context)
                }else if("com.android.providers.downloads.documents" == uri.authority){
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),docId.toLong())
                    imgPath = getImagePath(contentUri,null,context)
                }
            }else if("content".equals(uri.scheme,true)){
                imgPath = getImagePath(uri,null,context)
            }
            return imgPath
        }

        @SuppressLint("Range")
        private fun getImagePath(uri: Uri, selection: String?, context: Context): String {
            val cursor = context.contentResolver.query(
                uri,
                null,
                selection,
                null,
                null,
                null
            )
//            val imgPath = cursor?.takeIf {it.moveToFirst()}?.run {
//                getString(getColumnIndex(MediaStore.Images.Media.DATA))
//            }?:""
            var imgPath:String=""
            if(cursor?.moveToFirst() == true){
                imgPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            }
            cursor?.close()
            return imgPath
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