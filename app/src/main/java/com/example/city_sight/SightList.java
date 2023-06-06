package com.example.city_sight;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.city_sight.sight.Sight;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;

import java.util.ArrayList;

public class SightList extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    ArrayList<Sight> sights = toCreateSights();
    ArrayList<String> titles = toCreateSights(sights);

    private ListView sightList;
    CheckBox locationCheckbox;
    EditText radiusEditText;
    Button applyButton;
    User user;
    double radius = Double.MAX_VALUE;
    LocationManager locationManager;

    Double latitude;
    Double longitude;
    TextView textView;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey("3e9ed211-3558-476a-ab52-9b29735e3a9e");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_sight_list);
        locationCheckbox = findViewById(R.id.locationCheckbox);
        locationManager = MapKitFactory.getInstance().createLocationManager();

        Bundle arguments = getIntent().getExtras();

        if (arguments != null) {
            user = arguments.getParcelable(User.class.getSimpleName());
        }

        textView = (TextView) findViewById(R.id.helloUser);
        textView.setText("Здравствуйте, ".concat(user.getName()).concat("!"));

        sightList = findViewById(R.id.sightList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_list_item, R.id.titleTextView, titles);
        sightList.setAdapter(adapter);

        sightList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapAct(sights.get(position));
            }
        });

        applyButton = findViewById(R.id.applyButton);
        radiusEditText = findViewById(R.id.radiusEditText);
        locationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSights(isChecked);
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSights(locationCheckbox.isChecked());
            }
        });
    }

    private void updateSights(boolean isChecked) {
        if (isChecked) {
            String radiusText = radiusEditText.getText().toString();
            if (!radiusText.equals("")) {
                radius = Double.parseDouble(radiusText);
            }
            requestLocationPermission();
            locationManager = MapKitFactory.getInstance().createLocationManager();
            // Request location updates
            locationManager.subscribeForLocationUpdates(60000, 50, 0, true, FilteringMode.ON, new LocationListener() {

                        @Override
                        public void onLocationUpdated(@NonNull com.yandex.mapkit.location.Location location) {
                            latitude = location.getPosition().getLatitude();
                            longitude = location.getPosition().getLongitude();
                            displaySightsInRadius(latitude, longitude, radius);
                        }

                        @Override
                        public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
                        }
                    }
            );
        } else {
            displayAllSights();
        }
    }

    private void displayAllSights() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, R.id.titleTextView, titles);
        sightList.setAdapter(adapter);
        sightList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapAct(sights.get(position));
            }
        });
    }

    private void displaySightsInRadius(double latitude, double longitude, double radius) {
        ArrayList<String> filteredTitles = new ArrayList<>();
        ArrayList<Sight> filteredSights = new ArrayList<>();
        for (Sight sight : sights) {
            double distance = calculateDistance(latitude, longitude, sight.getCoordinates().getLatitude(), sight.getCoordinates().getLongitude());
            if (distance <= radius) {
                filteredTitles.add(sight.getTitle());
                filteredSights.add(sight);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_list_item, R.id.titleTextView, filteredTitles);
        sightList.setAdapter(adapter);

        sightList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapAct(filteredSights.get(position));
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Расчет расстояния между двумя координатами (широта, долгота) в метрах
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000; // Конвертируем в километры и затем в метры
        return dist;
    }


    public void mapAct(Sight sight) {
        Intent intent = new Intent(this, SightMap.class); // intent для перехода к активности sightMap
        intent.putExtra("title", sight.getTitle());
        intent.putExtra("discovery", sight.getDiscovery());
        intent.putExtra("latitude", sight.getCoordinates().getLatitude());
        intent.putExtra("longitude", sight.getCoordinates().getLongitude());
        startActivity(intent);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                "android.permission.ACCESS_FINE_LOCATION")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        }
    }

    public ArrayList<Sight> toCreateSights() {
        ArrayList<Sight> sights = new ArrayList<>();
        Point point1 = new Point(55.7539, 37.6208);
        Point point2 = new Point(59.9398, 30.3146);
        Point point3 = new Point(53.5587, 108.1650);
        Point point4 = new Point(59.8888, 29.8304);
        Point point5 = new Point(48.6921, 44.4946);
        Point point6 = new Point(55.752480, 37.719401);
        Sight sight1 = new Sight("Красная площадь", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        Sight sight10 = new Sight("legenda", "HOME\n" +
                "BEST PLACE IN THE WORLD", point6);
        Sight sight2 = new Sight("Эрмитаж", "Музей Государственный Эрмитаж\n" +
                "Эрмитаж - один из самых крупных и престижных художественных музеев в мире. Расположен " +
                "в Санкт-Петербурге, Россия, он представляет собой огромную коллекцию искусства и " +
                "культурных артефактов, включая произведения известных художников, таких как Леонардо " +
                "да Винчи, Рембрандт и Микеланджело. Музей располагается в великолепном Зимнем дворце, " +
                "который был официальной резиденцией российских императоров. Посещение Эрмитажа " +
                "обязательно для любителей искусства и поклонников истории.", point2);
        Sight sight3 = new Sight("Байкал", "Озеро Байкал - самое глубокое и " +
                "древнее пресноводное озеро в мире, расположенное в Сибири, Россия. Оно также считается " +
                "одним из самых чистых и красивых озер в мире, известное своим уникальным " +
                "биоразнообразием. Окруженное величественными горами, лесами и очаровательными деревнями, " +
                "озеро Байкал предлагает захватывающие пейзажи и множество возможностей для активного отдыха, " +
                "включая пешие прогулки, рыбалку и катание на коньках зимой.", point3);
        Sight sight4 = new Sight("Петергоф", "Петергоф - знаменитый дворцово-парковый " +
                "ансамбль, расположенный в Санкт-Петербурге, Россия. Он славится своими великолепными " +
                "фонтанами и разнообразными дворцами, окруженными прекрасными садами. Петергоф является " +
                "национальным достоянием России и привлекает туристов своей архитектурной красотой и " +
                "уникальными ландшафтами. Посещение Петергофа позволит вам окунуться в атмосферу роскоши " +
                "и истории русской империи.", point4);
        Sight sight5 = new Sight("Царицинская крепость", "Царицынская крепость - исторический " +
                "комплекс в городе Волгограде, Россия. Она является одной из самых значимых оборонительных " +
                "систем XVIII-XIX веков и символом города. Крепость была создана для защиты границ " +
                "Российской империи и была важным оплотом в различных конфликтах. Сегодня Царицынская " +
                "крепость является популярным туристическим объектом, где посетители могут узнать о ее " +
                "истории и насладиться живописными видами Волги.", point5);
        Sight sight6 = new Sight("sfdsd dsfsdf", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        Sight sight7 = new Sight("Красная dsfsdd", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        Sight sight8 = new Sight(",ffd площадь", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        Sight sight9 = new Sight(",", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        sights.add(sight1);
        sights.add(sight2);
        sights.add(sight3);
        sights.add(sight4);
        sights.add(sight5);
        sights.add(sight6);
        sights.add(sight7);
        sights.add(sight8);
        sights.add(sight9);
        sights.add(sight10);
        return sights;
    }

    public ArrayList<String> toCreateSights(ArrayList<Sight> sights) {
        ArrayList<String> titles = new ArrayList<>();
        for (Sight sight : sights) {
            titles.add(sight.getTitle());
        }
        return titles;
    }
}