import java.util.*;

/** Dependency-free tests. Run: see README. Exits non-zero on failure. */
public class NetworkTest {
    static int passed = 0;

    static void check(boolean cond, String name) {
        if (!cond) throw new AssertionError("FAILED: " + name);
        passed++;
        System.out.println("PASS  " + name);
    }

    static List<String> stops(int from, int to) {
        List<String> l = new ArrayList<>();
        for (int i = from; i < to; i++) l.add(String.valueOf(i));
        return l;
    }

    static Map<String, List<String>> routes(Object... kv) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            @SuppressWarnings("unchecked") List<String> v = (List<String>) kv[i + 1];
            m.put((String) kv[i], v);
        }
        return m;
    }

    public static void main(String[] a) {
        Set<String> none = new HashSet<>();

        // 1. Example from the problem statement
        Network ex = new Network(routes("R1", List.of("1", "2", "7"), "R2", List.of("3", "6", "7")));
        Network.Result r = ex.query("1", "6", none);
        check(r != null && r.buses == 2 && r.legs.get(0).route.equals("R1")
              && r.legs.get(0).alight.equals("7") && r.legs.get(1).route.equals("R2"), "example: 2 buses, R1 then R2 at 7");

        // 2. Inactive route
        check(ex.query("1", "6", Set.of("R2")) == null, "R2 inactive -> no route");

        // 3. Same stop
        check(ex.query("1", "1", none).buses == 0, "src == dst -> 0 buses");

        // 4. Disconnected
        Network dis = new Network(routes("A", List.of("1", "2"), "B", List.of("3", "4")));
        check(dis.query("1", "4", none) == null, "disconnected network -> no journey");

        // 5. Unknown stop
        check(ex.query("1", "99", none) == null && ex.query("99", "1", none) == null, "unknown stop -> no journey");

        // 6. Fewest buses, not fewest stops
        Network lng = new Network(routes("LONG", stops(1, 51), "S1", List.of("1", "100"), "S2", List.of("100", "50")));
        Network.Result lr = lng.query("1", "50", none);
        check(lr.buses == 1 && lr.legs.get(0).route.equals("LONG"), "50-stop direct bus beats 2-bus short path");

        // 7. Heavy overlap: 400 routes sharing 100 stops
        Map<String, List<String>> heavy = new LinkedHashMap<>();
        for (int i = 0; i < 400; i++) heavy.put("R" + i, stops(0, 100));
        heavy.put("X", List.of("0", "1000"));
        Network hn = new Network(heavy);
        long t = System.nanoTime();
        check(hn.query("5", "1000", none).buses == 2, "heavy overlap: correct answer");
        check((System.nanoTime() - t) / 1e9 < 1.0, "heavy overlap: finishes under 1s");

        // 8. Large network, repeated queries on one index
        Map<String, List<String>> big = new LinkedHashMap<>();
        for (int i = 0; i < 400; i++) {
            List<String> l = new ArrayList<>();
            for (int j = 0; j < 100; j++) l.add(String.valueOf((i * 7 + j) % 5000));
            big.put("R" + i, l);
        }
        Network bn = new Network(big);
        t = System.nanoTime();
        for (int q = 0; q < 50; q++) bn.query(String.valueOf(q), String.valueOf(4999 - q), none);
        check((System.nanoTime() - t) / 1e9 < 5.0, "400 routes x 100 stops, 50 repeated queries");

        // 9. Closure forces a longer path
        Network demo = new Network(routes("R1", List.of("1", "2", "3"), "R2", List.of("3", "4", "5"), "R3", List.of("1", "9", "5")));
        check(demo.query("1", "5", none).buses == 1, "demo: R3 direct = 1 bus");
        check(demo.query("1", "5", Set.of("R3")).buses == 2, "demo: R3 closed = 2 buses");

        // 10. Readable output
        check(ex.query("1", "6", none).toString().contains("change at: 7"), "output mentions change stop");

        System.out.println("\nAll " + passed + " checks passed.");
    }
}
