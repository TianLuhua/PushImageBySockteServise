package com.action.servise.pushimagebysockteservise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.action.servise.pushimagebysockteservise.utils.FileUtil;
import com.action.servise.pushimagebysockteservise.utils.HideSystemUIUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideSystemUIUtils.hideSystemUI(this);
        setContentView(R.layout.activity_main);
        initview();
        initSockte();
    }

    private void initview() {
        mImageview = (ImageView) findViewById(R.id.action_image);
    }


    private void initSockte() {
        new Thread(new TcpServer()).start();
    }

    private class TcpServer implements Runnable {
        private static final int PORT = 40000;
        private boolean mIsServiceDestroyed = false;


        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(PORT);
            } catch (IOException e) {
                Log.e("tlh", "建立链接失败, 端口:" + PORT);
                e.printStackTrace();
                return; // 链接建立失败直接返回
            }

            while (!mIsServiceDestroyed) {
                try {
                    final Socket client = serverSocket.accept();
                    Log.e("tlh", "接收数据");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private void responseClient(Socket client) throws IOException {
            DataInputStream dos = new DataInputStream(client.getInputStream());
            byte[] head = new byte[3];
            byte[] buf = null;
            dos.read(head);
            int len = bufferToInt(head);

            if (len > 0) {
                buf = new byte[len];
                dos.readFully(buf);
            }
            final Bitmap bitmap =  BitmapFactory.decodeByteArray(buf,0,buf.length);
           /* ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
            bitmap.copyPixelsFromBuffer(byteBuffer);*/

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mImageview.setImageBitmap(bitmap);
                }
            });

//            File fileImage = new File(FileUtil.getScreenShotsName(getApplicationContext()));
//            if (!fileImage.exists()) {
//                Log.e("tlh", "fileImage.path: " + fileImage.getAbsolutePath());
////                fileImage.createNewFile();
//            }
//            FileOutputStream out = new FileOutputStream(fileImage);
//            if (out != null) {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//                out.flush();
//                out.close();
//                Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri contentUri = Uri.fromFile(fileImage);
//                media.setData(contentUri);
//                sendBroadcast(media);
//            }

            Log.e("tlh", "len = " + len);
        }
    }

    public static int bufferToInt(byte[] src) {
        int value;
        value = (int) ((src[0] & 0xFF) | ((src[1] & 0xFF) << 8) | ((src[2] & 0xFF) << 16));
        return value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
