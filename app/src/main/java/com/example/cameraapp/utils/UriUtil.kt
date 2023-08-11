package com.example.cameraapp.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.cameraapp.utils.FileUtil.Companion.inToOut
import java.io.File
import java.io.FileOutputStream

/**
 * @Author Martlet
 * @Date 2023/8/11 10:26
 * @Description
 */
/**
 * uri转file
 */
fun uriToFile(uri:Uri?,context: Context): File?{
    var file: File?=null
    if(uri==null) return file
    if(uri.scheme== ContentResolver.SCHEME_FILE){
        file = File(uri.path)
    }else if(uri.scheme== ContentResolver.SCHEME_CONTENT){
        //把文件复制到沙盒目录
        val resolver = context.contentResolver
        val displayName = "${System.currentTimeMillis()}${Math.round((Math.random()+1)*1000)}." +
                "${MimeTypeMap.getSingleton().getExtensionFromMimeType(resolver.getType(uri))}"
        val inputStream = resolver.openInputStream(uri)
        val cache = File(context.cacheDir.absolutePath,displayName)
        val outputStream = FileOutputStream(cache)
        if(inToOut(inputStream,outputStream)){
            file = cache
        }
    }
    return file
}
/**
 * 从Uri中获取文件路径，参数为Context,Uri 返回String文件路径
 * 第一版：有很多问题，第二版很快就来
 * 第二版：写好了，但是很多的测试场景暂时没精力实现
 */
fun getMediaPathOnKitKat(context: Context?, uri: Uri?):String{
    var imgPath = ""
    if(context==null||uri==null){
        return imgPath
    }
    LogUtil.d("uri=$uri")
    LogUtil.d("uri.authority=${uri.authority}")
    if(DocumentsContract.isDocumentUri(context,uri)){
        val docId = DocumentsContract.getDocumentId(uri)
        when(uri.authority){
            "com.android.providers.media.documents"->{
                val array = docId.split(":")
                val type = array[0]
                val id = array[1]
                val contentUri = when(type){
                    "image"-> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video"-> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                        "audio"->MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "${MediaStore.MediaColumns._ID}=$id"
                imgPath = getImagePath(contentUri,selection,context)
            }
            "com.android.providers.downloads.documents"->{
                try {
                    //不同的uri获得的id也是不同的，可以转换为long
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),docId.toLong())
                    imgPath = getImagePath(contentUri,null,context)
                }catch (t:Throwable){
                    //这应该算是偷懒，不检测id合法性，直接交给异常处理
                    t.printStackTrace()
                    //todo 这种情况可以阻止程序奔溃吗？
                    return imgPath
                }
            }
            //todo 不知道这种写法对不对
            "com.android.externalstorage.documents"->{
                val array = docId.split(":")
                val type = array[0]
                val id = array[1]//是id吗？只是复制了上面的代码
                if("primary".equals(type,true)){
                    imgPath = Environment.getExternalStorageDirectory().absolutePath+"/"+id
                }
            }
        }

    }else{
        if("content".equals(uri.scheme,true)){
            if("com.google.android.apps.photos.content"==uri.authority){
                imgPath = uri.lastPathSegment.toString()
            }else{
                imgPath = getImagePath(uri,null,context)
            }
        }
        //这种模式暂时别想了，这种URI  file无法保存到
        if("file".equals(uri.scheme,true)){
            imgPath = uri.path.toString()
        }
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