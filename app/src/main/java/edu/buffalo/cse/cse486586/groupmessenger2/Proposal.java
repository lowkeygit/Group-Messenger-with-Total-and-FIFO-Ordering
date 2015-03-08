package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.security.Provider;

/**
 * Created by sherlock on 3/8/15.
 */
public class Proposal implements Serializable{
    private static final long serialVersionUID = 2L;
    String id;
    double proposedNum;
    String portNum;

    public Proposal(String id, double proposedNum, String portNum)
    {
        this.proposedNum = proposedNum;
        this.id = id;
        this.portNum = portNum;
    }
}
