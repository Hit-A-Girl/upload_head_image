package com.example.cameraapp.utils;

/**
 * @Author Martlet
 * @Date 2023/8/1 19:27
 * @Description
 * 如果真的有一天要适配所有机型
 *从相册中获取文件真实路径除了这里再参考
 * https://blog.csdn.net/qq_41904106/article/details/115264306
 */

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileUtilTemp {
    private static final String TAG = "FileUtils";
    private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmssSS");

    private FileUtilTemp() {
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{"_data"};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow("_data");
                String var8 = cursor.getString(column_index);
                return var8;
            }
        } catch (IllegalArgumentException var12) {
            Log.i("FileUtils", String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", var12.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

        return null;
    }

    @SuppressLint({"NewApi"})
    public static String getPath(Context context, Uri uri) {
        boolean isKitKat = VERSION.SDK_INT >= 19;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String id;
            String[] split;
            String type;
            if (isExternalStorageDocument(uri)) {
                id = DocumentsContract.getDocumentId(uri);
                split = id.split(":");
                type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    try {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, (String)null, (String[])null);
                    } catch (NumberFormatException var9) {
                        Log.i("FileUtils", var9.getMessage());
                        return null;
                    }
                }
            } else if (isMediaDocument(uri)) {
                id = DocumentsContract.getDocumentId(uri);
                split = id.split(":");
                type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, "_id=?", selectionArgs);
            }
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, (String)null, (String[])null);
            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static boolean copyFile(FileInputStream fileInputStream, String outFilePath) throws IOException {
        if (fileInputStream == null) {
            return false;
        } else {
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;

            boolean var5;
            try {
                inputChannel = fileInputStream.getChannel();
                outputChannel = (new FileOutputStream(new File(outFilePath))).getChannel();
                inputChannel.transferTo(0L, inputChannel.size(), outputChannel);
                inputChannel.close();
                boolean var4 = true;
                return var4;
            } catch (Exception var9) {
                var5 = false;
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }

                if (inputChannel != null) {
                    inputChannel.close();
                }

                if (outputChannel != null) {
                    outputChannel.close();
                }

            }

            return var5;
        }
    }

    public static void copyFile(@NonNull String pathFrom, @NonNull String pathTo) throws IOException {
        if (!pathFrom.equalsIgnoreCase(pathTo)) {
            FileChannel outputChannel = null;
            FileChannel inputChannel = null;

            try {
                inputChannel = (new FileInputStream(new File(pathFrom))).getChannel();
                outputChannel = (new FileOutputStream(new File(pathTo))).getChannel();
                inputChannel.transferTo(0L, inputChannel.size(), outputChannel);
                inputChannel.close();
            } finally {
                if (inputChannel != null) {
                    inputChannel.close();
                }

                if (outputChannel != null) {
                    outputChannel.close();
                }

            }

        }
    }

    public static String getCreateFileName(String prefix) {
        long millis = System.currentTimeMillis();
        return prefix + sf.format(millis);
    }

    public static String getCreateFileName() {
        long millis = System.currentTimeMillis();
        return sf.format(millis);
    }

    public static String rename(String fileName) {
        String temp = fileName.substring(0, fileName.lastIndexOf("."));
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return temp + "_" + getCreateFileName() + suffix;
    }
    //////////////////////////////

    /**
     * 根据Uri获取文件绝对路径，解决Android4.4以上版本Uri转换 兼容Android 10
     *
     * @param context
     * @param imageUri
     */
    public static String getFileAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }

        if (VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return getRealFilePath(context, imageUri);
        }

        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && VERSION.SDK_INT < Build.VERSION_CODES.Q
                && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        if (VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return uriToFileApiQ(context,imageUri);
        }
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri)) {
                return imageUri.getLastPathSegment();
            }
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    //此方法 只能用于4.4以下的版本
    private static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] projection = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

//            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
    /**
     * Android 10 以上适配 另一种写法
     * todo 怎么不管用？
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("Range")
    public static String getFileFromContentUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try {
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                return filePath;
            } catch (Exception e) {
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * Android 10 以上适配
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("Range")
    private static String uriToFileApiQ(Context context, Uri uri) {
        File file = null;
        //android10以上转换
        LogUtil.Companion.d("uri.toString():"+uri.toString());
        LogUtil.Companion.d("uri.getScheme():"+uri.getScheme());
        LogUtil.Companion.d("ContentResolver.SCHEME_FILE:"+ContentResolver.SCHEME_FILE);
        LogUtil.Companion.d("ContentResolver.SCHEME_CONTENT:"+ContentResolver.SCHEME_CONTENT);

        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(context.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    FileUtils.copy(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        assert file != null;
        return file.getAbsolutePath();
    }


    /**
     * 通过文件路径 uri的转字符也可以
     * @param filePath
     * @return
     */
    public static String getMimeType(String filePath) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }




    public static String getImagePathOnKitKat(Context context,Uri uri){
        String imgPath = null;
        LogUtil.Companion.d("uri:"+uri);
        if(DocumentsContract.isDocumentUri(context,uri)){
            LogUtil.Companion.d("uri.getAuthority():"+uri.getAuthority());
            String docId = DocumentsContract.getDocumentId(uri);
            LogUtil.Companion.d("DocumentsContract.getDocumentId(uri):"+docId);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = Media._ID+"="+id;
                imgPath = getImgPath(Media.EXTERNAL_CONTENT_URI,selection,context);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                //突然可能发现有问题 1：这个docId不是纯数字，必错。可能是拆解后的id吧
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.parseLong(docId));
                imgPath = getImgPath(contentUri,null,context);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imgPath = getImgPath(uri,null,context);
        }


        return imgPath;
    }
    ///////////从别处复制而来//////////////
    /**
     * 4.4及以上系统处理图片的方法
     * @param context
     * @param uri
     * @return
     * 这个方法虽然在安卓6以上都试过了，没有问题，但是不是表示测试了所有的情况，
     */
    public static String getImageOnKitKatPath(Context context, Uri uri){
        String imgPath = null;
//        Uri uri = intent.getData();
//        ContentResolver.SCHEME_CONTENT
        Log.d("zhen","uri="+uri.toString());
        //这个if的必要性是什么？为false又怎样？从手机中获取文件都是DocumentUri
        if(DocumentsContract.isDocumentUri(context,uri)){
            //数据表里指定的行
            String docId = DocumentsContract.getDocumentId(uri);
            Log.d("zhen","DocumentsContract.getDocumentId="+docId);
            Log.d("zhen","uri.getAuthority()="+uri.getAuthority());
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = Media._ID+"="+id;
                imgPath = getImgPath(Media.EXTERNAL_CONTENT_URI,selection,context);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                imgPath = getImgPath(contentUri,null,context);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imgPath = getImgPath(uri,null,context);
        }
        return imgPath;
    }

    /**
     *通uri和selection来获取真实的图片路径，从相册获取图片时要用
     * @param uri
     * @param selection
     * @param context
     * @return
     */
    @SuppressLint("Range")
    private static String getImgPath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

}