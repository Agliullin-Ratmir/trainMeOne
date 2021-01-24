package com.example.services;

import com.example.entities.Position;
import com.example.entities.Profile;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParserService {

    private HtmlPage getPageFromInternet(String link) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        return client.getPage(link);
    }

    private Profile createProfile() throws IOException, InterruptedException {
        Profile profile = new Profile();
        profile.setFirstName(getFirstName());
        profile.setLastName(getLastName());
        profile.setCertificates(getCertificates(getCertificatesInHtml()));
        List<Position> positions = getPositionSections();
        profile.setPositions(positions);
        profile.setExperience(getWholeExperience(positions));
        profile.setTitle(getTitle());
        profile.setSkills(getSkills());
        return profile;
    }

    private List<Position> getPositionSections() throws IOException {
        File input = new File("/home/ratmir/IdeaProjects/trainMeOne/src/main/resources/pages/Zhenya2.html");
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements positions = doc.getElementsByClass("full-width ember-view");
        List<Position> positionList = positions.stream()
                .map(item -> {
                    try {
                        return getPositionFromHtml(item.html());
                    } catch (IOException e) {
                        log.error("Can't parse position", e.getMessage());
                    }
                    return new Position();
                }).collect(Collectors.toList());
        return positionList;
    }

    private int convertPositionDuration(String duration) {
        int result = 0;
        String years = parseElement(duration, "(.*)(.+?(?=yr))");
        if (StringUtils.isNotEmpty(years)) {
            result += Integer.valueOf(years.trim());
        }
        result *= 12;
        String months = parseElement(duration, "(?<=yr)(.*)(.+?(?=mos))");
        if (StringUtils.isNotEmpty(months)) {
                result += Integer.valueOf(months.replaceAll("[^\\d+$]", " ").trim());
        } else {
            months = parseElement(duration, "(.*)(.+?(?=mos))");
            if (StringUtils.isNotEmpty(months)) {
                result += Integer.valueOf(months.replaceAll("[^\\d+$]", " ").trim());
            }
        }
        return result;
    }

    private String getAllFile() throws IOException {
        Path fileName = Path.of("/home/ratmir/IdeaProjects/trainMeOne/src/main/resources/pages/Zhenya2.html");
        return Files.readString(fileName);
    }

    private String getFullName() throws IOException {
        String actual = getAllFile();
        Pattern p = Pattern.compile("(?<=<title>)(.*)(.+?(?= | LinkedIn<\\/title>))");
        Matcher m = p.matcher(actual);
        String result = null;
        if (m.find()) {
            result = m.group();
        }
        return result;
    }

    private String getFirstName() throws IOException, InterruptedException {
        String fullName = getFullName();
        if (fullName == null) {
            return "";
        }
        int index = 0;
        if (fullName.startsWith("(")) {
            index = 1;
        }
        return fullName.split(" ")[index];
    }

    private String getLastName() throws IOException, InterruptedException {
        String fullName = getFullName();
        if (fullName == null) {
            return "";
        }
        int index = 1;
        if (fullName.startsWith("(")) {
            index = 2;
        }
        return fullName.split(" ")[index];
    }

    private String getTitle() throws IOException {
        String actual = getAllFile();
        Pattern p = Pattern.compile("(?<=<h3 class=\"t-16 t-black t-bold\">)(.*)(.+?(?=<\\/h3>))");
        Matcher m = p.matcher(actual);
        String result = null;
        if (m.find()) {
            result = m.group();
        }
        return result;
    }

    private String parseElement(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return StringUtils.EMPTY;
    }

    private Position getPositionFromHtml(String positionsHtml) throws IOException {
        Position position = new Position();
        position.setPositionTitle(parseElement(positionsHtml, "(?<=<h3 class=\"t-16 t-black t-bold\">)(.*)(.+?(?=</h3>))"));
        String companyName = parseElement(positionsHtml, "(?<=<p class=\"pv-entity__secondary-title t-14 t-black t-normal\">)(.*)(.+?(?=<!))");
        if (StringUtils.isEmpty(companyName)) {
            companyName = parseElement(positionsHtml, "(?<=<p class=\"pv-entity__secondary-title t-14 t-black t-normal\">)(.*)(.+?(?=<span))");
        }
        position.setCompanyName(companyName);
        position.setDurationInMonths(
                convertPositionDuration(parseElement(positionsHtml, "(?<=<span class=\"pv-entity__bullet-item-v2\">)(.*)(.+?(?=</span>))")));
        return position;
    }

    private String getPositionCompany(String positionsHtml, int index) {
        Pattern pattern = Pattern.compile("(?<=<p class=\"pv-entity__secondary-title t-14 t-black t-normal\"> )(.*)(.+?(?=<!))");
        Matcher matcher = pattern.matcher(positionsHtml);
        if (matcher.find()) {
            return matcher.group(index);
        }
        return StringUtils.EMPTY;
    }

    private String getCertificatesInHtml() throws IOException {
        File input = new File("/home/ratmir/IdeaProjects/trainMeOne/src/main/resources/pages/Zhenya2.html");
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements certificates = doc.getElementsByClass("pv-certifications__summary-info pv-entity__summary-info pv-entity__summary-info--background-section pv-certifications__summary-info--has-extra-details");
        return certificates.html();
    }

    private String getCertificates(String html) {
        Pattern pattern = Pattern.compile("(?<=<h3 class=\"t-16 t-bold\">)(.*)(.+?(?=</h3>))");
        Matcher matcher = pattern.matcher(html);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            result.add(matcher.group().trim());
        }
        return String.join(", ", result);
    }

    private String getSkills() throws IOException {
        File input = new File("/home/ratmir/IdeaProjects/trainMeOne/src/main/resources/pages/Zhenya2.html");
        Document doc = Jsoup.parse(input, "UTF-8");
        Elements certificates = doc.getElementsByClass("pv-skill-category-entity__name-text t-16 t-black t-bold");
        List<String> skills = certificates.stream()
                .map(item -> item.text()).collect(Collectors.toList());
        return String.join(", ", skills);
    }

    private int getWholeExperience(List<Position> positions) {
        return positions.stream().mapToInt(Position::getDurationInMonths)
                .sum();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        String n = "Name: " + getFirstName() + "\n SecondName: " + getSecondName()
//                + "\n Title: " + getTitle() + "\n Company: " + getCompany();
        //System.out.println(getCertificates(getCertificatesInHtml()).toString());
   //     System.out.println(getSkillsInHtml().toString());
        //System.out.println(getPositionSections());
       // System.out.println(getWholeExperience(getPositionSections()));
        ParserService parserService = new ParserService();
        System.out.println(parserService.createProfile().toString());
    }
}
