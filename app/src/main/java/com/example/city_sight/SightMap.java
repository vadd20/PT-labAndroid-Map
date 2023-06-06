package com.example.city_sight;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.city_sight.sight.Sight;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

public class SightMap extends AppCompatActivity {
    MapView mapview;
    TextView textView;
    TextView textView2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_map);
        Bundle arguments = getIntent().getExtras();
        Sight sight = new Sight(arguments.getString("title"), arguments.getString("discovery"),
                new Point((Double) arguments.get("latitude"), (Double) arguments.get("longitude")));
        mapview = (MapView)findViewById(R.id.mapview);
        mapview.getMap().move(
                new CameraPosition(sight.getCoordinates(), 16.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);

        mapview.getMap().getMapObjects().addPlacemark(sight.getCoordinates());

        textView = (TextView) findViewById(R.id.mapTitle);
        textView.setText((sight.getTitle()));
        textView2 = (TextView) findViewById(R.id.discTitle);
        textView2.setText((sight.getDiscovery()));
    }

    @Override
    protected void onStop() {
        mapview.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapview.onStart();
    }
}
