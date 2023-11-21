package edu.uga.cs.shopsync;

/**
 * The application graph singleton. Returns the application graph.
 */
public class ApplicationGraphSingleton {

    private static ApplicationGraph applicationGraph;

    /**
     * Returns the application graph.
     *
     * @return the application graph
     */
    public static ApplicationGraph getInstance() {
        if (applicationGraph == null) {
            applicationGraph = DaggerApplicationGraph.create();
        }
        return applicationGraph;
    }

}