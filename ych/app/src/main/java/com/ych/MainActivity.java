package com.ych;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

        private ImageView mPhoto;

        private Button shareBtn;
        private int flag=-1;
        private IWXAPI iwxapi;

        private Bitmap getPic()
        {
            shareBtn.setVisibility(View.INVISIBLE);
            View view =getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            return view.getDrawingCache();
        }
        private void wxshare(int flag) {
            // TODO Auto-generated method stub
            WXWebpageObject webpage=new WXWebpageObject();
            WXMediaMessage msg=new WXMediaMessage();
            msg.mediaObject=new WXImageObject(getPic());

            SendMessageToWX.Req req=new SendMessageToWX.Req();
            req.transaction=String.valueOf(System.currentTimeMillis());
            req.message=msg;
            req.scene=flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
            //req.scene=SendMessageToWX.Req.WXSceneSession;
            iwxapi.sendReq(req);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

            // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
            setContentView(R.layout.activity_main);

            iwxapi=WXAPIFactory.createWXAPI(this, "wx8e58872f530c9113");   //appId
            iwxapi.registerApp("wx8e58872f530c9113");

            mPhoto=(ImageView) findViewById(R.id.pic);
            shareBtn=(Button)findViewById(R.id.fxbtn);
            mPhoto.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    intent.putExtra("crop1", "true");
                    // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
                    startActivityForResult(intent, 100);
                }
            });

            shareBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    final String[] item=new String[]{"分享给好友","分享到朋友圈"};
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("分享到")
                            //.setMessage("分享到哪？")
                            .setItems(item, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int witch) {
                                    // TODO Auto-generated method stub
                                    flag=witch;
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    flag=-1;
                                }
                            })

                            .show();
                    if(flag>=0){
                        wxshare(flag);

                        shareBtn.setVisibility(View.VISIBLE);
                    }

                }



            });

        }
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);

        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        this.startActivityForResult(intent, 300);
    }
        @Override
        protected void onActivityResult(int requestCode,int resultCode,Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100.) {
                // 从相册返回的数据
                if (data != null) {

                    // 得到图片的全路径
                    Uri uri = data.getData();
                    crop(uri);
                }
            } else if (requestCode == 300) {
                // 从剪切图片返回的数据
                if (data != null) {
                    Bitmap rawBitmap = data.getParcelableExtra("data");
                    int a =rawBitmap.getByteCount();
                    Log.i("rawBitmap", String.valueOf(a));
                    Bitmap rawBitmap1=compressImage(rawBitmap);
                    int b =rawBitmap1.getByteCount();
                    Log.i("rawBitmap1", String.valueOf(b));
                    mPhoto.setImageBitmap(rawBitmap1);

                }


            }
            }


//        @Override
//        public boolean onCreateOptionsMenu(Menu menu) {
//            // Inflate the menu; this adds items to the action bar if it is present.
//            getMenuInflater().inflate(R.menu.main, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            // Handle action bar item clicks here. The action bar will
//            // automatically handle clicks on the Home/Up button, so long
//            // as you specify a parent activity in AndroidManifest.xml.
//            int id = item.getItemId();
//            if (id == R.id.action_settings) {
//                return true;
//            }
//            return super.onOptionsItemSelected(item);
//        }
private Bitmap compressImage(Bitmap image) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
    Log.i("baos", String.valueOf(baos.toByteArray().length));
    int options = 100;
    while ( baos.toByteArray().length / 1024>1) {  //循环判断如果压缩后图片是否大于32kb,大于继续压缩
        baos.reset();//重置baos即清空baos
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        options -= 1;//每次都减少1
    }
    Log.i("options", String.valueOf(options));
    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
    int aa = bitmap.getByteCount();
    Log.i("rrrrrrrrrrrrr", String.valueOf(aa));
    return bitmap;
}
}