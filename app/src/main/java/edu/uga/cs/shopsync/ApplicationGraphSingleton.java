package edu.uga.cs.shopsync;

public class ApplicationGraphSingleton {

    private static ApplicationGraph applicationGraph;

    public static ApplicationGraph getInstance() {
        if (applicationGraph == null) {
            applicationGraph = DaggerApplicationGraph.create();
        }
        return applicationGraph;
    }

}