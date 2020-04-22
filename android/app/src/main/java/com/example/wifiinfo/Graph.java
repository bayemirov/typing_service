package com.example.wifiinfo;

import java.lang.reflect.Array;
import java.util.*;

public class Graph {

    private List<String> nodes = new ArrayList<String>();
    private Map<String, Integer> nodeWPM = new HashMap<>();
    private Map<String, Integer> nodeRank = new HashMap<>();
    private final int WPMtreshold = 50;
    private final int maxWPM = 500;

    public void initNodes(String data) {
        String[] lines = data.split(System.getProperty("line.separator"));
        for (String line: lines) {
            if (line.equals(""))
                continue;
            nodes.add(line);
        }
    }

    public void updateWPM(Map<String, Integer> WPM) {
        for (String node: nodes) {
            Integer wpm = 0, cnt = 0;
            for (Map.Entry<String, Integer> entry : WPM.entrySet()) {
                int lastIndex = 0;
                int count = 0;
                // Occurence # of substr in node
                while (lastIndex != -1) {
                    lastIndex = node.indexOf(entry.getKey(), lastIndex);
                    if (lastIndex != -1) {
                        count++;
                        lastIndex += entry.getKey().length();
                    }
                }
                // -----------------------------

                wpm += entry.getValue() * count;
                cnt += count;

            }
            Integer speed = 0;
            if (cnt > 0)
                speed = wpm / cnt;
            nodeWPM.put(node, speed);
        }
        //System.out.println("[occ]: ");
        //for (Map.Entry<String, Integer> x: nodeWPM.entrySet())
        //    System.out.println(x.getKey() + " --- " + x.getValue());
    }

    public String getRandomNode() {
        List<String> keysAsArray = new ArrayList(nodeWPM.keySet());
        if (keysAsArray.size() <= 0)
            return "x";
        Random r = new Random();
        return keysAsArray.get(r.nextInt(keysAsArray.size()));
    }

    public String[] getAbsRandomNodes(int k, Map<String, Boolean> used) {
        List<String> keysAsArray = nodes;
        if (keysAsArray.size() <= 0)
            return new String[0];
        Random r = new Random();
        List<String> result = new ArrayList();

        while(k > 0) {
            String x = keysAsArray.get(r.nextInt(keysAsArray.size()));
            if (result.contains(x))
                continue;
            if (used.containsKey(x) == true && used.get(x))
                continue;
            result.add(x);
            k--;
        }
        String[] xx = result.toArray(new String[0]);

        return xx;
    }

    public void increaseRank(String node) {
        int count = nodeRank.containsKey(node) ? nodeRank.get(node) : 0;
        nodeRank.put(node, count + 1);
    }

    public String getNextRandomNeighbour(String node) {
        List<String> neighbours = new ArrayList();
        for (Map.Entry<String, Integer> x: nodeWPM.entrySet()) {
            if (node.equals(x.getKey()))
                continue;
            if (nodeWPM.containsKey(node) && x.getValue() >= nodeWPM.get(node) - WPMtreshold && x.getValue() <= nodeWPM.get(node) + WPMtreshold)
                neighbours.add(x.getKey());
        }
        //System.out.println("exit");
        Map<String, Integer> left = new HashMap<>();
        Map<String, Integer> right = new HashMap<>();
        Map<Integer, List<String>> sorted = new HashMap<>();
        for (String neighbour: neighbours) {
            int nwpm = nodeWPM.get(neighbour);
            if (!sorted.containsKey(nwpm))
                sorted.put(nwpm, new LinkedList<String>());
            sorted.get(nwpm).add(neighbour);
        }
        int boundary = 1;
        for (int i = 1; i <= maxWPM; i++) {
            if (!sorted.containsKey(i))
                continue;
            List<String> ls = sorted.get(i);
            for (String x: ls) {
                left.put(x, boundary);
                right.put(x, boundary + (maxWPM - i));
                boundary += (maxWPM - i);
            }
        }
        Random r = new Random();
        String ret = "";
        int randomInt = r.nextInt(boundary);
        for (String x: neighbours) {
            if (!left.containsKey(x))
                continue;
            if (left.get(x) <= randomInt && randomInt <= right.get(x))
                ret = x;
        }
        return ret;
    }

    public String[] getTopRankedNodes(int k, Map<String, Boolean> used) {
        Map<Integer, List<String>> sorted = new HashMap<>();
        int max = 0;
        for (String x: nodeRank.keySet()) {
            if (x.equals(""))
                continue;
            if (!sorted.containsKey(nodeRank.get(x)))
                sorted.put(nodeRank.get(x), new LinkedList<String>());
            sorted.get(nodeRank.get(x)).add(x);
            max = Math.max(max, nodeRank.get(x));
        }

        List<String> result = new ArrayList();

        for (int rank = max; rank >= 0; rank--) {
            if (!sorted.containsKey(rank))
                continue;
            if (k <= 0)
                break;
            for (String x: sorted.get(rank)) {
                if (k <= 0)
                    break;
                if (used.containsKey(x) && used.get(x))
                    continue;
                result.add(x);
                k--;
            }
        }

        String[] xx = result.toArray(new String[0]);

        return xx;
    }

    /*public void clearEdges() {
        for (Map.Entry<T, List<T>> entry : map.entrySet())
            entry.getValue().clear();
    }

    public void addVertex(T s) {
        map.put(s, new LinkedList<T>());
    }

    public void addEdge(T source,
                        T destination,
                        boolean bidirectional) {

        if (!map.containsKey(source))
            addVertex(source);

        if (!map.containsKey(destination))
            addVertex(destination);

        if (hasEdge(source, destination))
            return;

        map.get(source).add(destination);
        if (bidirectional == true) {
            map.get(destination).add(source);
        }
    }

    public boolean hasVertex(T s) {
        if (map.containsKey(s)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasEdge(T s, T d) {
        if (hasVertex(s) == false)
            return false;
        if (map.get(s).contains(d)) {
            return true;
        }
        else {
            return false;
        }
    }*/
}