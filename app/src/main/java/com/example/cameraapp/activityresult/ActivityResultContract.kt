package com.example.cameraapp.activityresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.example.cameraapp.utils.d
import java.io.File

/**
 * @Author Martlet
 * @Date 2023/8/9 9:49
 * @Description
 * todo 记得把工具类往发源地回馈
 */
/**
 * 选择照片的协定
 */
class SelectPhotoContract:ActivityResultContract<Unit?, Uri?>(){
    companion object{
        private const val TAG = "Zhen_SelectPhotoContract"

    }

    /**
     * Intent.ACTION_PICK之前有去获取通讯录联系人的
     * 这次去获取图片，是第一次
     * 之前获取图片有Intent.ACTION_GET_CONTENT,Intent.ACTION_OPEN_DOCUMENT都
     * 可以在一些图片中做选择，选择一个或多个
     */
    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(Intent.ACTION_PICK).setType("image/*")
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        "Select photo uri:${intent?.data}".d(TAG)
        return intent?.data
    }
}

/**
 * 拍照协定
 */
class TakePhotoContract:ActivityResultContract<Unit?,Uri?>(){
    companion object{
        private const val TAG = "Zhen_TakePhotoContract"
    }
    private var uri:Uri? = null
    override fun createIntent(context: Context, input: Unit?): Intent {
        val mimeType = "image/jpeg"
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        uri = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            //安卓10及以上获取图片Uri
            val value = contentValuesOf(
                Pair(MediaStore.MediaColumns.DISPLAY_NAME,fileName),
                Pair(MediaStore.MediaColumns.MIME_TYPE,mimeType),
                Pair(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM)
            )
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,value)
        }else{
            //不对不对，好奇怪啊，之前的拍照只是安卓10无效，其他的都没问题，
            //我不能把这里安卓10以下的改成之前的版本吗？
            //安卓9及以下获取Uri
            val file = File(context.externalCacheDir, "/$fileName")
            intent.putExtra("absolutePath",file.absolutePath)
            FileProvider.getUriForFile(
                context,"${context.packageName}.provider",
                file
            )
        }
        return intent.putExtra(MediaStore.EXTRA_OUTPUT,uri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        "Take photo,resultCode:$resultCode,uri:$uri".d(TAG)
        if (resultCode== Activity.RESULT_OK) return uri
        return null
    }
}
/**
 * 裁剪照片的协定
 */
class CropPhotoContract:ActivityResultContract<Uri,CropPhotoContract.CropOutput?>(){
    data class CropOutput(val uri:Uri,val fileName:String) {
        override fun toString(): String {
            return "{uri:$uri,fileName:$fileName}"
        }
    }
    companion object{
        private const val TAG = "Zhen_CropPhotoContract"
    }
    private var output:CropOutput? = null

    override fun createIntent(context: Context, input: Uri): Intent {
        //获取输入图片uri的媒体类型
        val mimeType = context.contentResolver.getType(input)
        mimeType?.d("Zhen")
        //创建新的图片名称
        val fileName = "IMG_${System.currentTimeMillis()}.${
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }"
        fileName.d("Zhen")
        val outputUri = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            //安卓10及以上获取图片URI
            val value = contentValuesOf(
                Pair(MediaStore.MediaColumns.DISPLAY_NAME,fileName),
                Pair(MediaStore.MediaColumns.MIME_TYPE,mimeType),
                Pair(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM)
            )
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,value)
        }else{
            //估计是file打头的uri，去看看效果，是的就是file开头
            Uri.fromFile(File(context.externalCacheDir!!.absolutePath,fileName))
        }
        outputUri.toString().d("Zhen")
        output = CropOutput(outputUri!!,fileName)
        return Intent("com.android.camera.action.CROP")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setDataAndType(input,"image/*")
            .putExtra("outputX",300)
            .putExtra("outputY",300)
            .putExtra("aspectX",1)
            .putExtra("aspectY",1)
            .putExtra("scale",true)
            .putExtra("crop",true)
            .putExtra("return-data",false)// 在小米手机部分机型中 如果直接返回Data给Intent，图片过大的时候会有问题
            .putExtra("noFaceDetection",true)
            .putExtra(MediaStore.EXTRA_OUTPUT,outputUri)
            .putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString())

    }

    override fun parseResult(resultCode: Int, intent: Intent?): CropOutput? {
        "Crop photo, resultCode: $resultCode output: $output".d(TAG)
        if (resultCode==Activity.RESULT_OK) return output
        return null
    }
}
/**
 * 不管了，自定义一个权限请求
 * 2023-8-9 自定义权限并实现全局权限请求大失败
 * 2023-8-10 不得不去参考其他的权限请求方法。
 * 这里就注释掉吧
 */
class RequestPermissionContract(private val result:Result):ActivityResultContract<Array<String>,Unit>(){
    interface Result {
        fun success()
        fun failure()
    }
    companion object{
        private const val TAG = "Zhen_RequestPermissions"
        const val ACTION_REQUEST_PERMISSIONS =
            "androidx.activity.result.contract.action.REQUEST_PERMISSIONS"
        const val EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS"
        const val EXTRA_PERMISSION_GRANT_RESULTS =
            "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS"

        internal fun createIntent(input: Array<String>): Intent {
            return Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS).putExtra(
                ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS, input)
        }
    }

    override fun createIntent(context: Context, input: Array<String>): Intent {
        return Companion.createIntent(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        if(resultCode==Activity.RESULT_OK&&intent!=null){
            //获得Intent返回的权限赋予情况
            val grantResults =
                intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
            var success = true
            grantResults?.map {
                if(it!=PackageManager.PERMISSION_GRANTED){
                    success = false
                }
            }
            //根据情况执行Result的方法
            if(success){
                result.success()
            }else{
                result.failure()
            }
        }else{
            result.failure()
        }
    }

}
//lateinit var result:RequestPermissionContract.Result
//lateinit var activity:ComponentActivity
//val requestPermissionsLauncher = activity.registerForActivityResult(RequestPermissionContract(result)){}
//fun testRequestPermissions(activity1: ComponentActivity,array:Array<String>,result1: RequestPermissionContract.Result){
//    activity = activity1
//    result = result1
//    requestPermissionsLauncher.launch(array)
//}