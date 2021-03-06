package solarview.spaceEncounter.EncounterEntities;

import java.util.ArrayList;
import java.util.List;

import shipsystem.weapon.WeaponCost;
import solarview.spaceEncounter.effectHandling.EffectHandler;
import solarview.spaceEncounter.gui.EncounterLog;
import spaceship.SpaceshipResource;
import vmo.GameManager;

public class CombatActionHandler {

	private List<CombatActionImpl> actions;
	private EncounterShipImpl ship;
	private EncounterLog encounterLog;
	private float clock;

	public CombatActionHandler(EncounterShipImpl ship)
	{
		clock=0;
		this.ship=ship;

		actions=new ArrayList<CombatActionImpl>();

	}

	public List<CombatActionImpl> getList()
	{
		return actions;
	}

	private boolean checkResources(CombatActionImpl action)
	{
		for (int i=0;i<action.getWeapon().getWeapon().getWeapon().getWeaponCosts().size();i++)
		{
			WeaponCost wp=action.getWeapon().getWeapon().getWeapon().getWeaponCosts().get(i);
			if (ship.getShip().getShipStats().getResource(wp.getResourceName())==null)
			{
				return false;
			}
			float r=ship.getShip().getShipStats().getResource(wp.getResourceName()).getResourceAmount();
			if (wp.getCost()>r)
			{
				return false;
			}
		}
		return true;
	}

	private void subtractResources(CombatActionImpl action)
	{
		for (int i=0;i<action.getWeapon().getWeapon().getWeapon().getWeaponCosts().size();i++)
		{
			WeaponCost wp=action.getWeapon().getWeapon().getWeapon().getWeaponCosts().get(i);
			SpaceshipResource r=ship.getShip().getShipStats().getResource(wp.getResourceName());
			r.setResourceAmount(r.getResourceAmount()-wp.getCost());
		}
	}

	private void startEffect(CombatActionImpl action, EffectHandler effectHandler)
	{
		//check resources
		if (!checkResources(action))
		{
			return;
		}

		//subtract resources
		subtractResources(action);

		//roll dice for attack
		int roll=GameManager.m_random.nextInt(20)+ship.getShip().getShipStats().getCrewStats().getGunnery()+
				action.getWeapon().getWeapon().getWeapon().getTracking();
		int rPenalty=0;
		if (action.getWeapon().getWeapon().getWeapon().getRangePenalty() > 0)
		{
			rPenalty=(int) (action.getWeapon().getWeapon().getWeapon().getRangePenalty()*action.getTarget().getPosition().getDistance(ship.getPosition()));
		}
		int defence = 6 + action.getTarget().getEvasion()
				+
				action.getTarget().getShip().getShipStats().getCrewStats().getNavigation();
		boolean miss=false;
		if (roll-rPenalty<defence)
		{
			miss=true;

		}
		String result = !miss ? "hit" : "miss";
		encounterLog.drawText("attk:" + ship.getShip().getName() + ">" + action.getTarget().getShip().getName() + " DC:"
				+ defence + " roll:" + roll + "-" + rPenalty + " result:" + result);
		//create effect and pass hit or miss
		ship.getMonitor().reportAttack(miss);
		effectHandler.addScript(ship,action,miss);
		action.getWeapon().useWeapon();
	}

	public void update(float dt, EffectHandler effectHandler)
	{
		if (actions.size()>0)
		{
			clock-=dt;
			if (clock<=0)
			{
				startEffect(actions.get(0),effectHandler);
				actions.remove(0);
				clock=0.1F;
			}
		}
	}

	public void setLog(EncounterLog log) {
		this.encounterLog = log;
	}
}
