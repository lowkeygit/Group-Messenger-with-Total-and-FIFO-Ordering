package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;

/**
 * Created by sherlock on 3/8/15.
 */
public class Agreed implements Serializable {
    private static final long serialVersionUID = 3L;
    String id;
    double agreedNumber;

    public Agreed(String id, double agreedNumber)
    {
        this.agreedNumber = agreedNumber;
        this.id = id;
    }
}
