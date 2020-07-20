package lecture.mobile.final_project.ma02_20151024;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class RouteActivity extends Activity {

    double gungLatitude;
    double gungLongitude;
    double curLatitude;
    double curLongitude;

    boolean flag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Intent intent = getIntent();

        gungLatitude = intent.getDoubleExtra("gungLatitude", 0);
        gungLongitude = intent.getDoubleExtra("gungLongitude", 0);
        flag = intent.getBooleanExtra("flag", false);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 사용자의 현재 위치 확인
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                curLatitude = location.getLatitude(); // 위도
                curLongitude = location.getLongitude(); // 경도

                if (flag == false) {
                    showRoute();
                    flag = true;
                }
            }

            public void onProviderDisabled(String provider) {

            }

            public void onProviderEnabled(String provider) {

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    200, // 위치 변경 사이의 최소 시간 간격
                    1, // 위치 변경 사이의 최소 시간 거리
                    mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void showRoute()
    {
        Intent intent;

        try {
            String url = "daummaps://route?sp=" + String.valueOf(curLatitude) + "," + String.valueOf(curLongitude) + "&ep=" + String.valueOf(gungLatitude) + "," + String.valueOf(gungLongitude) + "&by=PUBLICTRANSIT";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "카카오 맵을 설치한 후 이용해주세요.", Toast.LENGTH_LONG).show();
            String url = "https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
}
