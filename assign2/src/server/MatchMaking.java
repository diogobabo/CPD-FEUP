package server;

public class MatchMaking implements Runnable {
    private final Server server;
    private long matchmakingInterval = 1000;

    public MatchMaking(Server server, long matchmakingInterval) {
        this.server = server;
        this.matchmakingInterval = matchmakingInterval;
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(1000);
                i++;
                server.matchMaking();
                System.out.println("Second: " + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
