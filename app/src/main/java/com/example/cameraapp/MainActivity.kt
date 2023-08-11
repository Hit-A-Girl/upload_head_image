package com.example.cameraapp

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cameraapp.activityresult.CropPhotoContract
import com.example.cameraapp.activityresult.SelectPhotoContract
import com.example.cameraapp.activityresult.TakePhotoContract
import com.example.cameraapp.activityresult.permissions
import com.example.cameraapp.databinding.ActivityMainBinding
import com.example.cameraapp.utils.uriToFile
import com.example.shiningapp.util.ToastUtil
import java.io.File

/**
 * 项目来源：https://blog.csdn.net/zhuyb829/article/details/122746281
 * todo FileProvider
 * 历经艰辛，终于实现适配所有安卓版本的拍照和上传头像的方法
 * 第一个版本用的原生的拍照方法，但是在安卓10获取不到拍照的file，现在估计回去看的话会有新的思路
 * 第二版本开始用原生ActivityResult的拍照方法，但是依旧在安卓10获取不到拍照的file
 * 第三版使用了自定义的ActivityResult的拍照方法，可以在全版本获取file，唯一的不足是无法从URI中获取文件路径，
 * 只能把URI转换为沙盒文件file，再从file中获取路径，但是总归还是要占据额外的内存空间
 * todo 一直有个思路：就是把这里安卓10的部分嵌入到之前版本中，
 * 现在万事具备只欠东风，把这个代码上传就开始嵌入，哈哈哈，成了，我把这里URI的安卓10的部分嵌入到第二版，安卓10成了，哈哈哈
 * 哎，我可能还是不够明白原理啊啊啊啊。
 *
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityMainBinding
    private lateinit var imageFile:File
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * 首先获取权限
     * todo 希望可以把这个请求权限方法公有化
     * 不行了，看来第一步就要把这个请求权限公有化，不然，每个按钮都要写一个权限访问
     * 不行了，搞不动，看啦也需要自己定义一个权限请求啊啊啊啊啊
     * 失败了，无论是自己定义权限请求，还是网上的资料，都不行，目前公有化请求权限失败
     * todo 请求权限方法公有化失败，等待有一天可以解决
     * 为什么想要封装权限请求，因为打开相机和打开相册处于同一处，点开任意一个都最好把权限都申请了，不希望重复写申请权限
     * 的ActivityResult api
     */
    private val requestCameraPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty()->{
                ToastUtil.toastShow(this@MainActivity,"获取拍照权限失败")
            }
            else->{
                takePhotoLauncher.launch(null)
            }
        }
    }
    private val requestPhotoPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty()->{
                ToastUtil.toastShow(this@MainActivity,"访问图片权限失败")
            }
            else->{
                selectPhotoLauncher.launch(null)
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
        //FileProvider的URI获取path的方法即艰难又冷门又不一定是成功的，所以统一把uri换成file文件
        //todo 先暂停自己的获取file，把原网页剩下的裁剪一并完成再说 已完成
//        uri?.let {
//            //好像没啥dio用啊
//            cropPhotoLauncher.launch(it)
//        }
        val image = uriToFile(uri,this@MainActivity)
        binding.ivDisplay.setImageBitmap(BitmapFactory.decodeFile(image?.absolutePath))

    }

    /**
     *前往相册选择照片
     */
    private val selectPhotoLauncher = registerForActivityResult(SelectPhotoContract()){uri->

        uri?.let {
            //好像没啥dio用啊
            cropPhotoLauncher.launch(it)
        }
        val image = uriToFile(uri,this@MainActivity)
        binding.ivDisplay.setImageBitmap(BitmapFactory.decodeFile(image?.absolutePath))


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

    /**
     * 根据网上资料使用的全局方法，请求权限失败案例
     */
    private fun requestPhotoPermissions() {
        permissions(PERMISSIONS){
            success{
                selectPhotoLauncher.launch(null)
            }
            failure{
                ToastUtil.toastShow(this@MainActivity,"访问图片权限失败")
            }
        }
    }

    /**
     * 根据网上资料使用的全局方法，请求权限失败案例
     */
    private fun requestCameraPermissions() {
        permissions(PERMISSIONS){
            success{
                takePhotoLauncher.launch(null)
            }
            failure{
                ToastUtil.toastShow(this@MainActivity,"获取拍照权限失败")
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_take_photo->{
                //todo 可以改进，比如传入图片类型作为参数
//                takePhotoLauncher.launch(null)
                //我去安卓6崩了
//                requestPermissionsLauncher.launch(PERMISSIONS)
//                if(success){
//                    takePhotoLauncher.launch(null)
//                }else{
//                    ToastUtil.toastShow(this@MainActivity,"获取拍照权限失败")
//                }
//                permissions()
                requestCameraPermissionsLauncher.launch(PERMISSIONS)
            }
            R.id.btn_select_photo->{
//                requestPermissionsLauncher.launch(PERMISSIONS)
//                if(success){
//                    selectPhotoLauncher.launch(null)
//                }else{
//                    ToastUtil.toastShow(this@MainActivity,"访问图片权限失败")
//                }
                //todo 测试kotlin的invoke方法 已测试，可以执行方法变量的方法
                requestPhotoPermissionsLauncher.launch(PERMISSIONS)
            }
        }
    }


}