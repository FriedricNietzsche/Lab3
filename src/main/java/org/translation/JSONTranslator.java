package org.translation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An implementation of the Translator interface which reads in the translation
 * data from a JSON file. The data is read in once each time an instance of this class is constructed.
 */
public class JSONTranslator implements Translator {

    // Map from country code (alpha-3) to list of available language codes
    private final Map<String, List<String>> countryToLanguageCodes = new HashMap<>();
    // Map from country code to (language code -> translated name)
    private final Map<String, Map<String, String>> countryToTranslation = new HashMap<>();

    /**
     * Constructs a JSONTranslator using data from the sample.json resources file.
     */
    public JSONTranslator() {
        this("sample.json");
    }

    /**
     * Constructs a JSONTranslator populated using data from the specified resources file.
     * @param filename the name of the file in resources to load the data from
     * @throws RuntimeException if the resource file can't be loaded properly
     */
    public JSONTranslator(String filename) {
        try {
            String jsonString = Files.readString(
                    Paths.get(getClass().getClassLoader().getResource(filename).toURI())
            );

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String countryCode = obj.getString("alpha3");

                Map<String, String> translations = new HashMap<>();
                List<String> codes = new ArrayList<>();

                // Iterate over all keys, skipping metadata fields
                for (String key : obj.keySet()) {
                    if ("id".equals(key) || "alpha2".equals(key) || "alpha3".equals(key)) {
                        continue;
                    }
                    String translatedName = obj.getString(key);
                    translations.put(key, translatedName);
                    codes.add(key);
                }

                countryToTranslation.put(countryCode, translations);
                countryToLanguageCodes.put(countryCode, codes);
            }
        }
        catch (IOException | URISyntaxException ex) {
            throw new RuntimeException("Failed to load translation data from " + filename, ex);
        }
    }

    @Override
    public List<String> getCountryLanguages(String country) {
        List<String> codes = countryToLanguageCodes.get(country);
        return codes != null ? new ArrayList<>(codes) : Collections.emptyList();
    }

    @Override
    public List<String> getCountries() {
        return new ArrayList<>(countryToLanguageCodes.keySet());
    }

    @Override
    public String translate(String country, String language) {
        Map<String, String> translations = countryToTranslation.get(country);
        if (translations != null) {
            return translations.get(language);
        }
        return null;
    }
}

