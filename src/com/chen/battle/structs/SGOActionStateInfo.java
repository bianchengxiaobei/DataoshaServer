package com.chen.battle.structs;

public class SGOActionStateInfo
{
	public EGOActionState eOAS;
	public long time;
	public CVector2D pos;
	public CVector2D dir;
	public int skillId;
	public CVector2D skillTargetPos;
	public long skillTargetId;
	public float distMove;
	public SGOActionStateInfo()
	{
		eOAS = EGOActionState.Idle;
		time = System.currentTimeMillis();
	}
}
