package com.example.cameraapp

import android.Manifest
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.cameraapp.activityresult.CropPhotoContract
import com.example.cameraapp.activityresult.SelectPhotoContract
import com.example.cameraapp.activityresult.TakePhotoContract
import com.example.cameraapp.databinding.ActivityMainBinding
import com.example.cameraapp.utils.FileUtil
import com.example.cameraapp.utils.FileUtilTemp
import com.example.cameraapp.utils.d
import com.example.shiningapp.util.ToastUtil
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityMainBinding
    private lateinit var imageFile:File
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var success = true
    /**
     * 首先获取权限
     * todo 希望可以把这个请求权限方法公有化
     */
    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->

        resultMap.map {result->
            if(!result.value){
                success = false
            }
        }


    }
    /**
     * 打开相机拍照
     */
    private val takePhotoLauncher = registerForActivityResult(TakePhotoContract()){uri->
        //todo 最终目标是既要展示图片，又要可用于文件上传即获得File对象
        //方式1 从uri获取文件路径path 安卓10以下大失败
//        val path = FileUtil.getImgPathOnKitKat(this@MainActivity,uri)
//        val path = FileUtilTemp.getPath(this@MainActivity,uri)
        //todo 先暂停自己的获取file，把原网页剩下的裁剪一并完成再说
        uri?.let {
            //好像没啥dio用啊
            cropPhotoLauncher.launch(it)
        }
    }

    /**
     *前往相册选择照片
     */
    private val selectPhotoLauncher = registerForActivityResult(SelectPhotoContract()){uri->
        uri?.let {
            //好像没啥dio用啊
            cropPhotoLauncher.launch(it)
        }

    }

    /**
     * 前往裁剪
     */
    private val cropPhotoLauncher = registerForActivityResult(CropPhotoContract()){output->
        output?.let {
            "裁剪完成，开始上传头像到服务器，output:$it"
            //上传头像
            showLoading(getString(R.string.base_uploading))
//            mViewModel.fetchUpload(ContentUriRequestBody(contentResolver, it.uri), it.fileName)
        }

    }

    private fun showLoading(str: String) {
        ToastUtil.toastShow(this@MainActivity,"开始转圈")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.btnTakePhoto.setOnClickListener(this)
        binding.btnSelectPhoto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_take_photo->{
                //todo 可以改进，比如传入图片类型作为参数
//                takePhotoLauncher.launch(null)
                //我去安卓6崩了
                requestPermissionsLauncher.launch(PERMISSIONS)
                if(success){
                    takePhotoLauncher.launch(null)
                }else{
                    ToastUtil.toastShow(this@MainActivity,"获取拍照权限失败")
                }
            }
            R.id.btn_select_photo->{
                requestPermissionsLauncher.launch(PERMISSIONS)
                if(success){
                    selectPhotoLauncher.launch(null)
                }else{
                    ToastUtil.toastShow(this@MainActivity,"访问图片权限失败")
                }
            }
        }
    }
}