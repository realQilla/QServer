package net.qilla;

public final class QServer {
    public static void main(String[] args) {
        MCServ mcServ = new MCServ("0.0.0.0", 25565);
        mcServ.init();
    }
}