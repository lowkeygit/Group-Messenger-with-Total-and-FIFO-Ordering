package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;

/**
 * Created by sherlock on 3/8/15.
 */
public class Normal implements Serializable{
    private static final long serialVersionUID = 1L;
    String id;
    String message;
    public Normal(String id, String message)
    {
        this.id = id;
        this.message = message;
    }
}
