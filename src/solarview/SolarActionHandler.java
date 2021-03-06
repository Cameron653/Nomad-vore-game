package solarview;

import entities.Entity;
import entities.World;
import entities.stations.Station;
import spaceship.ShipController.scriptEvents;
import spaceship.Spaceship;
import spaceship.SpaceshipActionHandler;

public class SolarActionHandler {

	private Spaceship ship;
	private Entity target;
	public SolarActionHandler(Spaceship ship, Entity target) {
		this.ship = ship;
		this.target = target;

	}

	public void doAction() {
		if (target.getClass().getName().contains("World")) {
			new SpaceshipActionHandler().land(ship, (World) target);
		}
		if (target.getClass().getName().contains("Station")) {
			new SpaceshipActionHandler().dockStation(ship, (Station) target);
		}
		if (target.getClass().getName().contains("Spaceship")) {
			Spaceship ship2 = (Spaceship) target;
			if (ship2.getShipController() == null) {
				new SpaceshipActionHandler().join(ship, ship2);
			} else {
				ship2.getShipController().event(scriptEvents.contact);
			}

		}
	}

}
