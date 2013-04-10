import java.util.*;

/**
 * User: Vladyslav Kiriushkin
 * Date: 4/9/13
 * Time: 11:49 AM
 */
public class BaseballElimination {

    /*
     * key of hashmap is team name
     * stats[0] - teamID
     * stats[1] - number of wins
     * stats[2] - number of loses
     * stats[3] - numbers of games to play
     * stats[4]-[N] - games to play against each team
     */
    private HashMap<String,List<Integer>> teamsStats;
    private HashMap<String,List<String>> eliminatorsList;
    private List<String> vertices;
    private FlowNetwork flowNetwork;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        int numberOfTeams = in.readInt();
        teamsStats = new HashMap<String, List<Integer>>(numberOfTeams);
        eliminatorsList = new HashMap<String, List<String>>(numberOfTeams);
        int teamId = 0;
        while (in.hasNextLine()) {
            String[] team = in.readLine().split("\\s+");
            if (team.length == 1)
                continue;
            List<Integer> stats = new ArrayList<Integer>(4 + numberOfTeams);
            stats.add(teamId);
            for (int i = 1; i < team.length; i++) {
                stats.add(Integer.parseInt(team[i]));
            }
            teamsStats.put(team[0], stats);
            teamId++;
        }
    }

    // number of teams
    public int numberOfTeams() {
        return teamsStats.size();
    }

    // all teams
    public Iterable<String> teams() {
        return teamsStats.keySet();
    }

    // number of wins for given team
    public int wins(String team) {
        if (!teamsStats.containsKey(team))
            throw new IllegalArgumentException();

        return teamsStats.get(team).get(1);
    }

    // number of losses for given team
    public int losses(String team) {
        if (!teamsStats.containsKey(team))
            throw new IllegalArgumentException();

        return teamsStats.get(team).get(2);
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!teamsStats.containsKey(team))
            throw new IllegalArgumentException();

        return teamsStats.get(team).get(3);
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!teamsStats.containsKey(team1) || !teamsStats.containsKey(team2))
            throw new IllegalArgumentException();

        int team2Id = teamsStats.get(team2).get(0);
        return teamsStats.get(team1).get(4 + team2Id);
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (!teamsStats.containsKey(team))
            throw new IllegalArgumentException();

        generateVertices(team);
        generateFlowNetwork(vertices);
        FordFulkerson ff = new FordFulkerson(flowNetwork,0,vertices.size()-1);
        int flowExpected = 0;
        for (FlowEdge edge : flowNetwork.adj(0)) {
            flowExpected += edge.capacity();
        }
        if (ff.value() < flowExpected) {
            for (int i = vertices.size() - teamsStats.size(); i < vertices.size(); i++) {
                if (ff.inCut(i)) {
                    Iterator it = teamsStats.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String,List<Integer>> entry = (Map.Entry<String,List<Integer>>) it.next();
                        int id = entry.getValue().get(0);
                        if (id == Integer.parseInt(vertices.get(i))) {
                            if (eliminatorsList.containsKey(team)) {
                                eliminatorsList.get(team).add(entry.getKey());
                            } else {
                                List<String> eliminators = new ArrayList<String>(teamsStats.size());
                                eliminators.add(entry.getKey());
                                eliminatorsList.put(team,eliminators);
                            }
                        }
                    }
                }
            }
            return true;
        } else
            return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (!teamsStats.containsKey(team))
            throw new IllegalArgumentException();

        //in case elimination was already checked
        if (eliminatorsList.containsKey(team)) {
            return eliminatorsList.get(team);
        } else {
            //elimination should be discovered
            if (isEliminated(team)) {
                return eliminatorsList.get(team);
            } else
                return null;
        }
    }

    private int factorial(int num) {
        if (num < 0 )
            throw new IllegalArgumentException();
        return (num == 0) ? 1 : num * factorial(num - 1);
    }

    private int gamesLeftCount() {
        if (teamsStats.size() < 2)
            return 0;
        return factorial(teamsStats.size() - 1) / (factorial(2) * factorial((teamsStats.size() -1)  - 2));
    }

    private void generateVertices(String sourceTeam) {
        vertices = new ArrayList<String>(1 + gamesLeftCount() + (teamsStats.size() - 1) + 1);
        vertices.add(String.valueOf(teamsStats.get(sourceTeam).get(0)));
        //add games left: n!/(m!*(n-m)!)
        for (String team : teamsStats.keySet()) {
            if (team.equals(sourceTeam))
                continue;
            else {
                for (String t : teamsStats.keySet()) {
                    if (team.equals(t) || t.equals(sourceTeam))
                        continue;
                    else {
                        String vertexId = teamsStats.get(t).get(0) > teamsStats.get(team).get(0) ?
                                String.valueOf(teamsStats.get(t).get(0)).concat("-").concat(String.valueOf(teamsStats.get(team).get(0))) :
                                String.valueOf(teamsStats.get(team).get(0)).concat("-").concat(String.valueOf(teamsStats.get(t).get(0)));
                        if (!vertices.contains(vertexId))
                            vertices.add(vertexId);
                    }
                }
            }
        }
        //add teams that not equal to source
        for (String team : teamsStats.keySet()) {
            if (team.equals(sourceTeam))
                continue;
            else {
                vertices.add(String.valueOf(teamsStats.get(team).get(0)));
            }
        }
        //add target
        vertices.add("-1");
    }

    private void generateFlowNetwork(List<String> vertices) {
        flowNetwork = new FlowNetwork(vertices.size());
        //edges from source to games:
        for (int i = 1; i <= gamesLeftCount(); i++) {
            int capacity = gamesBetweenTeamsLeft(vertices.get(i));
            FlowEdge fromSourceToGameLeftEdge = new FlowEdge(
                    0,
                    i,
                    capacity, 0);
            flowNetwork.addEdge(fromSourceToGameLeftEdge);
        }
        //edges from games to teams:
        for (int i = 1; i < gamesLeftCount() + 1; i++) {
            String[] teamsId = vertices.get(i).split("-");
            for (int j = gamesLeftCount() + 1; j < vertices.size() - 1; j++) {
                if (vertices.get(j).equals(teamsId[0]) || vertices.get(j).equals(teamsId[1])) {
                    FlowEdge fromGameLeftToTeam = new FlowEdge(
                            i,
                            j,
                            Double.POSITIVE_INFINITY);
                    flowNetwork.addEdge(fromGameLeftToTeam);
                }
            }
        }
        //edges from teams to target:
        for (int j = gamesLeftCount() + 1; j < vertices.size() - 1; j++) {
            int teamId = Integer.parseInt(vertices.get(j));
            int capacity = 0;
            Iterator it = teamsStats.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String,List<Integer>> entry = (Map.Entry<String,List<Integer>>) it.next();
                int id = entry.getValue().get(0);
                if (id == teamId) {
                    capacity -= entry.getValue().get(1);
                }
                if (id == Integer.parseInt(vertices.get(0))) {
                    capacity += entry.getValue().get(1) + entry.getValue().get(3);
                }
            }
            if (capacity >= 0) {
                FlowEdge fromTeamToTarget = new FlowEdge(
                        j,
                        vertices.size() - 1,
                        capacity, 0);
                flowNetwork.addEdge(fromTeamToTarget);
            } else {
                FlowEdge fromTeamToTarget = new FlowEdge(
                        vertices.size() - 1,
                        j,
                        Math.abs(capacity), 0);
                flowNetwork.addEdge(fromTeamToTarget);
            }
        }
    }

    private int gamesBetweenTeamsLeft(String gameLeftId) {
        String[] teamsId = gameLeftId.split("-");
        Iterator it = teamsStats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,List<Integer>> entry = (Map.Entry<String,List<Integer>>) it.next();
            int id = entry.getValue().get(0);
            if (id == Integer.parseInt(teamsId[0])) {
                return entry.getValue().get(4 + Integer.parseInt(teamsId[1]));
            }
            if (id == Integer.parseInt(teamsId[1])) {
                return entry.getValue().get(4 + Integer.parseInt(teamsId[0]));
            }
        }
        return -1;
    }


    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
