package gem.app.gss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import gem.app.gss.toolbar.LoadingDialog;

import static gem.app.gss.MainActivity.INPUT_NAME;

public class CameraActivity extends Activity implements Callback, OnClickListener {
    private static final String TAG = "MainActivity";
    public static final String APPLICATION_ID = "F7061CD0-9851-0DC3-FFD4-755525AF1300";
    public static final String API_KEY = "052E0B94-0D73-1D5F-FF45-E0B1D74EEE00";
    public static final String SERVER_URL = "https://api.backendless.com";
    public static final String PATH = "mypics";

    LoadingDialog sProgress;
    String inputName;
    String imgName;
    Uri imgUri;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private ImageView btnTakePhoto;
    private ImageView btnFlipCamera;
    private TextView tvUpload;
    private TextView tvCancel;
    private TextView tvImgNum;
    private int cameraId;
    private int rotation;
    private int takeNum = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_view);
        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(), APPLICATION_ID, API_KEY);

        Intent intent = getIntent();
        inputName = intent.getStringExtra(INPUT_NAME);
        // camera surface view created

        btnTakePhoto = findViewById(R.id.capture_image);
        btnFlipCamera = findViewById(R.id.flipCamera);
        tvUpload = findViewById(R.id.btn_upload);
        tvCancel = findViewById(R.id.btn_cancel);
        tvImgNum = findViewById(R.id.img_num);
        surfaceView = findViewById(R.id.surfaceView);
        tvImgNum.setText((takeNum > 1) ? getString(R.string.take_imgs, String.valueOf(takeNum)) : getString(R.string.take_img, String.valueOf(takeNum)));
        btnTakePhoto.setVisibility(View.VISIBLE);
        tvImgNum.setVisibility(View.VISIBLE);
        tvCancel.setVisibility(View.GONE);
        tvUpload.setVisibility(View.GONE);

        if (Camera.getNumberOfCameras() > 1) {
            btnFlipCamera.setVisibility(View.VISIBLE);
            cameraId = CameraInfo.CAMERA_FACING_FRONT;

        } else {
            cameraId = CameraInfo.CAMERA_FACING_BACK;
        }

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        btnTakePhoto.setOnClickListener(this);
        btnFlipCamera.setOnClickListener(this);
        tvUpload.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!openCamera(CameraInfo.CAMERA_FACING_BACK)) {
            alertCameraDialog();
        }

    }

    private boolean openCamera(int id) {
        boolean result = false;
        cameraId = id;
        releaseCamera();
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (camera != null) {
            try {
                setUpCamera(camera);
                camera.setErrorCallback(new ErrorCallback() {

                    @Override
                    public void onError(int error, Camera camera) {

                    }
                });
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
                releaseCamera();
            }
        }
        return result;
    }

    private void setUpCamera(Camera c) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;

            default:
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // frontFacing
            rotation = (info.orientation + degree) % 330;
            rotation = (360 - rotation) % 360;
        } else {
            // Back-facing
            rotation = (info.orientation - degree + 360) % 360;
        }
        c.setDisplayOrientation(rotation);
        Parameters params = c.getParameters();

        List<String> focusModes = params.getSupportedFlashModes();
        if (focusModes != null) {
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }

        params.setRotation(rotation);
    }

    private void releaseCamera() {
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.setErrorCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error", e.toString());
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture_image:
                takeImage();
                break;

            case R.id.flipCamera:
                flipCamera();
                refreshCamera();
                break;

            case R.id.btn_upload:
                uploadPhoto();
                break;

            case R.id.btn_cancel:
                refreshCamera();
                break;

            default:
                break;
        }
    }

    private void refreshCamera() {
        btnTakePhoto.setVisibility(View.VISIBLE);
        tvCancel.setVisibility(View.GONE);
        tvUpload.setVisibility(View.GONE);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadPhoto() {
        Log.e(TAG, imgUri.toString());
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
            Log.e(TAG, "Upload image" + bitmap.getWidth() + "|" + bitmap.getHeight() + "|" + bitmap.getByteCount());

            sProgress = LoadingDialog.show(this, "Uploading...");

            AsyncCallback<BackendlessFile> response = new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(final BackendlessFile backendlessFile) {
                    sProgress.dismiss();
                    takeNum--;
                    tvImgNum.setText((takeNum > 1) ? getString(R.string.take_imgs, String.valueOf(takeNum)) : getString(R.string.take_img, String.valueOf(takeNum)));

                    if (takeNum >= 1) {
                        refreshCamera();

                    } else {
                        tvImgNum.setVisibility(View.GONE);
                        finish();
                    }
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    sProgress.dismiss();
                    refreshCamera();
                    Toast.makeText(CameraActivity.this, backendlessFault.toString(), Toast.LENGTH_SHORT).show();
                }
            };

            Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 85, imgName, PATH, response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void takeImage() {
        camera.takePicture(null, null, new PictureCallback() {

            private File imageFile;

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                btnTakePhoto.setVisibility(View.GONE);
                tvCancel.setVisibility(View.VISIBLE);
                tvUpload.setVisibility(View.VISIBLE);

                try {
                    // convert byte array into bitmap
                    Bitmap loadedImage = null;
                    Bitmap rotatedBitmap = null;
                    loadedImage = BitmapFactory.decodeByteArray(data, 0, data.length);

                    // rotate Image
                    Matrix rotateMatrix = new Matrix();
                    if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
                        rotateMatrix.postRotate(90);

                    } else {
                        rotateMatrix.postRotate(rotation);
                    }
                    rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), rotateMatrix, false);
                    String state = Environment.getExternalStorageState();
                    File folder = null;
                    if (state.contains(Environment.MEDIA_MOUNTED)) {
                        folder = new File(Environment.getExternalStorageDirectory() + "/Demo");
                    } else {
                        folder = new File(Environment.getExternalStorageDirectory() + "/Demo");
                    }

                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        imgName = inputName + "_" + System.currentTimeMillis() + ".JPG";
                        imageFile = new File(folder.getAbsolutePath()
                                + File.separator
                                + imgName);

                        imageFile.createNewFile();
                    } else {
                        Toast.makeText(getBaseContext(), "Image Not saved",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                    // save image into gallery
                    rotatedBitmap.compress(CompressFormat.JPEG, 100, ostream);

                    FileOutputStream fout = new FileOutputStream(imageFile);
                    fout.write(ostream.toByteArray());
                    fout.close();
                    ContentValues values = new ContentValues();

                    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, imageFile.getAbsolutePath());

                    imgUri = Uri.fromFile(imageFile);
                    CameraActivity.this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void flipCamera() {
        int id = (cameraId == CameraInfo.CAMERA_FACING_BACK ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK);
        if (!openCamera(id)) {
            alertCameraDialog();
        }
    }

    private void alertCameraDialog() {
        AlertDialog.Builder dialog = createAlert(CameraActivity.this, "Camera info", "error to open camera");
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private Builder createAlert(Context context, String title, String message) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
        if (title != null)
            dialog.setTitle(title);
        else
            dialog.setTitle("Information");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;

    }
}