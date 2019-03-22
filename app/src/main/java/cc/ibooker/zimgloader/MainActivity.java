package cc.ibooker.zimgloader;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import cc.ibooker.imgloader.DownLoadImage;
import cc.ibooker.imgloader.ImgLoader;


public class MainActivity extends AppCompatActivity {
    private ImgLoader imgLoader;
    private DownLoadImage downLoadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.image);
        final ImageView imageView1 = findViewById(R.id.image1);

        String url = "http://ww2.sinaimg.cn/large/85cccab3gw1etdghsr1xxg20ci071nlp.jpg";

        imgLoader = new ImgLoader();
        imgLoader.placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .setQuality(10)
                .formatType(ImgLoader.IMAGETYPE.PNG)
                .setColorFilter(0.5f)
                .tint(Color.parseColor("#FF0000"))
                .setImageLoadErrorListener(new ImgLoader.ImageLoadErrorListener() {
                    @Override
                    public void onImageLoadErrorListener(ImageView imageView, String url, boolean isCircleImg, String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }).showImage(imageView, url, true);
//        imgLoader.showImageByAsyncTask(imageView2, url, false);

        downLoadImage = new DownLoadImage();
        downLoadImage.loadImage(url, new DownLoadImage.ImageCallBack() {
            @Override
            public void getDrawable(Drawable drawable) {
                imageView1.setImageDrawable(drawable);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downLoadImage != null)
            downLoadImage.destory();
    }
}
