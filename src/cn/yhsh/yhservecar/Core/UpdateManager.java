package cn.yhsh.yhservecar.Core;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.util.Random;

public class UpdateManager {

    private Context mContext;

    //提示语  
    private String updateMsg = "有最新的软件包哦，亲快下载吧~";

    //返回的安装包url  
    private String apkUrl = "http://222.170.86.83:88/Public/apk/yhservecar.apk";


    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路径 */

    private static final String saveFileName = "yhservecar.apk";


    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;


    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int FAILED = 3;

    private int progress;

    private Thread downLoadThread;

    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:

                    installApk();
                    break;
                case FAILED:
                    downloadDialog.dismiss();
                default:
                    break;
            }
        }

        ;
    };
    private HttpHandler mFileHttpHandler;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    //外部接口让主Activity调用  
    public void checkUpdateInfo() {
        showNoticeDialog();
    }


    private void showNoticeDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mFileHttpHandler != null) {
                    mFileHttpHandler.cancel();
                }
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private boolean finished = false;
    private String randomName;
    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            finished = false;
            File folder;
            do {
                Random random = new Random(System.nanoTime());
                randomName = String.valueOf(random.nextInt(100000));
                folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), randomName);
            } while (folder.exists());
            folder.mkdir();

            File ApkFile = new File(folder, saveFileName);

            HttpUtils http = new HttpUtils();
            mFileHttpHandler = http.download(apkUrl,
                    ApkFile.getPath(),
                    true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
                    false, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
                    new RequestCallBack<File>() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onLoading(long total, long current, boolean isUploading) {
                            progress = (int) (((float) current / total) * 100);
                            //更新进度
                            mHandler.sendEmptyMessage(DOWN_UPDATE);
                        }

                        @Override
                        public void onSuccess(ResponseInfo<File> responseInfo) {
                            mHandler.sendEmptyMessage(DOWN_OVER);

                        }


                        @Override
                        public void onFailure(HttpException error, String msg) {
                            Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                            mHandler.sendEmptyMessage(FAILED);
                        }
                    });
        }
    };

    /**
     * 下载apk
     */

    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), randomName);
        if (!folder.exists()) {
            return;
        }
        File apkFile = new File(folder, saveFileName);
        if (!apkFile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);

    }


} 