package lecture.mobile.final_project.ma02_20151024;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lvGungName;
    ArrayAdapter<String> adapter;
    ArrayList<String> gungName;

    Button btnPlay;
    Button btnStop;
    TextView tvPlay;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        popUpIntroduction();

        lvGungName = (ListView) findViewById(R.id.lvGungName);

        gungName = new ArrayList<String>();
        gungName.add("경복궁");
        gungName.add("창덕궁");
        gungName.add("창경궁");
        gungName.add("덕수궁");
        gungName.add("종묘");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gungName);
        lvGungName.setAdapter(adapter);

        lvGungName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) { // 리스트 항목 클릭 시 궁궐 별 화면으로 이동
                Intent intent = new Intent(MainActivity.this, GungActivity.class);
                intent.putExtra("index", pos);
                intent.putExtra("gungName", gungName.get(pos).toString());
                startActivity(intent);
            }
        });

        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        tvPlay = findViewById(R.id.tvPlay);

        // MediaPlayer 객체 할당
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gung);
        mediaPlayer.setLooping(true); // 반복재생
        mediaPlayer.start();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.icon_play);
                    tvPlay.setText("P L A Y");

                    mediaPlayer.pause(); // 일시중지
                } else {
                    btnPlay.setBackgroundResource(R.drawable.icon_pause);
                    tvPlay.setText("P A U S E");

                    mediaPlayer.start();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                try {
                    btnPlay.setBackgroundResource(R.drawable.icon_play);
                    tvPlay.setText("P L A Y");

                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // kakao api 사용을 위한 hash 값 체크
        try {
            PackageInfo info = getPackageManager().getPackageInfo("lecture.mobile.final_project.ma02_20151024", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // MediaPlayer 는 시스템 리소스를 많이 차지함
    // 따라서 필요 이상으로 사용하지 않도록 주의
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // MediaPlayer 해지
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void popUpIntroduction() {
        Intent intent = new Intent(this, PopUpActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_diary_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.diary:
                intent = new Intent(MainActivity.this, DiaryFromHomeActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }



}
