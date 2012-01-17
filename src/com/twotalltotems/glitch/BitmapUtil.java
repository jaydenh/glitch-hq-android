package com.twotalltotems.glitch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Gallery;
import android.widget.ImageView;

public class BitmapUtil
{
	  static private String TAG = "BitmapUtil";
	
	  public static Bitmap CreateTransparentOvalBitmap( int nBitmapW, int nBitmapH, RectF r, int nInColor, int nOutColor )
	  {
	    	Bitmap  bmMask = Bitmap.createBitmap( nBitmapW, nBitmapH, Bitmap.Config.ARGB_8888 ); 
	    	Canvas cvsMask = new Canvas(bmMask);

	    	Paint paint = new Paint();
	        paint.setStyle( Paint.Style.FILL );
	        paint.setAntiAlias(true);

	    	paint.setColor(nOutColor);
	    	cvsMask.drawRect( 0, 0, nBitmapW, nBitmapH, paint ); 

	    	paint.setColor(nInColor);
	    	cvsMask.drawOval( r, paint );
	    	
	    	return bmMask;
	  }

	  public static void MultiplyPaint( Canvas cvs, Paint paint, Bitmap bmMask)
	  {
		  paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.MULTIPLY )  );
		  cvs.drawBitmap( bmMask, 0, 0, paint );
	  }
	  
	  public static Bitmap CreateOvalImage( Bitmap orgBmp )
	  {
	    	int nW = 50;
	    	int nH = 50;

	    	int nW0 = orgBmp.getWidth();
			int nH0 = orgBmp.getHeight();
			
			Rect r = new Rect();
			r.left = nW0/6;
			r.top =  nH0/11;
			r.right =nW0*12/13;
			r.bottom = (int)( nH0 * 0.65 ); 
	    	
	    	Bitmap  bmNew = Bitmap.createBitmap( nW, nH, Bitmap.Config.ARGB_8888 ); 
	        
	    	Canvas cvs = new Canvas(bmNew);
	    	
	        Paint paint = new Paint();
	        paint.setStyle( Paint.Style.STROKE );
	        paint.setAntiAlias(true);
	        paint.setColor(0xffc0c0c0);

	        cvs.drawCircle(nW/2, nH/2, nW/2, paint);
	        
	        RectF rDest = new RectF();
	        rDest.left = 1;   
	        rDest.right = nW-2;
	        rDest.top = 1;
	        rDest.bottom = nH-2;
	        
	        cvs.drawBitmap(orgBmp, r, rDest, paint);
	        
	    	return bmNew;
	  }
	  
	  public static void Paste( Bitmap largeBmp, Bitmap newBmp, float faceX, float faceY )
	  {
	    	Canvas cvs = new Canvas(largeBmp);
	    	
	        Paint paint = new Paint();
	        paint.setStyle( Paint.Style.FILL );

        	cvs.drawBitmap( newBmp, faceX, faceY, paint );
	  }
	  
	  public static Bitmap ShowLibraryImage( Activity act, Uri uri, ImageView imgv )
	  {
	    	String img_path = ImageUri2Path(act,uri);
	    	return ShowImage( img_path,act,imgv, 0 );
	  }
	  
	  public static DisplayMetrics GetScreenSize( Activity act )
	  {
		  DisplayMetrics metrics = new DisplayMetrics();
		  act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		  return metrics;
	  }
	  
	  public static Bitmap ScaleDown( Bitmap bmp, int maxW, int maxH )
	  {
		  if( maxW < bmp.getWidth() || maxH < bmp.getHeight() )
		  {
			  float ratioW = ((float)bmp.getWidth())/maxW;
			  float ratioH = ((float)bmp.getHeight())/maxH;
			  
			  if( ratioW > ratioH )
				  maxH = (int) ( bmp.getHeight() / ratioW );
			  else
				  maxW = (int) ( bmp.getWidth() / ratioH );
					  
			  return Bitmap.createScaledBitmap(bmp, maxW, maxH, false );
		  }else
			  return bmp;
	  }
	  
	  public static Bitmap ShowImage(String path, Activity act, ImageView imgv, int maxSize)
	  {
		  if( maxSize <= 0 )
		  {
			  int nW = imgv.getWidth();
			  int nH = imgv.getHeight();
			  
			  nW = Math.max(nW,nH);
			  if( nW == 0 )
			  {	  
				  DisplayMetrics metrics = GetScreenSize(act);
				  nW = Math.max(metrics.widthPixels, metrics.heightPixels);
			  }
			  maxSize = nW;
		  }
		  Bitmap bm = BitmapResampleFromStream(path,maxSize,maxSize);
		  imgv.setImageBitmap(bm);
		  
		  return bm;
	  }
	  
	  private static Bitmap BitmapResampleFromStream( String path,int outW,int outH )
	  {
	        Bitmap b = null;

			FileInputStream is;
			try {
				is = new FileInputStream( new File(path) );
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return null;
			}
	        
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        BitmapFactory.decodeStream(is, null, o);

	        int scale = 1;
	        if ( o.outHeight > outW || o.outWidth > outH ) 
	        {
	            scale = (int) Math.pow(2, (int) Math.round(Math.log(outW / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	            Log.i( TAG, " scale: " + scale + " outW: " + outW + " outH: " + outH );
	        } 

	        try {
				is = new FileInputStream( new File(path) );

		        BitmapFactory.Options o2 = new BitmapFactory.Options();
		        o2.inSampleSize = scale;
		        b = BitmapFactory.decodeStream( is, null, o2 );
	        
	        } catch (Exception e) {
				e.printStackTrace();
			}

	        if( b == null )
	        	Log.e( TAG," bitmap is null." );
	        
	        return b;
	  }    
	  
	  public static void GalleryAlignLeft( Activity act, Gallery gly, int offset )
	  {
	        int offsetX = GetScreenSize(act).widthPixels;
	        
	        MarginLayoutParams mlp = (MarginLayoutParams)gly.getLayoutParams();
	        mlp.setMargins(-offsetX + offset, 
	                       mlp.topMargin, 
	                       0, 
	                       mlp.bottomMargin );
	  }
	  
	  public static String ImageUri2Path( Activity act, Uri uri )
	  {
	    	if( uri == null )
	    		return null;

	    	String[] proj = { MediaStore.Images.Media.DATA };
	    	Cursor actualimagecursor = act.managedQuery(uri, proj,null, null, null);
	    	int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); actualimagecursor.moveToFirst();
	    	return actualimagecursor.getString(actual_image_column_index);
	  }
	  
	  public static boolean SaveJPEG( Bitmap bm, String sRelativePath )
	  {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 90, bos );
			byte[] data = bos.toByteArray();
			
			try{
				File file = new File( Environment.getExternalStorageDirectory(), sRelativePath );
				if( !file.exists() )
					file.createNewFile();
				
				FileOutputStream fos = new FileOutputStream( file );  
				fos.write(data);
				fos.close();
				
				return true;
			}catch( Exception e)
			{
				e.printStackTrace();
			}
			return false;
	  }
	  
	  public static Bitmap GetMirror( Bitmap bm )
	  {
	     float[] mirrorM = 
	     {  	-1, 0, 0, 
	    		 0, 1, 0,  
	    		 0, 0, 1    
	     };

	     Matrix m = new Matrix();
	     m.setValues(mirrorM);
	     return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false);
	  }	  
};
