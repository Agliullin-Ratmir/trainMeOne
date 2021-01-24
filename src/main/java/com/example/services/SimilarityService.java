package com.example.services;

import com.example.entities.Profile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;

@Service
public class SimilarityService {

    private static final JaccardSimilarity SIMILARITY = new JaccardSimilarity();
    private static ObjectMapper MAPPER = new ObjectMapper();

    private static double calculateSimilarity(String first, String second, double factor) {
        return factor * SIMILARITY.apply(first, second);
    }

    public static double getSimilarityBetweenProfiles(Profile first, Profile second) {
        return calculateSimilarity(first.getTitle(), second.getTitle(), 0.4) +
                calculateSimilarity(first.getCompany(), second.getCompany(), 0.3) +
                calculateSimilarity(first.getArea(), second.getArea(), 0.6) +
                calculateSimilarity(first.getSkills(), second.getSkills(), 1) +
                calculateSimilarity(first.getCertificates(), second.getCertificates(), 1) +
                getExperienceDiff(first.getExperience(), second.getExperience(), 1);
    }

    private static double getExperienceDiff(int first, int second, double factor) {
        return factor * (second - first);
    }

    public static Collection<Profile> getJson() {
        try(InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("profiles.json")){
            MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            JsonNode jsonNode = MAPPER.readValue(in, JsonNode.class);
            String str = MAPPER.writeValueAsString(jsonNode);
            Collection<Profile> profiles = MAPPER.readValue(str,
                    new TypeReference<Collection<Profile>>() { });
//            String jsonString = MAPPER.writeValueAsString(profiles.get(1));
//            System.out.println(jsonString);
            return profiles;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void getTitleSimilarity() {
        System.out.println(SIMILARITY.apply("developer", "software engineer"));
    }

    public static void main(String[] args) {
        Collection<Profile> list = getJson();
        Profile first = list.stream().findFirst().get();
        for (Profile profile : list) {
            if ("1".equals(profile.getId())) {
                continue;
            }
            double index = getSimilarityBetweenProfiles(first, profile);
            profile.setRating(index);
        }
        list.stream().sorted(Comparator.comparing(Profile::getRating).reversed())
                .forEach(System.out::println);
    }
}
