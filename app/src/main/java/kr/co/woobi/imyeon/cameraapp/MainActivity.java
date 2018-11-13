package kr.co.woobi.imyeon.cameraapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSION = 1;

    public static final int CAMERACODE = 10;
    public static final int GALLERY_CODE = 0;
    Button mbuttonCamera, mbuttonSave, mbuttonGallery;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        mbuttonGallery = (Button) findViewById(R.id.buttonGallery);
        mbuttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUpPicture();
            }
        });
        mbuttonSave = (Button) findViewById(R.id.buttonSave);
        mbuttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryAddPic();
            }
        });
        mbuttonCamera = (Button) findViewById(R.id.buttonCamera);
        mbuttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean camera = ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                boolean write = ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (camera && write) {
                    takePicture();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, GALLERY_CODE);
                } else {
                    Toast.makeText(MainActivity.this, "카메라 권한 및 쓰기 권한을 주지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void  pickUpPicture(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_CODE);
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this, "kr.co.woobi.imyeon.cameraapp.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERACODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void requestPermission() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                //권한이 허가가 안됐을 경우 요청할 권한을 모집하느 부분
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            //권한 요청하는 부분
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    private File createImageFile() throws IOException {

        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
       // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        //save a file : path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERACODE) {
            ImageView mimageView = (ImageView) findViewById(R.id.imageView1);
            mimageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
        }
        if(requestCode == GALLERY_CODE){
            Uri uri=data.getData();
            ImageView mimageView2 = (ImageView) findViewById(R.id.imageView2);
            mimageView2.setImageURI(uri);
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
