package com.chen.battle.ai;

import com.chen.battle.structs.CVector2D;
import com.chen.battle.structs.SSGameUnit;

public class SSAI_Hero extends SSAI
{
	public boolean bIsMoveDir;
	public SSAI_Hero(SSGameUnit _theOwner) 
	{
		super(_theOwner);
	}
	public void AskMoveDir(CVector2D dir)
	{
		if(IfPassitiveState() == true)
		{
			return;
		}
		if (theOwner.battle.AskMoveDir(theOwner, dir) == false)
		{
			theOwner.BeginActionIdle(true);
			return;
		}
		bIsMoveDir = true;
		bIsMoving = true;
	}
	public void AskStopMove()
	{
		if(IfPassitiveState() == true)
		{
			return;
		}
		theOwner.battle.AskStopMoveDir(theOwner);
		bIsMoveDir = false;
	}
}
