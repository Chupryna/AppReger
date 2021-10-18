package sample.manager;

import sample.data.model.Country;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountryManager {

    private static final String COUNTRIES = "resources/countries.txt";
    private static volatile CountryManager instance = null;
    private volatile List<Country> countryList;

    private CountryManager() {
    }

    public static CountryManager getInstance() {
        CountryManager localInstance = instance;
        if (localInstance == null) {
            synchronized (CountryManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CountryManager();
                }
            }
        }

        return localInstance;
    }

    public void init() {
        countryList = new ArrayList<>();

        ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
        threadExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(COUNTRIES),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = line.split(":");
                    if (strings.length < 4) {
                        continue;
                    }

                    String[] operators;
                    if (strings[2].isEmpty()) {
                        operators = new String[0];
                    } else {
                        operators = strings[2].split(",");
                    }
                    countryList.add(new Country(strings[0], strings[1], strings[3], Arrays.asList(operators)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized Country getCountryByApiCode(String apiCode) {
        return countryList.stream()
                .filter(country -> country.getAPICode().equals(apiCode))
                .findFirst()
                .orElse(Country.EMPTY_COUNTRY());
    }
}
