package com.chen.move.struct;

public class ColVector
{
	public float x;
	public float y;
	
	public ColVector(float x,float y)
	{
		this.x = x;
		this.y = y;
	}
	public ColVector(ColVector v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	public boolean equals(ColVector other)
	{
		if (this.x != other.x || this.y != other.y)
		{
			return false;
		}
		return true;
	}
	public float Length()
	{
		float ls = this.LengthSqrt();
		return (float) Math.sqrt(ls);
	}
	public float LengthSqrt()
	{
		return x*x + y*y;
	}
	public void AddVector(ColVector vec)
	{
		this.x += vec.x;
		this.y += vec.y;
	}
	public ColVector Normalize()
	{
		ColVector temp = new ColVector(0,0);
		float length = this.Length();
		if (length == 0.0f)
		{
			return temp;
		}
		temp.x = this.x / length;
		temp.y = this.y / length;
		return temp;
	}
	public static ColVector Multiply(ColVector o, float value)
	{
		ColVector temp = new ColVector(o);
		temp.x = temp.x * value;
		temp.y = temp.y * value;
		return temp;
	}
	public static ColVector Sub(ColVector o1,ColVector o2)
	{
		ColVector temp = new ColVector(0,0);
		temp.x = o1.x - o2.x;
		temp.y = o1.y - o2.y;
		return temp;
	}
}
