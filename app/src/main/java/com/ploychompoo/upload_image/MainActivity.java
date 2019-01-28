package com.ploychompoo.upload_image;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.ploychompoo.upload_image.API.ApiClient;
import com.ploychompoo.upload_image.API.ApiService;
import com.ploychompoo.upload_image.Utils.ProgressRequestBody;
import com.ploychompoo.upload_image.Utils.UploadCallBacks;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity implements UploadCallBacks {

    private static final int REQUEST_PERMISSION = 1000;
    private static final int PICK_FILE_REQUEST = 1001;

    private ApiService apiService;

    private Button btnUpload;
    private ImageView imgView;
    private Uri selectedFileUri;
    private ProgressDialog progressDialog;

    private ApiService getApiUpload() {
        return ApiClient.getClient().create(ApiService.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check Permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        }

        // Service
        apiService = getApiUpload();
        // Init view
        btnUpload = (Button) findViewById(R.id.btn_upload);
        imgView = (ImageView) findViewById(R.id.img_view);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
    }

    private void uploadFile() {
        if(selectedFileUri != null) {

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Uploading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();


            File file = FileUtils.getFile(this, selectedFileUri);
            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);
            final RequestBody desc = RequestBody.create(okhttp3.MultipartBody.FORM, "HelloWorld");
            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
            final Call<String> call = apiService.upload(body, desc);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    call.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                }
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == PICK_FILE_REQUEST) {
                if(data != null) {
                    selectedFileUri = data.getData();
                    if(selectedFileUri != null && !selectedFileUri.getPath().isEmpty())
                       imgView.setImageURI(selectedFileUri);
                    else
                        Toast.makeText(this, "Can't Upload File", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void chooseFile() {
        Intent intent = Intent.createChooser(FileUtils.createGetContentIntent(), "Select a file");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }


    @Override
    public void onProgressUpdate(int percentage) {
        progressDialog.setProgress(percentage);
    }
}
