package com.gome.gomeaccountservice.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;
import android.util.Log;

import com.gome.gomeaccountservice.Constants;

public class BitmapUtils {
	static final String TAG = Constants.TAG_PRE+"BitmapUtils";
	public static Bitmap compressBitmap(Bitmap bit) {
		Matrix matrix = new Matrix();
		matrix.setScale(Constants.IMAGE_SCALE, Constants.IMAGE_SCALE);
		Bitmap bm = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
				bit.getHeight(), matrix, true);
		bitmapToBytes(bm);
		return bm;
	}
	private static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, Constants.IMAGE_COMPRESS_QUALITY, baos);
		return baos.toByteArray();
	}
	/**
	 * 裁剪出正方形区域
	 * @param bitmap
	 * @param isRecycled
	 * @return
	 */
	public static Bitmap imageCropSquare(Bitmap bitmap, boolean isRecycled) {
		if (bitmap == null) {
			return null;
		}

		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();

		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

		int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
		int retY = w > h ? 0 : (h - w) / 2;

		Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
				false);
		if (isRecycled && bitmap != null && !bitmap.equals(bmp)
				&& !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}

		// 下面这句是关键
		return bmp;// Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
					// false);
	}
	/**
	 * 将bitmap转换成base64字符串
	 * @param bit
	 * @return
	 */
	public static String bitmap2StrByBase64(Bitmap bit) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bit.compress(CompressFormat.PNG, Constants.IMAGE_COMPRESS_QUALITY, bos);// 参数100表示不压缩
		byte[] bytes = bos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}
	/**
	 * 将bitmap保存到本地路径
	 */
	public static void bitmap2File(Bitmap bit,String fileName) {
		File dir = new File(Constants.HEAD_PORTRAIT_DIR_PATH);
		if(!dir.exists()){
			Log.i(TAG, "bitmap2File() dir not exists!mkdir dir:"+dir);
			dir.mkdir();
		}
		
		File f = new File(Constants.HEAD_PORTRAIT_DIR_PATH, fileName);
		Log.i(TAG, "Bitmap2File() fileName:"+fileName);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			bit.compress(CompressFormat.PNG, Constants.IMAGE_COMPRESS_QUALITY, out);// 参数100表示不压缩
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.i(TAG, "Bitmap2File() error:"+e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.i(TAG, "Bitmap2File() error:"+e.toString());
			e.printStackTrace();
		}

	}
}
