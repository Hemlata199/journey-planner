import java.util.*;

/** Build once, query many times. Index: stop -> routes serving it. */
public class Network {

    public static class Leg {
        public final String route, board, alight;
        Leg(String route, String board, String alight) {
            this.route = route; this.board = board; this.alight = alight;
        }
    }

    public static class Result {
        public final int buses;
        public final List<Leg> legs;
        Result(int buses, List<Leg> legs) { this.buses = buses; this.legs = legs; }

        @Override public String toString() {
            if (buses == 0) return "0 buses: you are already at your destination";
            StringBuilder sb = new StringBuilder(buses + (buses > 1 ? " buses: " : " bus: "));
            for (int i = 0; i < legs.size(); i++) {
                Leg l = legs.get(i);
                if (i > 0) sb.append(", then ");
                sb.append("Take ").append(l.route).append(" from ").append(l.board)
                  .append(" -> ").append(l.alight);
            }
            if (legs.size() > 1) {
                List<String> changes = new ArrayList<>();
                for (int i = 0; i < legs.size() - 1; i++) changes.add(legs.get(i).alight);
                sb.append("  (change at: ").append(String.join(", ", changes)).append(")");
            }
            return sb.toString();
        }
    }

    private final Map<String, List<String>> routes = new LinkedHashMap<>();
    private final Map<String, List<String>> stopRoutes = new HashMap<>();

    public Network(Map<String, List<String>> input) {
        for (Map.Entry<String, List<String>> e : input.entrySet()) {
            List<String> stops = new ArrayList<>(new LinkedHashSet<>(e.getValue())); // dedupe, keep order
            routes.put(e.getKey(), stops);
            for (String s : stops)
                stopRoutes.computeIfAbsent(s, k -> new ArrayList<>()).add(e.getKey());
        }
    }

    public int routeCount() { return routes.size(); }

    /**
     * Fewest-buses journey, or null if none exists.
     * BFS where each level = one more bus boarded. Each route is expanded at most
     * once per query, so cost is O(total stop appearances).
     */
    public Result query(String src, String dst, Set<String> inactive) {
        if (src.equals(dst)) return new Result(0, new ArrayList<>());
        if (!stopRoutes.containsKey(src) || !stopRoutes.containsKey(dst)) return null;

        Set<String> seenRoutes = new HashSet<>();
        Set<String> seenStops = new HashSet<>();
        Map<String, String[]> parent = new HashMap<>(); // stop -> {prevStop, route}
        seenStops.add(src);
        List<String> frontier = new ArrayList<>(List.of(src));

        while (!frontier.isEmpty()) {
            List<String> next = new ArrayList<>();
            for (String s : frontier) {
                for (String r : stopRoutes.getOrDefault(s, List.of())) {
                    if (inactive.contains(r) || !seenRoutes.add(r)) continue; // expand each route once
                    for (String t : routes.get(r)) {
                        if (seenStops.add(t)) {
                            parent.put(t, new String[]{s, r});
                            if (t.equals(dst)) return build(dst, parent);
                            next.add(t);
                        }
                    }
                }
            }
            frontier = next;
        }
        return null;
    }

    private Result build(String dst, Map<String, String[]> parent) {
        LinkedList<Leg> legs = new LinkedList<>();
        String cur = dst;
        while (parent.containsKey(cur)) {
            String[] p = parent.get(cur);
            legs.addFirst(new Leg(p[1], p[0], cur));
            cur = p[0];
        }
        return new Result(legs.size(), legs);
    }
}
