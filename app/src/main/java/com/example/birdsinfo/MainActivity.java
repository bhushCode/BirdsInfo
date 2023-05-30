package com.example.birdsinfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ScrollCaptureTarget;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.birdsinfo.ml.BirdsModel;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final int CAMERA_REQUEST_CODE =100,GALLERY_REQUEST_CODE=200;
 Button  btLoadImage,btcamera;
 TextView tvResult,tvseachGoogle;
 ImageView ivAddImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btLoadImage = (Button) findViewById(R.id.bt_gallery);
        btcamera = (Button) findViewById(R.id.bt_camera);

      tvResult = findViewById(R.id.tv_result);
      ivAddImage = findViewById(R.id.iv_add_image);
  tvseachGoogle=findViewById(R.id.tv_search_google);
      btLoadImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent intent = new Intent(Intent.ACTION_PICK);
              intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(intent,GALLERY_REQUEST_CODE);
          }
      });
    btcamera.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,CAMERA_REQUEST_CODE);
        }
    });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(resultCode == RESULT_OK)
       {
           if(requestCode==GALLERY_REQUEST_CODE)
           {
               ivAddImage.setImageURI(data.getData());

               try {
                   Bitmap imageBitmap=  UritoBitmap(data.getData());
                   outputGenrator(imageBitmap);
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           }
           if(requestCode==CAMERA_REQUEST_CODE)
           {
               Bitmap img = (Bitmap) (data.getExtras().get("data"));
               ivAddImage.setImageBitmap(img);
               outputGenrator(img);
           }
       }
    }
    private Bitmap UritoBitmap(Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
    }
    public void outputGenrator(Bitmap imagebitmap)
    {
        try {
            BirdsModel model = BirdsModel.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imagebitmap);

            // Runs model inference and gets result.
            BirdsModel.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            // Releases model resources if no longer used.

            int index=0;
            float max=probability.get(0).getScore();
            for(int i = 0;i<probability.size();i++)
            {
                if(max<probability.get(i).getScore())
                {
                    max=probability.get(i).getScore();
                    index=i;
                }
            }
            Category output=probability.get(index);
            tvResult.setText(output.getLabel());
             if(!tvResult.getText().toString().equals("None")) {
                 tvseachGoogle.setText("Search more about " + tvResult.getText().toString());
                 tvseachGoogle.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + tvResult.getText().toString()));
                         startActivity(intent);
                     }
                 });
             }
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }


    }
}