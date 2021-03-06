package solarview.spaceEncounter;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import input.MouseHook;
import nomad.universe.Universe;
import shared.SceneBase;
import shared.Vec2f;
import solarview.spaceEncounter.EncounterEntities.EncounterShipImpl;
import solarview.spaceEncounter.gui.EncounterGUI;
import solarview.spaceEncounter.rendering.EncounterRenderer;
import solarview.spaceEncounter.rendering.Targeting;
import spaceship.Spaceship;
import spaceship.stats.SpaceshipAnalyzer;
import vmo.Game;
import vmo.GameManager;

public class SpaceEncounter extends SceneBase {

	private EncounterLogic logic;
	private EncounterRenderer renderer;
	private EncounterGUI gui;
	private Targeting targeting;
	private TargetingControls targetingControl;
	private EncounterShipImpl[] buildShips(Spaceship playerShip, Spaceship[] alienShips) {
		if (playerShip.getShipStats()==null)
		{
			playerShip.setShipStats(new SpaceshipAnalyzer().generateStats(playerShip));
		}
		int c = 1;
		if (alienShips != null) {
			c += alienShips.length;
		}

		EncounterShipImpl[] list = new EncounterShipImpl[c];
		list[0] = new EncounterShipImpl(playerShip, new Vec2f(0, 0), 0);
		for (int i = 1; i < c; i++) {

			if (alienShips[i-1].getShipStats()==null)
			{
				alienShips[i-1].setShipStats(new SpaceshipAnalyzer().generateStats(alienShips[i-1]));
			}

			list[i] = new EncounterShipImpl(alienShips[i - 1],
					new Vec2f(-6 + (GameManager.m_random.nextInt(12)), 15 + GameManager.m_random.nextInt(10)),
					GameManager.m_random.nextInt(8));

		}
		return list;

	}

	public SpaceEncounter(Spaceship playerShip, Spaceship[] alienShips) {
		logic = new EncounterLogic(buildShips(playerShip, alienShips));
		renderer = new EncounterRenderer(logic.getShipList());
		renderer.setEffectHandler(logic.getEffectHandler());
		logic.setTrailControl(renderer.getTrailControl());
		gui = new EncounterGUI(logic.getShipList()[0], logic);

		renderer.position(new Vec2f(0.0F,0.0F), logic.getShipList()[0].getHeading());
		gui.setCircle(renderer.getCircle());
		targeting=new Targeting();
		targeting.setVisible(true);
		targetingControl=new TargetingControls(gui.getRangeText(),logic.getShipList()[0],targeting,logic.getShipList());
		targetingControl.Recalc(targetingControl.getIndex());
		gui.setWeaponController(new EncounterWeaponController(logic.getShipList()[0],logic.getShipList(),targetingControl));

	}

	@Override
	public void Update(float dt) {
		if (logic.isRunning()) {
			logic.update(dt);
			if (!logic.isRunning()) {
				Universe.getInstance().getPlayer().addBusy(1);
				Universe.AddClock(1);
				gui.updateUI();
				targetingControl.Recalc(targetingControl.getIndex());
				renderer.getWarpEffect().setLevel(logic.getWarpHandler().getWarpLevel());
				if (logic.getWarpHandler().getWarpLevel()>logic.getWarpHandler().MAXIMUMCHARGE)
				{
					renderer.getWarpEffect().discharge();
					logic.getShipList()[0].getSprite().setVisible(false);
				}

			}
			renderer.position(logic.getShipList()[0].getPosition(), logic.getShipList()[0].getHeading());
			renderer.getWarpEffect().update(dt);


		} else {
			gui.update(dt);
			targeting.update(dt);
			targetingControl.update(dt);
		}

	}

	@Override
	public void Draw() {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL20.glUniformMatrix4fv(m_variables[3], false, matrix44Buffer);
		GL20.glUseProgram(Game.m_pshader);
		GL20.glUniform4f(m_variables[0], 1.0F, 1.0F, 1.0F, 1);
		m_viewmatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		GL20.glUniformMatrix4fv(m_variables[1], false, matrix44Buffer);
		renderer.draw(m_variables[1], m_variables[2], m_variables[0], matrix44Buffer);
		if (!logic.isRunning())
		{
			targeting.draw(m_variables[1], m_variables[2], m_variables[0], matrix44Buffer);
		}
		gui.draw(m_variables[1], m_variables[2], m_variables[0], matrix44Buffer);
	}

	@Override
	public void start(MouseHook mouse) {
		logic.start();
		gui.start(mouse);

	}

	@Override
	public void end() {

		logic.end();
		renderer.discard();
		gui.discard();
		targeting.discard();
	}

}
