package com.example.cameraapp.module2

import android.Manifest
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cameraapp.R
import com.example.cameraapp.databinding.ActivitySecondBinding
import com.example.cameraapp.utils.getMediaPathOnKitKat
import com.example.shiningapp.util.ToastUtil
import java.io.File

/**
 * 和MainActivity相比不需要配置FileProvider
 */
class SecondActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySecondBinding
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val MIME_IMAGE = "image/*"
    //todo 这是重点
    //真的不想要file了，就这样吧
    private val imageSaveUri by lazy {
        val mimeType = "image/jpeg"
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        //其实这个路径存储在哪里也是很有说法的
        val image = File(externalCacheDir, "/$fileName")
        val values = ContentValues()
        //安卓10及以上的value，尝试ing
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,image.absolutePath)
            values.put(MediaStore.MediaColumns.MIME_TYPE,mimeType)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }else{
            values.put(MediaStore.Images.Media.DATA,image.absolutePath)
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
    }
    /**
     * 通过拍照并保存，在api 28华为真机上看不到图片，也看不到视频
     * todo 解决了。原因是未设置完整的Uri
     */
    private val launcherTakePicture = ActivityResultContracts.TakePicture().let { contract->
        registerForActivityResult(contract){
            //根据执行结果，操作image，把这个image放在外面，以便这里可以获取到，操作image
            if(it){
                val path = getMediaPathOnKitKat(this@SecondActivity,imageSaveUri);
                binding.ivDisplay.setImageBitmap(BitmapFactory.decodeFile(path))
            }
        }
    }
    /**
     * 通过Intent.ACTION_GET_CONTENT获取一个文件
     * 这个方法可以通过`android.content.ContentResolver.openInputStream`获取到文件的原始数据
     */
    private val launcherGetContent = ActivityResultContracts.GetContent().let { contract->
        registerForActivityResult(contract){
            if(it!=null){
//                val inputStream = contentResolver.openInputStream(it)
//                val text = inputStream?.bufferedReader().use {br->
//                    br?.readText() ?: ""
//                }
                //草率了，如果是下载目录就不知道数据类型了
//                initTVUri(text)
                //读取图片
                val path = getMediaPathOnKitKat(this@SecondActivity,it);
                binding.ivDisplay.setImageBitmap(BitmapFactory.decodeFile(path))

            }
        }
    }

    private val requestCameraPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty()->{
                ToastUtil.toastShow(this@SecondActivity,"获取拍照权限失败")
            }
            else->{
                launcherTakePicture.launch(imageSaveUri)
            }
        }
    }
    private val requestPhotoPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty()->{
                ToastUtil.toastShow(this@SecondActivity,"访问图片权限失败")
            }
            else->{
                launcherGetContent.launch(MIME_IMAGE)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_second)

        binding.btnSelectPhoto.setOnClickListener {
            requestPhotoPermissionsLauncher.launch(PERMISSIONS)
        }
        binding.btnTakePhoto.setOnClickListener {
            requestCameraPermissionsLauncher.launch(PERMISSIONS)
        }
    }
}