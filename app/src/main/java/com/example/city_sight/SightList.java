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
        Point point3 = new Point(55.7728552, 37.6979420);
        Point point4 = new Point(55.768891159678866, 37.689465570193704);
        Point point5 = new Point(55.77018233096677, 37.690406890581315);
        Point point6 = new Point(55.76502072326742, 37.69115871107883);
        Point point7 = new Point(55.77170599267602, 37.68279056822579);
        Point point8 = new Point(55.76621051027348, 37.68510112583539);
        Sight sight1 = new Sight("Красная площадь", "Красная площадь\n" +
                "Красная площадь - историческая площадь, расположенная в самом сердце Москвы, Россия. " +
                "Она является объектом всемирного наследия ЮНЕСКО и символизирует русскую историю и " +
                "культуру. Площадь окружена иконическими достопримечательностями, такими как Кремль, " +
                "собор Василия Блаженного и Государственный исторический музей. Здесь проходили важные " +
                "события в русской истории, и она продолжает быть популярным местом сбора как для " +
                "местных жителей, так и для туристов.", point1);
        Sight sight2 = new Sight("Эрмитаж", "Музей Государственный Эрмитаж\n" +
                "Эрмитаж - один из самых крупных и престижных художественных музеев в мире. Расположен " +
                "в Санкт-Петербурге, Россия, он представляет собой огромную коллекцию искусства и " +
                "культурных артефактов, включая произведения известных художников, таких как Леонардо " +
                "да Винчи, Рембрандт и Микеланджело. Музей располагается в великолепном Зимнем дворце, " +
                "который был официальной резиденцией российских императоров. Посещение Эрмитажа " +
                "обязательно для любителей искусства и поклонников истории.", point2);
        Sight sight3 = new Sight("СК МГТУ", "Спортивный комплекс МГТУ\n" +
                "МГТУ им Н.Э. Баумана предоставляет своим студентам замечательную возможность" +
                " заниматься спортом. В свободное от занятий студентов время можно приобрести" +
                " разовые посещения или абонементы.", point3);
        Sight sight4 = new Sight("Корпус Э", "Корпус Э\n" +
                "\"Энергомашиностроение\" - уникальный факультет. Ведь только здесь можно встретить за одной партой как специалиста по поршневым моторам, так и разработчика ракетных двигателей твёрдого топлива. Широта охвата направлений подготовки объясняется спецификой центрального, отражённого в названии, понятия для факультета - энергии. Энергия нужна везде. Без неё встанут заводы, остановятся поезда, не взлетят космические корабли. А значит выпускники факультета всегда будут востребованы. Это подтверждается блестящей статистикой трудоустройства, включающей в себя такие легендарные компании как РКК \"Энергия\" и \"Росатом\". Подробнее о каждой кафедре факультета и направлениях подготовки можно узнать из следующего раздела.", point4);
        Sight sight5 = new Sight("Корпус СМ", "Корпус СМ\n" +
                "Факультет СМ - один из крупнейших в МГТУ.\n" +
                "За годы существования факультет выпустил более 30000 инженеров.\n" +
                "Сейчас факультет выпускает специалистов, разрабатывающих технику для всех сред и " +
                "стихий. Это наземная техника (колесные, гусеничные транспортные устройства)," +
                " техника, которая движется в атмосфере, подводная техника и, конечно же," +
                " космическая техника.", point5);
        Sight sight6 = new Sight("Лефортовский парк", "Лефортовский парк\n" +
                "Лефо́ртовский парк (изначально Головинский сад, также известный как Версаль " +
                "на Яузе) — московский исторический и природный памятник архитектуры и" +
                " садово-паркового искусства, построенный в начале XVIII века, прилегающий " +
                "к Екатерининскому дворцу, расположенный в Лефортовском районе. Один из" +
                " старейших парков Москвы", point6);
        Sight sight7 = new Sight("Марсель", "Шаурма Марсель\n" +
                "Шаурма Marseille на Ладожской - это не просто обычная шаурма, а настоящее " +
                "произведение искусства. Она приготовлена из свежих и качественных ингредиентов," +
                " таких как курица, овощи, соусы и специи. Шаурма Marseille обладает неповторимым" +
                " вкусом и ароматом, который никого не оставит равнодушным. Кроме того, она очень" +
                " сытная и питательная, что делает ее идеальным выбором для перекуса или обеда." +
                " Если вы еще не пробовали шаурму Marseille, то обязательно сделайте это! " +
                "Вы не пожалеете!\n" +
                "P.S. Сгенерировано by YaGPT", point7);
        Sight sight8 = new Sight("МГТУ им. Н.Э.Бамана", "ГЗ МГТУ\n" +
                "лавный учебный корпус (ГУК) МГТУ состоит из двух частей.\n" +
                "\n" +
                "Старая часть (также называемая «дворцовой») — Слободской дворец XVIII—XIX веков, перестроенный в современном виде в 1826 году по проекту Доменико Жилярди для «мастерских разных ремёсел» Воспитательного дома. Эта часть обращена фасадом на 2-ю Бауманскую улицу.\n" +
                "\n" +
                "Новая часть (так называемая «циркульная» или «высотная»), имеющая 12 этажей и построенная в 1949-1960 годах по проекту Л. К. Комаровой, обращена фасадом на Лефортовскую набережную Яузы. Её строительство началось с левого крыла, которое носит название «северное». Позже было закончено правое, «южное» крыло.", point8);

        sights.add(sight1);
        sights.add(sight2);
        sights.add(sight3);
        sights.add(sight4);
        sights.add(sight5);
        sights.add(sight6);
        sights.add(sight7);
        sights.add(sight8);
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