package com.chen.move.handler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.chen.battle.handler.ReqEnterSceneHandler;
import com.chen.battle.manager.BattleManager;
import com.chen.battle.message.req.ReqEnterSceneMessage;
import com.chen.battle.structs.BattleContext;
import com.chen.battle.structs.CVector2D;
import com.chen.battle.structs.SSPlayer;
import com.chen.command.Handler;
import com.chen.move.message.req.ReqAskMoveMessage;
import com.chen.player.manager.PlayerManager;
import com.chen.player.structs.Player;

public class ReqAskMoveHandler extends Handler
{

	private Logger log = LogManager.getLogger(ReqAskMoveHandler.class);
	@Override
	public void action() 
	{
		try 
		{
			ReqAskMoveMessage msg = (ReqAskMoveMessage)getMessage();
			Player player = PlayerManager.getInstance().getPlayer(msg.getRoleId().get(0));
			if (player == null)
			{
				log.error("玩家没有注册角色");
				return;
			}
			BattleContext battle = BattleManager.getInstance().getBattleContext(player);
			if (battle == null)
			{
				log.error("该角色还没有战斗对决");
				return;
			}
			SSPlayer player2 = battle.getUserBattleInfo(player).sPlayer;
			if (player2 != null)
			{
				CVector2D dir = new CVector2D(msg.x, 0);
				BattleManager.getInstance().askMove(player2,dir);
			}else
			{
				log.error("不存在SSPlayer");
			}
			
		} catch (Exception e)
		{
			log.error("玩家加载游戏场景失败");
		}		
	}
}
