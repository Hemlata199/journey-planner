import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Interactive query tool. Usage: java Main network.txt
 * Network file: one route per line, "R1: 1 2 7"
 * Commands: query A B | disable R | enable R | status | quit
 */
public class Main {

    static Map<String, List<String>> load(String path) throws IOException {
        Map<String, List<String>> routes = new LinkedHashMap<>();
        for (String line : Files.readAllLines(Paths.get(path))) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(":", 2);
            routes.put(parts[0].trim(), Arrays.asList(parts[1].trim().split("[\\s,]+")));
        }
        return routes;
    }

    public static void main(String[] args) throws IOException {
        Network net = new Network(load(args.length > 0 ? args[0] : "network.txt"));
        Set<String> inactive = new TreeSet<>();
        System.out.println("Loaded " + net.routeCount()
            + " routes. Commands: query A B | disable R | enable R | status | quit");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String line = in.readLine();
            if (line == null) break;
            String[] p = line.trim().split("\\s+");
            if (p[0].isEmpty()) continue;
            switch (p[0].toLowerCase()) {
                case "quit": return;
                case "query":
                    if (p.length == 3) {
                        Network.Result r = net.query(p[1], p[2], inactive);
                        System.out.println(r == null ? "No journey exists" : r);
                    } else System.out.println("Usage: query A B");
                    break;
                case "disable":
                    if (p.length == 2) { inactive.add(p[1]); System.out.println(p[1] + " disabled"); }
                    break;
                case "enable":
                    if (p.length == 2) { inactive.remove(p[1]); System.out.println(p[1] + " enabled"); }
                    break;
                case "status":
                    System.out.println("Inactive: " + (inactive.isEmpty() ? "none" : inactive));
                    break;
                default: System.out.println("Unknown command");
            }
        }
    }
}
