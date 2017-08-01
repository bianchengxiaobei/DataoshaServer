package com.chen.battle.structs;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DebugGraphics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

import com.chen.battle.message.res.ResEnterSceneMessage;
import com.chen.battle.message.res.ResGamePrepareMessage;
import com.chen.battle.message.res.ResSceneLoadedMessage;
import com.chen.battle.message.res.ResSelectHeroMessage;
import com.chen.player.manager.PlayerManager;
import com.chen.player.structs.Player;
import com.chen.server.BattleServer;
import com.chen.server.config.BattleConfig;

import com.chen.utils.MessageUtil;

public class BattleContext extends BattleServer
{
	private Logger log = LogManager.getLogger(BattleContext.class);
	private EBattleType battleType;
	private EBattleServerState battleState = EBattleServerState.eSSBS_SelectHero;
	private long battleId;
	private long battleStateTime;
	private BattleUserInfo[] m_battleUserInfo = new BattleUserInfo[maxMemberCount];
	private int m_battleHero;//战斗中英雄的数量
	public static final int maxMemberCount = 6; 
	public static final int timeLimit = 200000;
	public static final int prepareTimeLimit = 5000;
	public static final int loadTimeLimit = 10000;
	public EBattleType getBattleType() {
		return battleType;
	}
	public void setBattleType(EBattleType battleType) {
		this.battleType = battleType;
	}
	public long getBattleId() {
		return battleId;
	}
	public void setBattleId(long battleId) {
		this.battleId = battleId;
	}
	public EBattleServerState getBattleState() {
		return battleState;
	}
	public void setBattleState(EBattleServerState battleState) {
		this.battleState = battleState;
	}
	public BattleUserInfo[] getM_battleUserInfo() {
		return m_battleUserInfo;
	}
	public void setM_battleUserInfo(BattleUserInfo[] m_battleUserInfo) {
		this.m_battleUserInfo = m_battleUserInfo;
	}
	public BattleContext(EBattleType type, long battleId,List<BattleConfig> configs)
	{
		super("战斗-"+battleId,configs);
		this.battleId = battleId;
		this.battleType = type;
	}
	
