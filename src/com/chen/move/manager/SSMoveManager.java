package com.chen.move.manager;

import java.util.HashSet;
import java.util.Set;

import com.chen.battle.structs.SSMoveObject;
import com.chen.move.struct.ColVector;
import com.chen.move.struct.EAskStopMoveType;
import com.chen.move.struct.ESSMoveObjectMoveType;
import com.chen.move.struct.SSMoveObjectStatus;

public class SSMoveManager
{
	public Set<SSMoveObject> allMoveObjectSet = new HashSet<>();
	public void AddMoveObject(SSMoveObject object)
	{
		allMoveObjectSet.remove(object);
		object.moveStatus = SSMoveObjectStatus.SSMoveObjectStatus_Stand;
		object.stepMoveTarget = object.GetColSphere();
		object.beforeMovePos = object.stepMoveTarget.point;
		allMoveObjectSet.add(object);
	}
	public boolean AskStartMoveDir(SSMoveObject obj,ColVector dir)
	{
		if (AskStartMoveCheck(obj) == false)
		{
			return false;
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_Stand)
		{
			obj.moveType = ESSMoveObjectMoveType.Dir;
			long now = System.currentTimeMillis();
			obj.moveStatus = SSMoveObjectStatus.SSMoveObjectStatus_Move;
			obj.askMoveDir = dir;
			obj.SetDir(dir);;
			obj.startMoveTime = now;
			//计算下一个100毫秒后的位置
			obj.CalculateStepMoveTarget(now+100);
			//检测是否会撞墙
			
			obj.OnStartMove(dir);
			return true;
		}
		return true;
	}
	public boolean AskStopMoveObject(SSMoveObject obj,EAskStopMoveType type)
	{
		if (!this.allMoveObjectSet.contains(obj))
		{
			return false;
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_Stand)
		{
			return false;
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_Move)
		{
			if (type == EAskStopMoveType.All || 
					(type == EAskStopMoveType.Dir && obj.moveType == ESSMoveObjectMoveType.Dir) ||
					type == EAskStopMoveType.Target && obj.moveType == ESSMoveObjectMoveType.Target)
			{
				StopLastStep(obj, true);
			}
			
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_ForceMove)
		{
			
		}
		return true;
	}
	private boolean AskStartMoveCheck(SSMoveObject obj)
	{
		if (!this.allMoveObjectSet.contains(obj))
		{
			return false;
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_ForceMove)
		{
			return false;
		}
		if (obj.moveStatus == SSMoveObjectStatus.SSMoveObjectStatus_Move)
		{
			//先将上次移动终止
			StopLastStep(obj, false);
		}
		return true;
	}
	private void StopLastStep(SSMoveObject obj,boolean bCallback)
	{
		long now = System.currentTimeMillis();
		obj.CalculateStepMoveTarget(now);
		TryMove(obj, now);
		obj.Stop(now, bCallback);
	}
	private float TryMove(SSMoveObject obj,long now)
	{
		//静态碰撞检测
		//移动
		float moveDist =obj.Move(now); 
		return moveDist;
	}
}
