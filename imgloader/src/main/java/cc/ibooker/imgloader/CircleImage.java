package cc.ibooker.imgloader;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * 实现圆角图片Bitmap
 *
 * @author 邹峰立
 */
public class CircleImage {
    // 获得圆角图片的方法
    private Paint paint = new Paint();

    public Bitmap toRoundCorner(Bitmap bitmap) {
        try {
            // 创建一个bitmap
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            int x = bitmap.getWidth();
            int y = bitmap.getHeight();
            int c = Math.min(x, y);
            // 画圆
            canvas.drawCircle(x / 2, y / 2, c / 2, paint);
            // 是图片全部重叠
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
