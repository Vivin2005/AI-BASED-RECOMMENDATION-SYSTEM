import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleRecommender {

    private Map<Integer, Map<Integer, Double>> data = new HashMap<>();

    // Load CSV data
    public void loadCsv(String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;

        while ((line = br.readLine()) != null) {

            // REMOVE ALL DOUBLE QUOTES
            line = line.replace("\"", "").trim();

            if (line.isEmpty()) continue;  // skip empty lines

            String[] parts = line.split(",");

            int user = Integer.parseInt(parts[0].trim());
            int item = Integer.parseInt(parts[1].trim());
            double rating = Double.parseDouble(parts[2].trim());

            data.computeIfAbsent(user, k -> new HashMap<>()).put(item, rating);
        }
        br.close();
    }

    private Map<Integer, Map<Integer, Double>> buildItemVectors() {
        Map<Integer, Map<Integer, Double>> items = new HashMap<>();

        for (var userEntry : data.entrySet()) {
            int user = userEntry.getKey();
            for (var itemEntry : userEntry.getValue().entrySet()) {
                int item = itemEntry.getKey();
                double rating = itemEntry.getValue();
                items.computeIfAbsent(item, k -> new HashMap<>()).put(user, rating);
            }
        }
        return items;
    }

    private double cosine(Map<Integer, Double> a, Map<Integer, Double> b) {
        Set<Integer> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());

        double dot = 0, na = 0, nb = 0;

        for (int u : common) dot += a.get(u) * b.get(u);
        for (double v : a.values()) na += v * v;
        for (double v : b.values()) nb += v * v;

        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public List<Integer> recommend(int userId, int topN) {
        var items = buildItemVectors();
        var userRatings = data.getOrDefault(userId, new HashMap<>());

        Map<Integer, Double> scores = new HashMap<>();

        for (int candidate : items.keySet()) {
            if (userRatings.containsKey(candidate)) continue;

            double score = 0;
            for (int ratedItem : userRatings.keySet()) {
                double sim = cosine(items.get(candidate), items.get(ratedItem));
                score += sim * userRatings.get(ratedItem);
            }
            scores.put(candidate, score);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws Exception {
        SimpleRecommender r = new SimpleRecommender();

        // FULL PATH (YOUR EXACT FOLDER)
        r.loadCsv("D:\\intership_java\\task04\\recommendation-system\\data\\ratings.csv");

        int userId = 5;
        System.out.println("Recommendations for user " + userId + ":");

        List<Integer> recs = r.recommend(userId, 5);
        for (Integer item : recs) {
            System.out.println("  Item: " + item);
        }
    }
}
