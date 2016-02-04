package com.diegoalejogm.enhueco.model.other;

/**
 * Created by Diego on 10/11/15.
 */
public class Tuple<A, B>
{
    public A first;
    public B second;

    public Tuple(A first, B second)
    {
        this.first = first;
        this.second = second;
    }
}
