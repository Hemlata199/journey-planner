# Journey Recommendation System (Java)

Finds the journey with the **fewest buses** across overlapping routes, with support for closed routes.

## Run (JDK 11+)
```bash
mkdir -p out
javac -d out src/*.java test/*.java
java -cp out Main network.txt        # query 1 5 | disable R3 | enable R3 | status | quit
java -cp out NetworkTest             # run tests
```
Network file: one route per line, e.g. `R1: 1 2 7`

## Algorithm
BFS where each level = one more bus boarded. Staying on a bus is free, so the first level
reaching the destination is the minimum number of buses (not minimum stops).
- Index `stop -> routes` is built once in `new Network(...)`; queries reuse it.
- Each route is expanded at most once per query => O(total stop appearances) per query (<= 40,000).
- `src == dst` returns 0 buses; `null` means no journey exists.
- Inactive routes are passed per query and skipped, so no rebuild on closures.
