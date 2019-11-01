package com.RuoyuNiu.frogDetector;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "YOLODetect";

    public static final int CHOOSE_PHOTO = 2;
    public static final int TAKE_PHOTO = 1;

    private ImageView ivPhoto;
    private List<Style> styleList = new ArrayList<Style>();
    private Uri photoURI;

    private String INPUT_NODE = "input";
    private String OUTPUT_NODE = "output";

    private int[] intValues;
    private float[] floatValues;

    private int width = 512;
    private int height = 512;

    private Bitmap chosen_bitmap = null;
    private Bitmap final_styled_bitmap = null;

    private TensorFlowInferenceInterface inferenceInterface;

    private String model_file;
    private int style_pos = 0;

    /***************YOLO***************/
    // Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
    // must be manually placed in the assets/ directory by the user.
    // Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
    // DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
    // ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise
    private static final String YOLO_MODEL_FILE = "file:///android_asset/tiny-yolo-voc-frog.pb";
    private static final int YOLO_INPUT_SIZE = 416;
    private static final String YOLO_INPUT_NAME = "input";
    private static final String YOLO_OUTPUT_NAMES = "output";
    private static final int YOLO_BLOCK_SIZE = 32;
    
    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
    // or YOLO.
    private Classifier detector;
    int cropSize = 416;
    /******************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init image view and button
        ivPhoto = findViewById(R.id.imageview);
        FloatingActionButton onCamera = findViewById(R.id.camera);
        FloatingActionButton onPhoto = findViewById(R.id.photo);
        FloatingActionButton download = findViewById(R.id.download);

        // init style list
        initStyles();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        StyleAdapter adapter = new StyleAdapter(styleList);
        recyclerView.setAdapter(adapter);

        // callback function when choosing the style image
        adapter.setOnItemClickListener(new StyleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "Start YOLO Detect...", Toast.LENGTH_SHORT).show();

                /**********YOLO************/
                detector =  TensorFlowYoloDetector.create(
                        getAssets(),
                        YOLO_MODEL_FILE,
                        YOLO_INPUT_SIZE,
                        YOLO_INPUT_NAME,
                        YOLO_OUTPUT_NAMES,
                        YOLO_BLOCK_SIZE);
                cropSize = YOLO_INPUT_SIZE;

                StylizeTask stylizeTask = new StylizeTask();
                stylizeTask.execute(style_pos);
            }
        });

        // callback function when clicking then camera icon
        onCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Taking Photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT < 24) {
                    photoURI = Uri.fromFile(outputImage);
                } else {
                    photoURI = FileProvider.getUriForFile(MainActivity.this, "com.shaofengzou.styletransfer.fileprovider", outputImage);
                }
                Log.e(TAG, "prepare start camera");
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        // callback function when clicking then photo icon
        onPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Selecting Photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                try {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        openAlbum();
                    }
                } catch (Exception e) {
                }
            }
        });

        // callback function when clicking then download icon
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saving Photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                if (Build.VERSION.SDK_INT >= 23) {

                    int hasReadContactsPermission = MainActivity.this.checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {

                        MainActivity.this.requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                1);

                        return;
                    }
                    if (final_styled_bitmap == null) {
                        System.out.println("final_photo == null");
                    }
                    saveBitmap(final_styled_bitmap);

                } else {
                    saveBitmap(final_styled_bitmap);
                }

            }
        });


    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); 
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap The bitmap to save.
     */
    public static void saveBitmap(final Bitmap bitmap) {
        String filename;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        filename = timeStamp + ".jpg";

        final String root =
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "style_transfer";
        Log.e(TAG, "Saving bitmap to " + root);
        final File myDir = new File(root);

        if (!myDir.mkdirs()) {
            Log.e(TAG, "Make dir failed");
        }

        final String fname = filename;
        final File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Init tensorflow interface and load model
     */
    private void initTensorFlowAndLoadModel() {
        intValues = new int[height * width];
        floatValues = new float[height * width * 3];
        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), model_file);
    }

    /**
     * Rescale the bitmap to desired size
     *
     * @param origin
     * @param newWidth
     * @param newHeight
     * @return
     */
    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        return newBM;
    }

    /**
     * Do operation of style transfer in background
     */
    class StylizeTask extends AsyncTask<Integer, Void, Bitmap> {
        private StylizeTask() {
        }

        // Add some notification events
        @Override
        protected void onPreExecute() {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "Starting YOLO Detect", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        // Do some calculation
        @Override
        protected Bitmap doInBackground(Integer... params) {
            if (chosen_bitmap != null) {
                int target_width = chosen_bitmap.getWidth();
                int target_height = chosen_bitmap.getHeight();
                Log.e(TAG, "Stage 1, width:" + String.valueOf(width));
                Log.e(TAG, "Stage 1, height:" + String.valueOf(height));

                /*************************************************/
                Bitmap yoloBitmap = Bitmap.createBitmap(chosen_bitmap);
                Bitmap croppedBitmap = scaleBitmap(yoloBitmap, cropSize, cropSize); // desiredSize
                final Canvas canvas = new Canvas(croppedBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setTextSize(20);
                paint.setStrokeWidth(2.0f);
                paint.setFakeBoldText(false);
                float minimumConfidence=0.2f;;


                Log.e(TAG, "Start YOLO detect");

                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        canvas.drawRect(location, paint);
                        paint.setStrokeWidth(0);
                        paint.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawText(result.getTitle()+" "+ String.valueOf(result.getConfidence()), location.left, location.top, paint);
                        //setTextSizeForWidth(paint, 100, result.getTitle()+" "+ String.valueOf(result.getConfidence()));
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(2.0f);
                    }
                }
                Bitmap scaledBitmap_yolo = scaleBitmap(croppedBitmap, target_width, target_height); // desiredSize
                /*************************************************/
                return scaledBitmap_yolo;
            } else {
                return chosen_bitmap;
            }
        }

        // Represent the result
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            final_styled_bitmap = bitmap;
            ivPhoto.setImageBitmap(bitmap);
        }
    }

    /**
     * Stylize the image using tensorflow model trained before.
     *
     * @param bitmap image to be styled
     * @return styled image
     */
    private Bitmap stylizeImage(Bitmap bitmap) {
        // Rescale image to fixed image size
        Bitmap scaledBitmap = scaleBitmap(bitmap, width, height);
        // Get image data from bitmap
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());

        // Turn to 8bit format
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) * 1.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) * 1.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) * 1.0f;
        }

        // Different pb file require different input tensor size
        // The model trained by myself such as wave_my.pb require 1*512*512*3 input format
        if (style_pos > 8) {
            // Copy the input data into TensorFlow
            inferenceInterface.feed(INPUT_NODE, floatValues, 1, height, width, 3);
        } else {
            // Copy the input data into TensorFlow
            inferenceInterface.feed(INPUT_NODE, floatValues, height, width, 3);
        }
        // Run the inference call.
        inferenceInterface.run(new String[]{OUTPUT_NODE});
        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);

        // Normalize the output before turn into integer, otherwise it will have some fake pixels.
        float r_max = 0, g_max = 0, b_max = 0, r_min = 999, g_min = 999, b_min = 999;
        for (int i = 0; i < intValues.length; ++i) {
            if (floatValues[i * 3] > r_max) {
                r_max = floatValues[i * 3];
            }
            if (floatValues[i * 3] < r_min) {
                r_min = floatValues[i * 3];
            }
            if (floatValues[i * 3 + 1] > g_max) {
                g_max = floatValues[i * 3 + 1];
            }
            if (floatValues[i * 3 + 1] < g_min) {
                g_min = floatValues[i * 3 + 1];
            }
            if (floatValues[i * 3 + 2] > b_max) {
                b_max = floatValues[i * 3 + 2];
            }
            if (floatValues[i * 3 + 2] < b_min) {
                b_min = floatValues[i * 3 + 2];
            }
        }

        for (int i = 0; i < intValues.length; ++i) {
            floatValues[i * 3] = (floatValues[i * 3] - r_min) / (r_max - r_min) * 255;
            floatValues[i * 3 + 1] = (floatValues[i * 3 + 1] - g_min) / (g_max - g_min) * 255;
            floatValues[i * 3 + 2] = (floatValues[i * 3 + 2] - b_min) / (b_max - b_min) * 255;
        }

        // Convert float type to Integer type
        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] = 0xFF000000
                    | (((int) (floatValues[i * 3])) << 16)
                    | (((int) (floatValues[i * 3 + 1])) << 8)
                    | ((int) (floatValues[i * 3 + 2]));
        }

        // Generate styled bitmap
        scaledBitmap.setPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());

        return scaledBitmap;
    }

    /**
     * This is response function of click event for taking photo or choosing image from album
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
                try {
                    chosen_bitmap = handleSamplingAndRotationBitmap(MainActivity.this, photoURI);
                    ivPhoto.setImageBitmap(chosen_bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CHOOSE_PHOTO) {
                if (Build.VERSION.SDK_INT >= 19) {
                    handleImageOnKitKat(data);
                } else {
                    handleImageBeforeKitKat(data);
                }
            }
        }
    }

    /**
     * Get image uri from intent of choosing photo for android system lower then 4.4
     *
     * @param data
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; 
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                //Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                //imagePath = getImagePath(contentUri, null);
                imagePath = docId;
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        if (imagePath != null) {
            try {
                chosen_bitmap = handleSamplingAndRotationBitmap(MainActivity.this, uri);
                ivPhoto.setImageBitmap(chosen_bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Get image uri from intent of choosing photo for android system higher then 4.4
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        if (imagePath != null) {
            try {
                chosen_bitmap = handleSamplingAndRotationBitmap(MainActivity.this, uri);
                ivPhoto.setImageBitmap(chosen_bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is responsible for verify the image path from usi and selection
     *
     * @param uri       Uri from photo album or camera
     * @param selection
     * @return Image path
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 480;
        int MAX_WIDTH = 640;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }


    private void initStyles() {
        Style frog = new Style("frog", R.drawable.start);
        styleList.add(frog);

    }
}
