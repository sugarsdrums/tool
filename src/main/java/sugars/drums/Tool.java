package sugars.drums;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Tool {

    private static final LocalDate TARGET_DATE = LocalDate.of(2024, 12, 1);
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("yyMM");

    private static final String YEAR_MONTH = TARGET_DATE.format(SHORT);
    private static final String HTML_FILE_PATH = String.format("files/schedule20%s.html", YEAR_MONTH);
    private static final String MOBILE_FILE_PATH = String.format("files/info%s.html", YEAR_MONTH);
    private static final String HTML_TITLE = TARGET_DATE.format(DateTimeFormatter.ofPattern("yyyy年M月"));
    private static final String MOBILE_BEFORE = TARGET_DATE.minusDays(1).format(SHORT);
    private static final String MOBILE_AFTER = TARGET_DATE.plusMonths(1).format(SHORT);

    public static void main(String[] args) throws IOException {


        StringBuilder htmlBuilder = new StringBuilder();
        StringBuilder mobileBuilder = new StringBuilder();
        List<String> allLines = readAllLinesFromInputTxt();
        int index = 0;
        while (index < allLines.size()) {
            String line = allLines.get(index);
            if (!line.matches("^1?[0-9]/[1-3]?[0-9].*")) {
                index++;
                continue;
            }
            Schedule schedule = new Schedule();

            // 日付・バンド名・タイトル
            schedule.date = line.substring(0, line.indexOf(')') + 1);
            line = sub(line, schedule.date);
            schedule.band = next(line, "『", "  ");
            schedule.title = sub(line, schedule.band);
            index++;

            // メンバー
            schedule.members = allLines.get(index);
            if (schedule.members.startsWith("配信")) {
                schedule.delivery = " <span style=\"font-weight:bold; color:white; background-color:blue;\">配信</span>";
                schedule.members = schedule.members.substring(2).trim();
            }
            index++;
            while (!allLines.get(index).startsWith("●")) {
                schedule.members = schedule.members + "<br/>" + allLines.get(index);
                index++;
            }

            // 場所・時刻・料金
            line = allLines.get(index).substring(1);
            if (line.matches("^札幌 +くう.*")) {
                line = line.replaceFirst("札幌 +くう", "札幌 くう");
            }
            schedule.place = next(line, "**separator**", "open", "start", "1st", "詳細", "時間未定", "時間料金未定", "時間・料金未定", "  ");
            line = sub(line, schedule.place).replace("**separator**", "");
            schedule.time = next(line, "##separator##", "一般", "大人", "前売", "予約", "\\", "￥", "料金", "Free", "    ");
            schedule.fee = sub(line, schedule.time).replace("##separator##", "");
            index++;

            // 詳細
            schedule.details = allLines.get(index);
            index++;
            while (index < allLines.size() && !allLines.get(index).isEmpty()) {
                schedule.details = schedule.details + "<br/>" + allLines.get(index);
                index++;
            }

            schedule.band = schedule.band.replace(Const.LATTE, Const.LATTE_LINK);
            String html = String.format(Const.TEMPLATE,
                            schedule.date + schedule.delivery, schedule.band, schedule.place,
                            schedule.title,
                            schedule.time, schedule.fee,
                            schedule.members,
                            schedule.details)
                    .replace("      <div class=\"live-title\"></div>\n", "");
            htmlBuilder.append(html);

            String mobile = div(schedule.date + (schedule.delivery.isEmpty() ? "" : " 配信")) +
                    div(schedule.band + " " + schedule.title) +
                    div(schedule.place) +
                    div(schedule.time) +
                    div(schedule.fee) + "<br>\n";
            mobileBuilder.append(mobile);
        }

        try (BufferedWriter htmlWriter = Files.newBufferedWriter(
                Path.of(HTML_FILE_PATH), StandardCharsets.UTF_8);
             BufferedWriter mobileWriter = Files.newBufferedWriter(
                     Path.of(MOBILE_FILE_PATH), StandardCharsets.UTF_8)) {

            String htmlTemplate = load("/schedule.html");
            htmlWriter.write(String.format(htmlTemplate, HTML_TITLE, htmlBuilder));

            String mobileTemplate = load("/mobile.html");
            mobileWriter.write(String.format(mobileTemplate, MOBILE_BEFORE, MOBILE_AFTER, mobileBuilder));
        }
    }

    public static List<String> readAllLinesFromInputTxt() {

        try {
            return Files.readAllLines(Paths.get("files/input.txt"), StandardCharsets.UTF_8)
                    .stream()
                    .map(s -> s.replaceAll("[\t　]", " "))
                    .map(s -> s.replaceAll("(^ +| +$|・・・)", ""))
                    .map(Tool::convert)
                    .map(s -> s.replaceAll("(1?[0-9])月 ?([1-3]?[0-9])日", "$1/$2 "))
                    .map(s -> s.replaceAll("[＆&]", "&amp;"))
                    .map(String::trim)
                    .map(s -> s.matches("^1?[0-9]/[1-3]?[0-9] .*") ? List.of("", s) : List.of(s))
                    .flatMap(Collection::stream)
                    .toList();

        } catch (IOException ioEx) {
            throw new UncheckedIOException(ioEx);
        }
    }

    public static String load(String path) {

        InputStream stream = Objects.requireNonNull(Tool.class.getResourceAsStream(path));
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                builder.append(line).append("\n");
                line = reader.readLine();
            }
            return builder.toString();

        } catch (IOException ioEx) {
            throw new UncheckedIOException(ioEx);
        }
    }

    public static String convert(String input) {

        final StringBuilder sb = new StringBuilder();
        input.chars().forEach(c -> {
            int i = Const.FROM.indexOf((char) c);
            sb.append(i < 0 ? (char) c : Const.TO.charAt(i));
        });
        return sb.toString();
    }

    public static String next(String line, String... separators) {

        for (String separator : separators) {
            int i = line.indexOf(separator);
            if (i >= 0) {
                return line.substring(0, i).trim();
            }
        }
        return line;
    }

    public static String sub(String line, String value) {

        return line.substring(value.length()).trim();
    }

    public static String div(String value) {

        return "<div>" + value.trim() + "</div>\n";
    }

    public static class Schedule {
        String date = "";
        String delivery = "";
        String band = "";
        String title = "";
        String members = "";
        String place = "";
        String time = "";
        String fee = "";
        String details = "";
    }
}
