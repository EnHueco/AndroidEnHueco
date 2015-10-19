package com.diegoalejogm.enhueco.Model.Other;

/**
 * Created by Diego on 10/18/15.
 */
public class Either<A, B>
{
    public A left;
    public B right;

    public Either(A left, B right)
    {
        this.left = left;
        this.right = right;
    }
}
