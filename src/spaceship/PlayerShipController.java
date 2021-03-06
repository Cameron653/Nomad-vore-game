package spaceship;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.lwjgl.glfw.GLFW;

import entities.Entity;
import entities.Star;
import entities.StarSystem;
import faction.Faction;
import faction.FactionLibrary;
import input.Keyboard;
import nomad.universe.Universe;
import rendering.SpriteRotatable;
import shared.Vec2f;
import solarview.SolarActionHandler;
import solarview.spaceEncounter.EncounterEntities.combatControllers.CombatController;
import view.ZoneInteractionHandler;

public class PlayerShipController implements ShipController {

	private StarSystem currentSystem;
	private float controlClock;
	private int busy;
	private Faction faction;

	public PlayerShipController(StarSystem system) {
		faction=FactionLibrary.getInstance().getFaction("player");
		currentSystem = system;
	}

	@Override
	public void update(Spaceship Ship) {

		if (busy > 0) {
			Universe.getInstance().getPlayer().Update();
			busy--;
		}
	}

	@Override
	public void setBusy(int i)
	{
		busy=i;
	}

	private Entity collisionCheck(int x, int y, Spaceship ship) {
		for (int i = 0; i < currentSystem.getEntities().size(); i++) {
			if (currentSystem.getEntities().get(i) != ship) {
				int x0 = (int) currentSystem.getEntities().get(i).getPosition().x;
				int y0 = (int) currentSystem.getEntities().get(i).getPosition().y;
				if (x == x0 && y0 == y) {
					return currentSystem.getEntities().get(i);
				}
			}
		}

		return null;
	}

	private boolean handleCollision(int x, int y, Spaceship ship) {
		Entity e = collisionCheck(x, y, ship);
		if (e != null && e.isVisible()) {
			SolarActionHandler handler = new SolarActionHandler(ship, e);
			handler.doAction();

			return true;
		}

		return false;
	}

	private void useFuel(Spaceship ship) {
		float navBonus=(ship.getShipStats().getCrewStats().getNavigation())*0.05F;
		if (navBonus > 0.5F) {
			navBonus = 0.5F;
		}
		float v = ship.getShipStats().getFuelEfficiency()*(1-navBonus);
		ship.getShipStats().getResource("FUEL")
		.setResourceAmount(ship.getShipStats().getResource("FUEL").getResourceAmount() - v);
	}

	private boolean move(int v, Spaceship ship) {
		Vec2f p = ZoneInteractionHandler.getPos(v, ship.getPosition());
		if (!handleCollision((int) p.x, (int) p.y, ship)) {
			// drain fuel, move player
			useFuel(ship);
			ship.setPosition(p);
			((SpriteRotatable) ship.getSpriteObj()).setFacing(v);
			busy += ship.getShipStats().getMoveCost();
			calcSolar(Universe.getInstance().getcurrentSystem(), ship);
			if (ship.getWarpHandler()!=null)
			{
				ship.setWarpHandler(null);
			}
			return true;
		}
		return false;
	}

	public boolean altControl(Spaceship ship) {

		if (!Keyboard.isKeyDown(GLFW_KEY_UP) && !Keyboard.isKeyDown(GLFW_KEY_W) && !Keyboard.isKeyDown(GLFW_KEY_DOWN)
				&& !Keyboard.isKeyDown(GLFW_KEY_S) && !Keyboard.isKeyDown(GLFW_KEY_LEFT)
				&& !Keyboard.isKeyDown(GLFW_KEY_A) && !Keyboard.isKeyDown(GLFW_KEY_RIGHT)
				&& !Keyboard.isKeyDown(GLFW_KEY_D)) {
			controlClock = 0;
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_5)||Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
				busy+=10;
				return true;
			}
			return false;
		} else {
			if (controlClock <= 0) {
				controlClock = 0.01F;
			} else if (controlClock > 0.1F) {
				controlClock = 0.01F;
				return altControlFinal(ship);
			}
		}

		return false;
	}

	private void calcSolar(StarSystem system, Spaceship ship) {
		Star star = (Star) system.getEntities().get(0);
		float v = star.getIntensity();
		v = (float) (v / Math.sqrt(ship.getPosition().getDistance(star.getPosition())));
		ship.getShipStats().setSolar(v);
	}

	private boolean altControlFinal(Spaceship ship) {
		boolean up = (Keyboard.isKeyDown(GLFW_KEY_UP) || Keyboard.isKeyDown(GLFW_KEY_W));
		boolean right = (Keyboard.isKeyDown(GLFW_KEY_RIGHT) || Keyboard.isKeyDown(GLFW_KEY_D));
		boolean down = (Keyboard.isKeyDown(GLFW_KEY_DOWN) || Keyboard.isKeyDown(GLFW_KEY_S));
		boolean left = (Keyboard.isKeyDown(GLFW_KEY_LEFT) || Keyboard.isKeyDown(GLFW_KEY_A));
		if (up && right) {
			return move(1, ship);
		}
		if (down && right) {
			return move(3, ship);
		}
		if (up && left) {
			return move(7, ship);
		}
		if (down && left) {
			return move(5, ship);
		}

		if (up) {
			return move(0, ship);
		}
		if (down) {
			return move(4, ship);
		}
		if (left) {
			return move(6, ship);
		}
		if (right) {
			return move(2, ship);
		}

		return false;
	}

	public void controlUpdate(float dt) {
		if (controlClock > 0) {
			controlClock += dt;

		}
	}

	public boolean control(Spaceship ship) {
		if (ship.canThrust()) {
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_8)) {
				return move(0, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_2)) {
				return move(4, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_4)) {
				return move(6, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_6)) {
				return move(2, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_9)) {
				return move(1, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_3)) {
				return move(3, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_1)) {
				return move(5, ship);
			}
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_7)) {
				return move(7, ship);
			}
			return altControl(ship);
		}
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_KP_5)||Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			busy+=10;
		}
		return false;
	}

	@Override
	public boolean canAct() {
		if (busy == 0) {
			return true;
		}
		return false;
	}

	@Override
	public CombatController getCombat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Faction getFaction() {
		return faction;
	}

	@Override
	public void setShip(Spaceship spaceship) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getExperience() {
		// TODO Auto-generated method stub
		return 0;
	}

}
