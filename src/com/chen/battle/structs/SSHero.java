package com.chen.battle.structs;

import com.chen.battle.ai.SSAI_Hero;
import com.chen.move.struct.ColVector;

public class SSHero extends SSGameUnit
{
	public CVector2D bornPos;
	public int colliderRadius;
	public SSHero(long playerId, BattleContext battle)
	{
		super(playerId, battle);
	}
	@Override
	public int GetColliderRadius()
	{	
		return colliderRadius;
	}
	@Override
	public float GetSpeed() 
	{	
		return 10;
	}
	@Override
	public void OnMoved(ColVector pos)
	{
		CVector2D cPos = new CVector2D(0, 0);
		cPos.x = pos.x;
		cPos.y = pos.y;
		curActionInfo.pos = cPos;
	}
	public void AskMoveDir(CVector2D dir)
	{
		SSAI_Hero hAi = (SSAI_Hero)ai;
		if (hAi != null)
			hAi.AskMoveDir(dir);
	}
	public void AskStopMove()
	{
		SSAI_Hero hAi = (SSAI_Hero)ai;
		if (hAi != null)
			hAi.AskStopMove();
	}
	public void ResetAI()
	{
		if (ai != null)
		{
			ai = null;
		}
		ai = new SSAI_Hero(this);
	}
}