	@Override
	protected void init() 
	{
         System.out.println("BattleContent:Init");
	}
	@Override
	public void run()
	{
		super.run();
		new Timer("Time out").schedule(new TimerTask() {
			@Override
			public void run() 
			{			
				BattleContext.this.checkLoadingTimeout();
				BattleContext.this.checkPrepareTimeout();
				BattleContext.this.checkSelectHeroTimeout();
			}
		},1000,1000);
	}
	public void EnterBattleState(Player player)
	{
		boolean isReconnect = player.isReconnect();
		if (isReconnect)
		{
			//通知重新连接信息
		}
		log.info("玩家"+player.getId()+"确认加入战斗房间，当前战斗状态:"+battleState.toString());
		//以后再扩展开选择符文等
	}
	/**
	 * 玩家确认选择该英雄
	 * @param player
	 * @param heroId
	 */
	public void AskSelectHero(Player player,int heroId)
	{
		BattleUserInfo info = getUserBattleInfo(player);
		info.selectedHeroId = heroId;
		info.bIsHeroChoosed = true;
		ResSelectHeroMessage msg = new ResSelectHeroMessage();
		msg.playerId = player.getId();
		msg.heroId = heroId;
		MessageUtil.tell_battlePlayer_message(this,msg);
	}
	/**
	 * 玩家发送加载完成消息
	 */
	public void EnsurePlayerLoaded(Player player)
	{
		BattleUserInfo data = this.getUserBattleInfo(player);
		data.bIsLoadedComplete = true;
		ResSceneLoadedMessage msg = new ResSceneLoadedMessage();
		msg.m_playerId = player.getId();
		MessageUtil.tell_battlePlayer_message(this, msg);
	}
	public void checkSelectHeroTimeout()
	{
		if (this.battleState != EBattleServerState.eSSBS_SelectHero)
		{
			return ;
		}
		boolean ifAllUserSelect = true;
		for (int i=0; i<maxMemberCount; i++)
		{
			if (this.m_battleUserInfo[i] != null)
			{
				if (this.m_battleUserInfo[i].bIsHeroChoosed == false)
				{
					ifAllUserSelect = false;
					break;
				}
			}
		}
		//等待时间结束
		if (ifAllUserSelect || (System.currentTimeMillis() - this.battleStateTime) >= timeLimit)
		{
			for (int i = 0; i < maxMemberCount; i++) {
				if (this.m_battleUserInfo[i] != null)
				{
					if (false == this.m_battleUserInfo[i].bIsHeroChoosed) {
						//如果还没有选择神兽，就随机选择一个
						if (this.m_battleUserInfo[i].selectedHeroId == -1)
						{
							this.m_battleUserInfo[i].selectedHeroId = randomPickHero(this.m_battleUserInfo[i].sPlayer.canUserHeroList);
						}
						this.m_battleUserInfo[i].bIsHeroChoosed = true;
						//然后将选择该神兽的消息广播给其他玩家
						ResSelectHeroMessage msg = new ResSelectHeroMessage();
						msg.heroId = this.m_battleUserInfo[i].selectedHeroId;
						msg.playerId = this.m_battleUserInfo[i].sPlayer.player.getId();
						MessageUtil.tell_battlePlayer_message(this, msg);
					}
				}
			}
			//选择神兽阶段结束，改变状态，进入准备状态
			setBattleState(EBattleServerState.eSSBS_Prepare,true);
		}
	}
	public void checkLoadingTimeout()
	{
		if (this.battleState != EBattleServerState.eSSBS_Loading)
		{
			return ;
		}
		boolean bIfAllPlayerConnect = true;
		//时间未到，则检查是否所有玩家已经连接
		if (System.currentTimeMillis() - this.battleStateTime < loadTimeLimit)
		{
			for (int i=0;i<this.m_battleUserInfo.length;i++)
			{
//				if (this.m_battleUserInfo[i].getPlayerId() != 0 && !this.m_battleUserInfo[i].isLoadCompleted())
//				{
//					bIfAllPlayerConnect = false;
//					break;
//				}
			}
		}
		if (bIfAllPlayerConnect == false)
		{
			return;
		}
		//加载静态的配置文件
		this.LoadMapConfigNpc();
		//然后创建神兽
		for (int i=0;i<this.m_battleUserInfo.length;i++)
		{
			if (this.m_battleUserInfo[i] == null)
			{
				continue;
			}
//			int count = this.m_battleUserInfo[i].getSelectedBeastList().size();
//			if (count <= 0)
//			{
//				return;
//			}
//			for (int j=0;j<count;j++)
//			{
//				SSBeast beast = this.AddBeast(this.m_battleUserInfo[i], j);			
//				this.m_battleHero++;
//				if (beast == null)
//				{
//					System.err.println("添加神兽到战场中失败");
//					break;
//				}
//				//beast.ChangeMP(0, (byte)0);
//				beast.ChangeHp(0, (byte)0);
//				//beast.ChangeCP(100, false);
//				this.ssBeastList.add(beast);				
//			}		
		}
		//通知玩家游戏开始消息
		this.PostStartGameMsg();
		this.setBattleState(EBattleServerState.eSSBS_Playing);
	}
	public void checkPrepareTimeout()
	{
		if (this.battleState != EBattleServerState.eSSBS_Prepare)
		{
			return ;
		}
		if (System.currentTimeMillis() - this.battleStateTime > prepareTimeLimit)
		{
			this.setBattleState(EBattleServerState.eSSBS_Loading, true);
		}
	}
	/**
	 * 改变游戏状态
	 * @param state
	 * @param isSendToClient
	 */
	public void setBattleState(EBattleServerState state,boolean isSendToClient)
	{
		this.battleState = state;
		this.battleStateTime = System.currentTimeMillis();
		if (isSendToClient)
		{
			switch (state) {
			case eSSBS_Prepare:
				//通知客户端开始进入准备状态
				ResGamePrepareMessage pre_msg = new ResGamePrepareMessage();
				pre_msg.setTimeLimit(prepareTimeLimit);
				MessageUtil.tell_battlePlayer_message(this, pre_msg);
				break;
			case eSSBS_Loading:
				//通知客户端开始加载场景
				ResEnterSceneMessage scene_msg = new ResEnterSceneMessage();
				MessageUtil.tell_battlePlayer_message(this, scene_msg);
				break;			
			default:
				break;
			}
		}
	}
	/**
	 * 角色进入战斗
	 * @param unit
	 * @param pos
	 * @param dir
	 * @return
	 */
//	public boolean enterBattle(SSGameUnit unit,Vector3 pos,Vector3 dir)
//	{
//		return true;
//	}
	/**
	 * 加载地图配置
	 */
	public void LoadMapConfigNpc()
	{
//		map = new SSMap();
//		map.Init(0, "server-config/map-config.xml");
	}
	/**
	 * 是否玩家能选择该神兽
	 * @param player
	 * @param beastId
	 * @return
	 */
	private boolean isCanSelectBeast(Player player,long beastId,int beastTypeId)
	{
		if (player == null)
		{
			return false;
		}
		if (this.battleState != EBattleServerState.eSSBS_SelectHero)
		{
			return false;
		}
//		//获取该玩家的信息
//		RoomMemberData info =getUserBattleInfo(player);
//		if (info.isM_bHasBeastChoosed() || info.getM_nSelectBeastTypeId() == beastTypeId)
//		{
//			return false;
//		}
//		boolean ifInBeastList = false;
//		Iterator<Long> iter = info.getBeastList().iterator();
//		while (iter.hasNext()) {
//			Long beast = (Long) iter.next();
//			if (beast == beastId)
//			{
//				ifInBeastList = true;
//				break;
//			}
//		}
//		if (false == ifInBeastList)
//		{
//			return false;
//		}
		for (int i=0; i<6; i++)
		{
//			if (this.m_battleUserInfo[i] != null){
//				if (this.m_battleUserInfo[i].getPlayerId() == player.getId())
//				{
//					continue;
//				}
//				if (this.m_battleUserInfo[i].getM_nSelectBeastTypeId() == beastTypeId)
//				{
//					return false;
//				}
//			}
		}
		return true;
	}
	/**
	 * 取得随机神兽
	 * @param pickHeroList
	 * @param camType
	 * @return
	 */
	private int randomPickHero(Set<Integer> pickHeroList)
	{
		List<Integer> canChooseList = new ArrayList<Integer>();
		if (pickHeroList == null || pickHeroList.size() == 0)
		{
			System.out.println("没有英雄可以选择");
		}
		for (int heroId : pickHeroList) 
		{
			canChooseList.add(heroId);
		}
		return canChooseList.get((int) (Math.random()*canChooseList.size()));		
	}
	/**
	 * 根据玩家取得玩家数据
	 * @param player
	 * @return
	 */
	private BattleUserInfo getUserBattleInfo(Player player)
	{
		if (player == null)
		{
			return null;
		}
		for (int i=0; i<this.m_battleUserInfo.length; i++)
		{
			if (this.m_battleUserInfo[i] == null)
			{
				continue;
			}
			if (this.m_battleUserInfo[i].sPlayer.player.getId() == player.getId())
			{
				return this.m_battleUserInfo[i];
			}
		}
		return null;
	}
	private void PostStartGameMsg()
	{
//		for (RoomMemberData data : this.m_battleUserInfo)
//		{
//			if (data == null)
//			{
//				continue;
//			}
//			ResStartGameMessage msg = new ResStartGameMessage();
//			msg.beastId = data.getSelectedBeastList().get(0);
//			msg.empireHp = 10;
//			msg.leagueHp = 10;
//			msg.playerOrder = beastOrder;
//			msg.timeLimit = 10;
//			ResSelectBornPosMessage msg2 = new ResSelectBornPosMessage();
//			msg2.setBeastId(data.getSelectedBeastList().get(0));
//			MessageUtil.tell_player_message(PlayerManager.getInstance().getPlayer(data.getPlayerId()), msg);
//			MessageUtil.tell_player_message(PlayerManager.getInstance().getPlayer(data.getPlayerId()), msg2);
//		}	
	}
	public int getBattleBeast() {
		return m_battleHero;
	}
	public void setBattleBeast(int m_battleBeast) {
		this.m_battleHero = m_battleBeast;
	}
}
