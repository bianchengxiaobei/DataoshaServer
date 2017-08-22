package com.chen.battle.structs;

public class CVector2D
{
	public float x;
	public float y;
	public CVector2D(float x,float y)
	{
		this.x = x;
		this.y = y;
	}
	public void zero()
	{
		this.x = 0;
		this.y = 0;
	}
	public float Length()
	{
		return (float) Math.sqrt(x*x+y*y);
	}
	public float SqrtLength()
	{
		return x*x+y*y;
	}
	public CVector2D Normalize()
	{
		CVector2D temp = new CVector2D(0,0);
		float length = this.Length();
		if (length == 0.0f)
		{
			return temp;
		}
		temp.x = this.x / length;
		temp.y = this.y / length;
		return temp;
	}
	public void normalized()
	{
		float length = this.Length();
		if (length == 0)
		{
			return;
		}
		this.x = this.x / length;
		this.y = this.y / length;
	}
	public boolean CanWatch(float dist,CVector2D targetPos)
	{
		return this.GetWatchDistSqr(targetPos) <= dist * dist;
	}
	public float GetWatchDistSqr(CVector2D targetPos)
	{
		float x = targetPos.x - this.x;
		float y = targetPos.y - this.y;
		return x * x + y * y;
	}
	public static CVector2D Sub(CVector2D v1,CVector2D v2)
	{
		CVector2D result = new CVector2D(0, 0);
		result.x = v1.x - v2.x;
		result.y = v1.y - v2.y;
		return result;
	}
}
