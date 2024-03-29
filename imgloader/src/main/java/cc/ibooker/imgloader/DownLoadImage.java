package cc.ibooker.imgloader;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用来获取网络图片-没有缓存
 *
 * @author 邹峰立
 */
public class DownLoadImage {
    private MyHandler handler;
    private ExecutorService executorService;
    private ImageCallBack callBack;

    public DownLoadImage() {
        handler = new MyHandler(this);
    }

    // 销毁方法
    public void destory() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (executorService != null)
            executorService.shutdownNow();
    }

    // 加载图片
    public void loadImage(final String imagePath, ImageCallBack callBack) {
        this.callBack = callBack;
        // 开启线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable drawable;
                    drawable = Drawable.createFromStream(new URL(imagePath).openStream(), "");
                    Message message = Message.obtain();
                    message.obj = drawable;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (executorService == null)
            executorService = Executors.newCachedThreadPool();
        executorService.execute(thread);
    }

    // 自定义Handler
    private static class MyHandler extends Handler {
        private WeakReference<DownLoadImage> mWef;

        MyHandler(DownLoadImage downLoadImage) {
            mWef = new WeakReference<>(downLoadImage);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Drawable drawable;
            drawable = (Drawable) msg.obj;
            if (mWef.get().callBack != null)
                mWef.get().callBack.getDrawable(drawable);
        }
    }

    // 对外提供回调
    public interface ImageCallBack {
        void getDrawable(Drawable drawable);
    }

}
