package gem.app.gss;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import java.io.IOException;

import gem.app.gss.toolbar.LoadingDialog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CODE_CAMERA = 101;
    private static final int CODE_PICK_IMAGE = 102;
    public static final String INPUT_NAME = "inputname";

    EditText inputName;
    Button takePhoto;
    View inputView;
    TextView tvDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputName = findViewById(R.id.inputName);
        inputView = findViewById(R.id.inputView);
        tvDone = findViewById(R.id.tv_done);
        takePhoto = findViewById(R.id.btn_take_photo);

        inputView.setVisibility(View.VISIBLE);
        tvDone.setVisibility(View.GONE);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputName.getText().toString().trim().isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Please input your name");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    InputMethodManager imm = (InputMethodManager) (MainActivity.this).getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputName.getWindowToken(), 0);

                    String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (hasPermissions(MainActivity.this, permission)) {
                        openCameraWithPermission();

                    } else if (needRequestPermissions(MainActivity.this, permission)) {
                        ActivityCompat.requestPermissions(MainActivity.this, permission, CODE_CAMERA);
                    }
                }
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                inputName.setText("");
//                inputView.setVisibility(View.VISIBLE);
//                tvDone.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "request permission result");
            openCameraWithPermission();
        }
    }

    private void openCameraWithPermission() {
        Log.i(TAG, "open camera with permission");
        /*
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        ContentValues values = new ContentValues();
        imgName = inputName.getText().toString().trim() + "_" + System.currentTimeMillis() + ".JPG";
        values.put(MediaStore.Images.Media.TITLE, imgName);
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(cameraIntent, CODE_PICK_IMAGE);
        */
        inputView.setVisibility(View.GONE);
        tvDone.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(INPUT_NAME, inputName.getText().toString().trim());
        startActivity(intent);
    }

    private static boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "has not permission");
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean needRequestPermissions(Activity context, String[] permissions) {
        if (hasPermissions(context, permissions)) {
            Log.i(TAG, "not need permission");
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_PICK_IMAGE) {

            /*
            Log.e(TAG, imgUri.toString());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                Log.e(TAG, "Upload image" + bitmap.getWidth() + "|" + bitmap.getHeight() + "|" + bitmap.getByteCount());

                sProgress = LoadingDialog.show(this, "Uploading...");

                AsyncCallback<BackendlessFile> response = new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(final BackendlessFile backendlessFile) {
                        sProgress.dismiss();
                        inputView.setVisibility(View.GONE);
                        tvDone.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Upload image completed");
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        sProgress.dismiss();
                        Toast.makeText(MainActivity.this, backendlessFault.toString(), Toast.LENGTH_SHORT).show();
                    }
                };

                Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 80, imgName, PATH, response);

            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }

    }
}
