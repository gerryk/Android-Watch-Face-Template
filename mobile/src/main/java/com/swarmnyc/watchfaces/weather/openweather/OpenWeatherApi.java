package com.swarmnyc.watchfaces.weather.openweather;


import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarmnyc.watchfaces.R;
import com.swarmnyc.watchfaces.weather.ISimpleWeatherApi;
import com.swarmnyc.watchfaces.weather.WeatherInfo;

import java.net.URL;

public class OpenWeatherApi implements ISimpleWeatherApi {
    private static final String TAG ="OpenWeatherApi";
    private static final String APIURL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=%s&APPID=%s";
    private static final String FAHRENHEIT = "imperial";
    private static final String CELSIUS = "metric";
    private Context context;


    @Override
    public WeatherInfo getCurrentWeatherInfo(double lat, double lon, boolean isFahrenheit) {
        WeatherInfo w = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            String key = context.getResources().getString(R.string.openweather_appid);
            String url = String.format(APIURL, lat, lon, isFahrenheit ? FAHRENHEIT : CELSIUS, key);

            Log.d(TAG,"ApiUrl: "+url);
            OpenWeatherQueryResult result = mapper.readValue(new URL(url), OpenWeatherQueryResult.class);

            if ("200".equals(result.getCod())) {
                w = new WeatherInfo();
                w.setCityName(result.getName());
                w.setIsFahrenheit(isFahrenheit);
                w.setTemperature((int) result.getMain().getTemp());

                OpenWeatherData[] dataArray = result.getWeather();
                if (dataArray != null && dataArray.length > 0) {
                    w.setCondition(ConvertCondition(dataArray[0].getId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return w;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    private String ConvertCondition(String code) {
        switch (code.charAt(0)) {
            case '2':
                return "thunder";
            case '5':
                return "rain";
            case '6':
                return "snow";
            case '8':
                if (code.equals("800")) {
                    return "clear";
                } else {
                    return "cloudy";
                }
            default:
                return "";
        }
    }
}