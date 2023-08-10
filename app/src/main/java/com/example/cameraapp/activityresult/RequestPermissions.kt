package com.example.cameraapp.activityresult

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.security.cert.Extension

/**
 * @Author Martlet
 * @Date 2023/8/10 15:08
 * @Description根据网上资料尝试封装ActivityResult请求权限的api
 * 但是最终可能还是失败了
 * java.lang.IllegalStateException: LifecycleOwner com.example.cameraapp.MainActivity@b5fdeae is attempting to
 * register while current state is RESUMED. LifecycleOwners must call register before they are STARTED.
 * 很不幸，但是还是留作纪念吧，成为我曾走过的弯路，加油
 */
/**
 * 多个权限
 */
class MultiplePermissionsBuilder{
    /**
     * 多个权限全部被允许时回调
     */
    var success:()->Unit={}
    var failure:(List<String>)->Unit={}
    fun success(callback:()->Unit){
        this.success = callback
    }
    fun failure(callback: (List<String>) -> Unit){
        this.failure = callback
    }
}
/**
 * Activity/Fragment权限扩展函数
 */
private inline fun ComponentActivity.requestMultiplePermissions(
    permissions:Array<String>,
    crossinline success:()->Unit={},
    crossinline failure:(List<String>)->Unit={}
){
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        //todo 找个类似的map测试一下输出的结果，不出意外就是个List集合
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        //todo 第一次见到这种写法，when直接{}
        when{
            deniedList.isNotEmpty()->{
                failure.invoke(deniedList)
            }
            else->{
                success.invoke()
            }
        }
    }.launch(permissions)
}

/**
 * 申请多个权限
 */
fun AppCompatActivity.permissions(
    permissions:Array<String>,
    extension: MultiplePermissionsBuilder.()->Unit
){
    val builder = MultiplePermissionsBuilder()
    builder.apply(extension)
    requestMultiplePermissions(
        //todo 这个*之前好像见过，是什么作用来着
        permissions,
        builder.success,
        builder.failure
    )
}
private inline fun Fragment.requestMultiplePermissions(
    permissions: Array<String>,
    crossinline success: () -> Unit={},
    crossinline failure: (List<String>) -> Unit={}
){
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){resultMap->
        val deniedList = resultMap.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty()->{
                failure.invoke(deniedList)
            }
            else->{
                success.invoke()
            }
        }
    }.launch(permissions)
}
/**
 * 申请多个权限
 */
fun Fragment.permissions(
    permissions:Array<String>,
    extension: MultiplePermissionsBuilder.() -> Unit
){
    val builder = MultiplePermissionsBuilder()
    builder.apply(extension)
    requestMultiplePermissions(
        permissions,
        builder.success,
        builder.failure
    )
}
