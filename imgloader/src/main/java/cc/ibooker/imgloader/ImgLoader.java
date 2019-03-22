package cc.ibooker.imgloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

/**
 * 采用HttpURLConnection实现图片加载器
 *
 * @author 邹峰立
 */
public class ImgLoader {
    public enum IMAGETYPE {
        PNG, JPEG, WEBP
    }

    private Bitmap.CompressFormat imageType = Bitmap.CompressFormat.PNG;
    private int quality = 50;
    private int defaultResId;
    private int errorResId;
    private ColorMatrixColorFilter filter;
    private int tintColor;
    // 任务集合，一般用于列表中图片于滚动过程中暂停和停止滚动时候下载
    private HashSet<NewsAsyncTask> hashSet;
    // 设置缓存mCache
    private LruCache<String, Bitmap> mCache;

    // 定义构造方法
    public ImgLoader() {
        synchronized (ImgLoader.class) {
            if (mCache == null) {
                hashSet = new HashSet<>();
                int maxMemory = (int) Runtime.getRuntime().maxMemory();// 获取最大可用内存
                int cacheSize = maxMemory / 4; // 设置缓存大小是总空间的1/4
                mCache = new LruCache<String, Bitmap>(cacheSize) {
                    protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                        // 在每次存入缓存的时候调用
                        return value.getByteCount();// 返回bitmap的实际大小
                    }
                };
            }
        }

    }

    // 保存到缓存当中
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (getBitmapFromCache(url) == null) {
            mCache.put(url, bitmap);
        }
    }

    // 从缓存中获取bitmap
    private Bitmap getBitmapFromCache(String url) {
        return mCache.get(url);
    }

    // 通过url获取bitmap
    private Bitmap getBitmapFromUrl(String urlStr) throws Exception {
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());// 字节流到字符流
            bitmap = BitmapFactory.decodeStream(is);// 获取bitmap
            connection.disconnect();// 释放资源
            return bitmap;
        } finally {
            if (is != null)
                is.close();
        }
    }

    // 显示默认图片
    private void showDefault(ImageView mImageView, boolean isCircleImg) {
        // 设置默认图片
        if (defaultResId > 0 && mImageView != null) {
            if (filter != null)
                mImageView.setColorFilter(filter);
            if (isCircleImg) {
                Bitmap bitmap = BitmapFactory.decodeResource(mImageView.getResources(), defaultResId);
                mImageView.setImageBitmap(new CircleImage().toRoundCorner(bitmap));
            } else
                mImageView.setImageResource(defaultResId);
        }
    }

    // 显示默认图片
    private void showError(ImageView mImageView, boolean isCircleImg) {
        // 设置默认图片
        if (errorResId > 0 && mImageView != null) {
            if (filter != null)
                mImageView.setColorFilter(filter);
            if (isCircleImg) {
                Bitmap bitmap = BitmapFactory.decodeResource(mImageView.getResources(), errorResId);
                mImageView.setImageBitmap(new CircleImage().toRoundCorner(bitmap));
            } else
                mImageView.setImageResource(errorResId);
        }
    }

    // 显示图片
    private void showBitmap(ImageView mImageView, Bitmap bitmap, boolean isCircleImg) {
        if (mImageView != null && bitmap != null) {
            if (filter != null)
                mImageView.setColorFilter(filter);
            if (tintColor > 0) {
                Drawable drawable = new BitmapDrawable(bitmap);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable.setTint(tintColor);
                }
            }
            if (isCircleImg) {
                mImageView.setImageBitmap(new CircleImage().toRoundCorner(bitmap));
            } else {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 上面是使用多线程，下面另一种方法使用异步
     *
     * @param mImageView 带显示的ImageView
     * @param url        加载的图片Url
     */
    public ImgLoader showImage(ImageView mImageView, String url, boolean isCircleImg) {
        // 显示默认图片
        showDefault(mImageView, isCircleImg);
        // 首先判断要获取的对象是否在缓存中存在，没有就获取，有则直接显示
        Bitmap bitmap = getBitmapFromCache(url);
        if (bitmap == null) {
            // 加载图片
            NewsAsyncTask newsAsyncTask = new NewsAsyncTask(mImageView, url, isCircleImg);
            newsAsyncTask.execute();
            hashSet.add(newsAsyncTask);
        } else {
            showBitmap(mImageView, bitmap, isCircleImg);
        }
        return this;
    }

    /**
     * 默认图片
     *
     * @param res 图片地址
     */
    public ImgLoader placeholder(int res) {
        this.defaultResId = res;
        return this;
    }

    /**
     * 错误图片
     *
     * @param res 图片地址
     */
    public ImgLoader error(int res) {
        this.errorResId = res;
        return this;
    }

    /**
     * 图片格式转换
     */
    public ImgLoader formatType(IMAGETYPE imagetype) {
        if (imagetype == IMAGETYPE.JPEG)
            imageType = Bitmap.CompressFormat.JPEG;
        else if (imagetype == IMAGETYPE.PNG)
            imageType = Bitmap.CompressFormat.PNG;
        else if (imagetype == IMAGETYPE.WEBP)
            imageType = Bitmap.CompressFormat.WEBP;
        return this;
    }

    /**
     * 图片质量压缩
     *
     * @param quality 0 - 100
     */
    public ImgLoader setQuality(int quality) {
        if (quality <= 0
                || quality >= 100)
            this.quality = 100;
        this.quality = quality;
        return this;
    }

    /**
     * 设置图片色彩透明度
     *
     * @param sat 0-1
     */
    public ImgLoader setColorFilter(float sat) {
        if (sat >= 1)
            sat = 1;
        if (sat <= 0)
            sat = 0;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(sat);// 饱和度 0灰色 100过度彩色，50正常
        filter = new ColorMatrixColorFilter(matrix);
        return this;
    }

    // 着色
    public ImgLoader tint(int tintColor) {
        this.tintColor = tintColor;
        return this;
    }

    // 缩放图片
    private Bitmap pressBitmap(Bitmap bmp) throws Exception {
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream isBm = null;
        try {
            baos = new ByteArrayOutputStream();
            bmp.compress(imageType, quality, baos);
            // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到
            Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
            bmp.recycle();
            System.gc(); // 显示位图
            return bitmap;
        } finally {
            if (baos != null)
                baos.close();
            if (isBm != null)
                isBm.close();
        }

    }

    // 定义异步
    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView mImageView;
        private String mUrl;
        private boolean isCircleImg;// 是否显示圆角ImageView
        private String errorMsg = "未知异常错误！";

        // 构造方法
        NewsAsyncTask(ImageView mImageView, String mUrl, boolean isCircleImg) {
            this.mImageView = mImageView;
            this.mUrl = mUrl;
            this.isCircleImg = isCircleImg;
        }

        @Override
        protected Bitmap doInBackground(String... arg0) {
            // 从网络上获取图片
            Bitmap bitmap;
            try {
                Bitmap newBitmap = getBitmapFromUrl(mUrl);
                if (newBitmap != null) {
                    bitmap = pressBitmap(newBitmap);
                    if (bitmap != null) {
                        // 添加到缓存当中
                        addBitmapToCache(mUrl, bitmap);
                    }
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMsg = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mImageView != null) {
                if (result != null)
                    showBitmap(mImageView, result, isCircleImg);
                else if (errorResId > 0) {
                    showError(mImageView, isCircleImg);
                    if (imageLoadErrorListener != null)
                        imageLoadErrorListener.onImageLoadErrorListener(mImageView, mUrl, isCircleImg, errorMsg);
                }
            }
            // 移除当前任务
            if (hashSet != null)
                hashSet.remove(this);
        }
    }

    // 开始下载图片
    public void startListImageLoad() {
        for (NewsAsyncTask newsAsyncTask : hashSet) {
            newsAsyncTask.execute();
        }
    }

    // 暂停下载图片
    public void stopListImageLoad() {
        for (NewsAsyncTask newsAsyncTask : hashSet) {
            newsAsyncTask.cancel(true);
        }
    }

    // 图片加载出错的接口
    public interface ImageLoadErrorListener {
        void onImageLoadErrorListener(ImageView imageView, String url, boolean isCircleImg, String message);
    }

    private ImageLoadErrorListener imageLoadErrorListener;

    public ImgLoader setImageLoadErrorListener(ImageLoadErrorListener imageLoadErrorListener) {
        this.imageLoadErrorListener = imageLoadErrorListener;
        return this;
    }
}
