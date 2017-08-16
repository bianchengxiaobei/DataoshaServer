package com.chen.battle.structs;



import com.chen.battle.ai.SSAI;
import com.chen.battle.message.res.ResGoAppearMessage;
import com.chen.battle.message.res.ResIdleStateMessage;
import com.chen.battle.message.res.ResRunningStateMessage;
import com.chen.message.Message;
import com.chen.move.struct.ColSphere;
import com.chen.move.struct.ColVector;
import com.chen.utils.MessageUtil;

public abstract class SSGameUnit extends SSMoveObject
{
	public long id;
	public BattleContext battle;
	public SSAI ai;
	public SGOActionStateInfo curActionInfo;
	public long enterBattleTime;
	public boolean bExpire;
	public SSGameUnit(long playerId,BattleContext battle)
	{
		this.id = playerId;
		this.battle = battle;
		this.curActionInfo = new SGOActionStateInfo();
	}
	public void BeginActionIdle(boolean asyn)
	{
		if (EGOActionState.Idle != curActionInfo.eOAS)
		{
			SetGOActionState(EGOActionState.Idle);
			if (asyn && battle != null)
			{
				battle.SyncState(this);
			}
		}
	}
	public void BeginActionMove(CVector2D dir,boolean asyn)
	{
		SetGOActionState(EGOActionState.Running);;
		curActionInfo.dir = dir;
		curActionInfo.distMove = 0;
		if (asyn && battle != null)
		{
			battle.SyncState(this);
		}
	}
	public void SetGOActionState(EGOActionState state)
	{
		this.curActionInfo.eOAS = state;
		this.curActionInfo.time = System.currentTimeMillis();
	}
	/**
	 * 构建角色状态消息
	 * @return
	 */
	public Message ConstructObjStateMessage()
	{
		switch (this.curActionInfo.eOAS) 
		{
		case Idle:
		case Controlled:
			ResIdleStateMessage res = new ResIdleStateMessage();
			res.playerId = this.id;
			res.posX = this.curActionInfo.pos.x;
			res.posY = this.curActionInfo.pos.y;
			res.dirX = (int)this.curActionInfo.dir.x;
			res.dirY = (int)this.curActionInfo.dir.y;
			return res;
		case Running:
			ResRunningStateMessage message = new ResRunningStateMessage();
			message.playerId = this.id;
			message.posX = this.curActionInfo.pos.x;
			message.posY = this.curActionInfo.pos.y;
			message.dirX = (int)this.curActionInfo.dir.x;
			message.dirY = (int)this.curActionInfo.dir.y;
			message.moveSpeed = (int)GetSpeed();
			return message;
		}
		return null;
	}
	public void SendAppearMessage()
	{
		ResGoAppearMessage message = new ResGoAppearMessage();
		message.playerId = this.id;
		message.dirX = (int)this.GetCurDir().x;
		message.dirY = (int)this.GetCurDir().y;
		message.posX = (int)this.GetCurPos().x;
		message.posY = (int)this.GetCurPos().y;
		message.hp = this.GetCurHp();
		MessageUtil.tell_battlePlayer_message(this.battle, message);
	}
	public boolean IfCanImpact()
	{
		return IsDead() == false;
	}

	public boolean IsDead()
	{
		return this.curActionInfo.eOAS == EGOActionState.Dead || bExpire == true;
	}
	public ColVector GetColVector()
	{
		ColVector vector = new ColVector(GetCurPos().x, GetCurPos().y);
		return vector;
	}
	public ColSphere GetColSphere()
	{
		ColSphere sphere = new ColSphere(GetColVector(), GetColRadius());
		return sphere;
	}
	public int GetColRadius()
	{
		return this.GetColliderRadius();
	}
	public CVector2D GetCurPos()
	{
		return this.curActionInfo.pos;
	}
	public CVector2D GetCurDir()
	{
		return this.curActionInfo.dir;
	}
	public int GetCurHp()
	{
		return 100;
	}
	
	public abstract int GetColliderRadius();
	/*
	 * 发送移动消息
	 * @see com.chen.battle.structs.SSMoveObject#OnStartMove(com.chen.move.struct.ColVector)
	 */
	@Override
	public void OnStartMove(ColVector dir)
	{
		CVector2D cDir = new CVector2D(0, 0);
		cDir.x = dir.x;
		cDir.y= dir.y;
		if (curActionInfo.eOAS.value < EGOActionState.PassiveState.value)
		{
			BeginActionMove(cDir, true);
		}
		//可能有被动技能触发
		//OnPassitiveSkillCalled(EPassiveSkillTriggerType_Move, this);
	}
	@Override
	public void OnChangeDir(ColVector dir)
	{
		CVector2D cDir = new CVector2D(0, 0);
		cDir.x = dir.x;
		cDir.y = cDir.y;
		curActionInfo.dir = cDir;
	}
	@Override
	public void OnMoveBlock()
	{
		if (this.curActionInfo.eOAS.value < EGOActionState.PassiveState.value)
		{
			this.CheckBeginActionFree(true);
		}
		//技能停止移动
//		if(m_moveHolder != NULL){
//			m_moveHolder->OnStopMove();
		this.ai.OnMoveBlock();
	}
	
	private void CheckBeginActionFree(boolean asyn)
	{
		if (this.curActionInfo.eOAS.value >= EGOActionState.PassiveState.value)
		{
			return;
		}
		BeginActionIdle(asyn);
	}
}
