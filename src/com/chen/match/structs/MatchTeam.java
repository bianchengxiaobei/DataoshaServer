package com.chen.match.structs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.slf4j.impl.StaticLoggerBinder;

import com.chen.battle.structs.EBattleState;
import com.chen.battle.structs.EBattleType;
import com.chen.data.bean.MapBean;
import com.chen.data.manager.DataManager;
import com.chen.match.message.res.ResMatchStartMessage;
import com.chen.match.message.res.ResMatchTeamBaseInfoMessage;
import com.chen.match.message.res.ResMatchTeamPlayerInfoMessage;
import com.chen.player.structs.Player;
import com.chen.utils.MessageUtil;

public class MatchTeam 
{
	public static int TeamId = 0;
	private MapBean mapBean;
	private Vector<MatchPlayer> players = new Vector<MatchPlayer>();
	private int mapId;
	private int teamId;
	private EBattleMatchType matchType;
	private boolean isInMatch;
	private HashMap<Long, Boolean> stopedPlayers = new HashMap<Long, Boolean>();
	
	public MatchTeam(EBattleMatchType matchType,int mapId)
	{
		this.teamId = ++TeamId;
		this.matchType = matchType;
		this.mapId = mapId;
		this.mapBean = DataManager.getInstance().mapContainer.getList().get(mapId);
		this.isInMatch = false;
	}

	public MapBean getMapBean() {
		return mapBean;
	}

	public void setMapBean(MapBean mapBean) {
		this.mapBean = mapBean;
	}

	public Vector<MatchPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(Vector<MatchPlayer> players) {
		this.players = players;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public EBattleMatchType getMatchType() {
		return matchType;
	}

	public void setMatchType(EBattleMatchType matchType) {
		this.matchType = matchType;
	}

	public boolean isInMatch() {
		return isInMatch;
	}

	public void setInMatch(boolean isInMatch) {
		this.isInMatch = isInMatch;
	}

	public HashMap<Long, Boolean> getStopedPlayers() {
		return stopedPlayers;
	}

	public void setStopedPlayers(HashMap<Long, Boolean> stopedPlayers) {
		this.stopedPlayers = stopedPlayers;
	}
	
	
	public boolean addOneUser(MatchPlayer player)
	{
		if (this.isInMatch)
		{
			return false;
		}

		//发送给玩家的信息
		ResMatchTeamBaseInfoMessage msg1 = new ResMatchTeamBaseInfoMessage();
		msg1.teamId = this.teamId;
		msg1.matchType = this.matchType.getValue();
		msg1.mapId = this.mapId;
		MessageUtil.tell_player_message(player.getPlayer(), msg1);
		ResMatchTeamPlayerInfoMessage msg2 = new ResMatchTeamPlayerInfoMessage();
		msg2.pos = (byte)players.size();
		msg2.icon = 0;
		msg2.nickName = player.getPlayer().getUserName();
		msg2.isInsert = 1;
		byte pos = 0;
		ResMatchTeamPlayerInfoMessage msg3 = new ResMatchTeamPlayerInfoMessage();
		Iterator<MatchPlayer> playerIter = players.iterator();
		while (playerIter.hasNext())
		{
			MatchPlayer p = playerIter.next();
			msg3.pos = pos;
			msg3.nickName = p.getPlayer().getUserName();
			msg3.icon = 0;
			msg3.isInsert = 1;
			MessageUtil.tell_player_message(player.getPlayer(), msg3);
			MessageUtil.tell_player_message(p.getPlayer(), msg2);
		}
		MessageUtil.tell_player_message(player.getPlayer(),msg2);
		players.add(player);
		player.setMatchTeamId(this.teamId);
		if (players.size() == 1)
		{
			player.setMonster(true);
		}
		player.getPlayer().getBattleInfo().changeTypeWithState(EBattleType.eBattleType_Match, EBattleState.eBattleState_Wait);
		return true;
	}
	/**
	 * 取得匹配队伍里面的玩家数量
	 * @return
	 */
	public int getPlayerCount()
	{
		return this.players.size();
	}
	/**
	 * 开始寻找队友和敌方对于
	 * @param isMatch
	 */
	public void search(boolean isMatch)
	{
		this.isInMatch = isMatch;
		//给客户端发送开始搜索队友的消息，和计时等待时间
		ResMatchStartMessage msg = new ResMatchStartMessage();
		if (isMatch)
		{
			msg.setM_reason(1);
		}else
		{
			msg.setM_reason(0);
		}
		msg.setM_waitTime(90);
		for (int i=0; i<this.players.size(); i++)
		{
			MessageUtil.tell_player_message(this.players.get(i).getPlayer(),msg);
		}
	}
	/**
	 * 解散匹配队员
	 * @param player
	 * @return
	 */
	public boolean dissolve(MatchPlayer player)
	{
		if (this.isInMatch)
		{
			return false;
		}
		ResMatchTeamBaseInfoMessage message = new ResMatchTeamBaseInfoMessage();
		message.teamId = 0;
		message.mapId = 0;
		message.matchType = EBattleMatchType.MATCH_MODE_INVALID.getValue();
		Iterator<MatchPlayer> iterator = players.iterator();
		while (iterator.hasNext())
		{
			MatchPlayer current = iterator.next();
			current.setMatchTeamId(0);
			current.setMonster(false);
			current.setPunishLeftTime(0);
			MessageUtil.tell_player_message(current.getPlayer(), message);
			stopedPlayers.remove(current.getPlayer().getId());
		}
		return true;
	}
}
