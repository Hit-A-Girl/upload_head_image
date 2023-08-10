package com.example.cameraapp.base

import androidx.appcompat.app.AppCompatActivity
import com.example.cameraapp.activityresult.RequestPermissionContract

/**
 * @Author Martlet
 * @Date 2023/8/9 19:04
 * @Description不行啊，不能给出Result的初始化，应该是每有一个权限请求群就一有一个自定义的方法才对
 * 不过还是保留这个BaseActivity，将来可能改成别的用
 */
abstract class BaseActivity<P>:AppCompatActivity() where P:RequestPermissionContract.Result{
    lateinit var p:P
    val requestPermissionsLauncher = registerForActivityResult(RequestPermissionContract(p)){p->


    }
}