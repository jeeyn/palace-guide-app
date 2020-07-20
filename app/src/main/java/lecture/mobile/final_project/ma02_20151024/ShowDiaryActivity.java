package lecture.mobile.final_project.ma02_20151024;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.network.storage.ImageUploadResponse;
import com.kakao.util.helper.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ShowDiaryActivity extends AppCompatActivity {
    final static String TAG = "ShowDiaryActivity";

    DiaryDBHelper helper;
    Cursor cursor;

    TextView tvTitle;
    TextView tvDate;
    ImageView ivPhoto;
    TextView tvMemo;

    int id;
    String title;
    String gung;
    String path;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_diary);

        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvDate = (TextView) findViewById(R.id.tvDate);
        ivPhoto = (ImageView)findViewById(R.id.ivPhoto);
        tvMemo = (TextView)findViewById(R.id.tvMemo);

        // DiaryActivity 에서 전달 받은 _id 값을 사용하여 DB 레코드를 가져온 후 View 설정
        Intent intent = getIntent();
        id = intent.getIntExtra("index", 0);

        helper = new DiaryDBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + DiaryDBHelper.TABLE_NAME + " WHERE _id = " + id + ";", null);
        cursor.moveToNext();

        tvTitle.setText(cursor.getString(1));
        gung = cursor.getString(2);
        tvDate.setText(cursor.getString(3));
        path = cursor.getString(4);
        Log.i("imagePathLog", path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ivPhoto.setImageBitmap(bitmap);
        tvMemo.setText(cursor.getString(5));

        helper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                uploadImageToKakao();
                break;
        }

        return true;
    }

    public void shareKakao()
    {
        String address = "";

        switch (gung) {
            case "경복궁":
                address = "서울 종로구 사직로 161 경복궁";
                break;

            case "창덕궁":
                address = "서울 종로구 율곡로 99 창덕궁";
                break;

            case "창경궁":
                address = "서울 종로구 창경궁로 185 창경궁";
                break;

            case "덕수궁":
                address = "서울 중구 세종대로 99 덕수궁";
                break;

            case "종묘":
                address = "서울 종로구 종로 157 종묘";
                break;
        }

        LocationTemplate params = LocationTemplate.newBuilder(address,
                ContentObject.newBuilder(tvTitle.getText().toString() + "\n" + tvDate.getText().toString(),  imageUrl,
                LinkObject.newBuilder()
                        .build())
                        .setDescrption(tvMemo.getText().toString())
                        .build())
                .setAddressTitle(gung)
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                Log.d(TAG, "onSuccess");
            }
        });
    }

    public void uploadImageToKakao() {
        File imageFile = setPic(path);

        KakaoLinkService.getInstance().uploadImage(this, false, imageFile, new ResponseCallback<ImageUploadResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
            }

            @Override
            public void onSuccess(ImageUploadResponse result) {
                Log.d(TAG, result.getOriginal().getUrl());
                Logger.e(result.getOriginal().getUrl());
                imageUrl = result.getOriginal().getUrl();
                shareKakao();
            }
        });
    }

    public File setPic(String path) { // 카카오링크 api에서 서버로 업로드할 수 있는 이미지 용량은 500kb이하로 제한 되므로 용량 조절
        // Get the dimensions of the View
        int targetW = ivPhoto.getWidth();
        int targetH = ivPhoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

        File file = new File(path);
        OutputStream out = null;

        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e)  {
                e.printStackTrace();
            }
        }

        return file;
    }

}
