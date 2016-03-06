package com.enhueco.model.structures;

/**
 * Created by Diego on 10/11/15.
 * Data structure to contain to elements (a tuple)
 */
public class Tuple<A, B>
{
    /**
     * First element in the tuple
     */
    public A first;

    /**
     * Second element in the tuple
     */
    public B second;

    /**
     * Creates a new tuple with the elements given as arguments
     * @param first First element to be added to the tuple
     * @param second Second element to be added to the tuple
     */
    public Tuple(A first, B second)
    {
        this.first = first;
        this.second = second;
    }
}
