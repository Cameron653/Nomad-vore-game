package solarview.spaceEncounter.EncounterEntities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import rendering.Square_Rotatable_Int;
import shared.ParserHelper;
import shared.Vec2f;
import shipsystem.weapon.ShipWeapon;
import solarview.spaceEncounter.EncounterEntities.combatControllers.CombatController;
import solarview.spaceEncounter.EncounterEntities.monitoring.Monitor;
import solarview.spaceEncounter.EncounterEntities.monitoring.Ship_Monitor;
import solarview.spaceEncounter.effectHandling.EffectHandler;
import solarview.spaceEncounter.effectHandling.EffectHandler_Interface;
import solarview.spaceEncounter.interfaces.CombatAction;
import solarview.spaceEncounter.interfaces.EncounterShip;
import spaceship.Spaceship;
import vmo.GameManager;

public class EncounterShipImpl implements EncounterShip {

	private Spaceship ship;
	private CombatController controller;

	private CombatManouver manouver;
	private Square_Rotatable_Int sprite;
	private ShipEmitters emitters;
	private CombatShield shield;
	private List<CombatWeapon> weapons;
	private CombatActionHandler actionHandler;
	private Monitor monitor;

	public EncounterShipImpl(Spaceship ship, Vec2f position, int heading) {
		this.ship = ship;
		monitor=new Ship_Monitor();
		if (ship.getShipStats().getShield() != null) {
			shield = new CombatShield(ship, ship.getShipStats().getShield());
		}
		if (this.ship.getShipController() != null) {
			this.controller = this.ship.getShipController().getCombat();
		}
		if (this.ship.getShipStats().getWeapons() != null && this.ship.getShipStats().getWeapons().size() > 0) {
			weapons = new ArrayList<CombatWeapon>();
			for (int i = 0; i < this.getShip().getShipStats().getWeapons().size(); i++) {
				weapons.add(new CombatWeapon(this.getShip().getShipStats().getWeapons().get(i)));
			}
		}
		buildEmitters();
		manouver = new CombatManouver(this, position, heading);

		actionHandler=new CombatActionHandler(this);

	}

	private void buildEmitters() {
		Document doc = ParserHelper.LoadXML("assets/data/ships/" + ship.getshipName() + ".xml");

		// read through the top level nodes
		Element root = doc.getDocumentElement();
		Element n = (Element) doc.getFirstChild();
		NodeList children = n.getElementsByTagName("emitters");
		emitters = new ShipEmitters((Element) children.item(0));

	}

	public Spaceship getShip() {
		return ship;
	}

	public Square_Rotatable_Int getSprite() {
		return sprite;
	}

	public void setSprite(Square_Rotatable_Int sprite) {
		this.sprite = sprite;
	}

	@Override
	public Vec2f getPosition() {
		return manouver.getPosition();
	}

	public float getHeading() {
		return manouver.getHeading();
	}

	public CombatShield getShield() {
		return shield;
	}

	public int getCourse() {
		return manouver.getCourse();
	}

	public void setCourse(byte course) {
		manouver.setCourse(course);
	}

	public List<CombatWeapon> getWeapons() {
		return weapons;
	}

	public void update(float dt,EffectHandler handler) {
		manouver.update(dt);
		actionHandler.update(dt, handler);
		emitters.update(manouver.getPosition(),manouver.getHeading());
	}

	public void runAi(EncounterShipImpl [] allShips,EffectHandler effectHandler)
	{
		if (controller!=null)
		{
			controller.run(this,allShips,effectHandler);
		}
	}

	public void updateResources(EffectHandler handler) {
		ship.getShipStats().battleRrun();
		if (shield != null) {
			shield.update(handler,manouver.getPosition());
		}
		if (weapons != null && weapons.size() > 0) {
			for (int i = 0; i < weapons.size(); i++) {
				weapons.get(i).decrementCooldown();
			}
		}
	}

	public ShipEmitters getEmitters() {
		return emitters;
	}

	public Vec2f getEmitter(int i)
	{
		return emitters.getOffsetWeaponEmitters().get(i);
	}

	public List<CombatActionImpl> getActions() {
		return actionHandler.getList();
	}

	public Vec2f getLeading(float v) {
		return manouver.lead(v);
	}

	@Override
	public void attack(float distance, CombatAction action, EffectHandler_Interface effectHandler) {
		ShipWeapon weapon=action.getWeapon().getWeapon().getWeapon();
		int damage=weapon.getMinDamage();
		if (weapon.getMaxDamage()>weapon.getMinDamage())
		{
			damage+=GameManager.m_random.nextInt(weapon.getMaxDamage()-weapon.getMinDamage());
		}
		if (weapon.getFalloff()>0)
		{
			damage-=weapon.getFalloff()*distance;
		}
		//apply shield and get amount of shield resistance
		if (shield!=null && shield.isActive())
		{
			damage=shield.applyDefence(damage, weapon.getDisruption(),effectHandler,this);
		}

		//apply armour
		damage-=ship.getShipStats().getArmour();
		if (damage<0)
		{
			damage=0;
		}
		effectHandler.drawText(manouver.getPosition().replicate(),Integer.toString(damage), 0);
		if (damage>0)
		{
			monitor.reportDamage(damage);
			ship.getShipStats().getResource("HULL").subtractResourceAmount(damage);
		}
	}

	public Monitor getMonitor() {
		return monitor;
	}


	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

}